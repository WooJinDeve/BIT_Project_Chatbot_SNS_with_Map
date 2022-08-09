//채팅방 액티비티

package com.test.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.chatapp.adapters.ChatAdapter;
import com.test.chatapp.databinding.ActivityChatBinding;
import com.test.chatapp.httpserver.HttpClient;
import com.test.chatapp.models.ChatMessage;
import com.test.chatapp.models.User;
import com.test.chatapp.newwork.ApiClient;
import com.test.chatapp.newwork.ApiService;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    public String conversionId = null;
    private Boolean inReceiverAvailable = false;


    //챗봇 호출 함수
    public String callchatbot(String msg) {
        if (msg.contains("명소") || msg.contains("맛집"))
            msg = msg.replace("알려줘", "");
        else if (receiverUser.id.equals("@zelly") && (msg.contains("약속") || msg.contains("일정") || msg.contains("스케쥴"))) {
            msg = "젤리약속/";
        }
        String request = "chatbot/" + msg + "?me=" + preferenceManager.getString(Constants.KEY_USER_ID) + "&friend=" + receiverUser.id + "&date=" + "null" +
                "&lat=" + "null" + "&lon=" + "null" + "&content=" + "null";
        System.out.println(request);
        HttpClient httpclient = new HttpClient(request);
        Thread th = new Thread(httpclient);
        th.start();
        String result = null;

        long start = System.currentTimeMillis();

        while (result == null) {
            result = httpclient.getResult();
            long end = System.currentTimeMillis();
            if (end - start > 10000) {
                try {
                    return new JSONObject("{ " + "\"state\":\"텍스트\"" + "\"value\":서버 연결 확인해보세요" + "}").toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();
        listenMessages();
    }

    //초기화
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                preferenceManager.getString(Constants.KEY_USER_ID),
                receiverUser.id,
                getBitmapFromEncodedString(receiverUser.image),
                receiverUser.name
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        binding.ChatbotZellyHelper.bringToFront();
    }

    //메시지 전송 함수
    private void sendMessage() {
        if (binding.inputMessage.getText().toString().isEmpty()) {
            return;
        }
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP, new Date());
        message.put(Constants.KEY_MESSAGE_CHECK, "false");
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);

        //메시지가 @젤리 를 포함하고 있을 경우
        if (binding.inputMessage.getText().toString().contains("@젤리 ") || receiverUser.id.equals(Constants.KEY_CHATBOT)) {
            String tempmsg = binding.inputMessage.getText().toString();
            String msg = callchatbot(tempmsg.replace("@젤리 ", "") + "/");

            HashMap<String, Object> chatbotmessage = new HashMap<>();
            chatbotmessage.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            chatbotmessage.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            chatbotmessage.put(Constants.KEY_MESSAGE, "@Zelly " + msg);
            chatbotmessage.put(Constants.KEY_TIMESTAMP, new Date());
            chatbotmessage.put(Constants.KEY_MESSAGE_CHECK, "false");
            database.collection(Constants.KEY_COLLECTION_CHAT).add(chatbotmessage);
        }

        if (conversionId != null) {
            if (binding.inputMessage.getText().toString().contains("@Picture")) {
                updateConversion("사진");
                ;
            } else {
                updateConversion(binding.inputMessage.getText().toString());
            }
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            if (binding.inputMessage.getText().toString().contains("@Picture")) {
                conversion.put(Constants.KEY_LAST_MESSAGE, "사진");
            } else {
                conversion.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
            }
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!inReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());
            } catch (Exception exception) {
                showToast(exception.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    //뒤로가기 버튼(폰자체 뒤로가기 버튼) 클릭시 지정된 액티비티로 이동
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(ChatActivity.this, MainActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void mOnPopupClick(View v) {
//데이터 담아서 팝업(액티비티) n호출
        Intent intent = new Intent(this, PopupActivity.class);
        intent.putExtra("data", "Popup");
        startActivityForResult(intent, 1);
    }

    //
    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("에러: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    //상대방 접속 여부 (online/offline)
    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    inReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if (receiverUser.image == null) {
                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (inReceiverAvailable) {
                binding.textAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.textAvailability.setVisibility(View.GONE);
            }

        });
    }

    //메시지 수신부
    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    //메시지 수신부
    private final com.google.firebase.firestore.EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.documentId = documentChange.getDocument().getId();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessage.check = documentChange.getDocument().getString(Constants.KEY_MESSAGE_CHECK);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkForConversion();
        }
    });

    //비트맵 인코딩(스트링->비트맵)
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    //메시지 수신시 발송인의 이름과 사진 출력
    private void loadReceiverDetails() {
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);
    }

    //클릭 이벤트 처리
    private void setListeners() {
        //뒤로가기 버튼 클릭
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        //메시지 버튼 클릭
        binding.layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                binding.ChatbotZellyHelper.setVisibility(View.GONE);
            }
        });
        //이미지 버튼 클릭
        binding.layoutImage.setOnClickListener(v -> selectImage());
        //젤리 이미지 클릭
        binding.ChatbotZelly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputTextZelly();
                binding.ChatbotZellyHelper.setVisibility(View.VISIBLE);
            }
        });
    }

    //젤리 이미지 클릭시 @젤리 문구 텍스트창에 입력
    private void inputTextZelly() {
        binding.inputMessage.setText("@젤리 ");
        binding.inputMessage.setSelection(binding.inputMessage.length());
    }

    //전송할 이미지 선택
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, 1);
    }

    //사진 선택 및 전송
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
//데이터 받기
                String result = data.getStringExtra("result");
            } else {
                try {
                    String path = "image/" + preferenceManager.getString(Constants.KEY_USER_ID) + "_" + getImageDateTime(new Date());
                    String message = "@Picture " + path;

                    Uri file = data.getData();
                    StorageReference storageRef = storage.getReference();
                    StorageReference riversRef = storageRef.child(path);

                    UploadTask uploadTask = riversRef.putFile(file);
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            binding.inputMessage.setText(message);
                            sendMessage();
                        }
                    });
                } catch (Exception e) {
                    showToast("사진 선택을 취소합니다.");
                }
            }
        }
    }

    //사진 전송한 시간 출력
    private String getImageDateTime(Date date) {
        return new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault()).format(date);

    }

    //현재시간 변환 코드
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyy - hh:mm a", Locale.getDefault()).format(date);
    }

    //대화방 추가
    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    //대화방 업데이트
    private void updateConversion(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE,
                message, Constants.KEY_TIMESTAMP, new Date()
        );
    }

    //대화방 체크?
    private void checkForConversion() {
        if (chatMessages.size() != 0) {
            checkForConversionRemotely(
                    preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversionRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    //대화방 체크?
    private void checkForConversionRemotely(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);
    }

    //대화방 체크?
    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }


}