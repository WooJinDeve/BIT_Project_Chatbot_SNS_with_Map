//프로필 변경 액티비티

package com.test.chatapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.chatapp.R;
import com.test.chatapp.adapters.ChatAdapter;
import com.test.chatapp.adapters.ChatbotScheduleAdapter;
import com.test.chatapp.adapters.PostUserMessageAdapter;
import com.test.chatapp.databinding.ActivityProfileChangeBinding;
import com.test.chatapp.models.ChatMessage;
import com.test.chatapp.models.Schedule;
import com.test.chatapp.models.UserSchedule;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ProfileChangeActivity extends AppCompatActivity {

    private String UserID;
    private ActivityProfileChangeBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String encodedImage;
    public Context context = this;
    public Button[] buttons = new Button[7];
    private UserSchedule userSchedule = new UserSchedule();
    private List<UserSchedule> userSchedules;
    private String image;
    private ChatbotScheduleAdapter chatbotScheduleAdapter;

    //초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileChangeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        getScheduleLoader();

        setListeners();
    }

    private void init() {
        database = FirebaseFirestore.getInstance();
        userSchedules = new ArrayList<>();
        chatbotScheduleAdapter = new ChatbotScheduleAdapter(
                userSchedules, this
        );
        preferenceManager = new PreferenceManager(getApplicationContext());
        UserID = preferenceManager.getString(Constants.KEY_USER_ID);
        binding.username.setText(preferenceManager.getString((Constants.KEY_NAME)));
        byte[] bytes = Base64.getDecoder().decode(preferenceManager.getString(Constants.KEY_IMAGE));
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
        encodedImage = preferenceManager.getString(Constants.KEY_IMAGE);
    }

    //클릭 이벤트
    private void setListeners() {
        binding.ImageChange.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.update.setOnClickListener(v -> {
            Update();
        });

        binding.imageBack.setOnClickListener(v -> onBackPressed());
        //뒤로가기
        binding.change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(
                        context, R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.layout_bottom_recyclerview, (LinearLayout) findViewById(R.id.bottonSheetRecyclerView)
                );

                RecyclerView recyclerView = bottomSheetView.findViewById(R.id.scheduleRecyclerView);
                recyclerView.setAdapter(chatbotScheduleAdapter);

                bottomSheetView.findViewById(R.id.scheduleButtonButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                                context, R.style.BottomSheetDialogTheme
                        );
                        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                                R.layout.layout_bottom_sheet, (LinearLayout) findViewById(R.id.bottomSheetContainer)
                        );

                        buttons[0] = bottomSheetView.findViewById(R.id.btn0);
                        buttons[1] = bottomSheetView.findViewById(R.id.btn1);
                        buttons[2] = bottomSheetView.findViewById(R.id.btn2);
                        buttons[3] = bottomSheetView.findViewById(R.id.btn3);
                        buttons[4] = bottomSheetView.findViewById(R.id.btn4);
                        buttons[5] = bottomSheetView.findViewById(R.id.btn5);
                        buttons[6] = bottomSheetView.findViewById(R.id.btn6);

                        TimePicker timePicker = bottomSheetView.findViewById(R.id.timePicker);
                        TextView Starttime = bottomSheetView.findViewById(R.id.startTimeValue);
                        TextView StartNTimeName = bottomSheetView.findViewById(R.id.startTimeName);
                        TextView Endtime = bottomSheetView.findViewById(R.id.endTimeValue);
                        TextView EndtimeName = bottomSheetView.findViewById(R.id.endTimeName);
                        Switch repeat = bottomSheetView.findViewById(R.id.timeSwitch);

                        repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    userSchedule.repeat = true;
                                } else {
                                    userSchedule.repeat = false;
                                }

                            }
                        });
                        //타임피커 시작시간으로 설정
                        Starttime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                StartNTimeName.setTextColor(Color.parseColor("#FFFFFF"));
                                Starttime.setTextColor(Color.parseColor("#FFFFFF"));
                                EndtimeName.setTextColor(Color.parseColor("#80000000"));
                                Endtime.setTextColor(Color.parseColor("#80000000"));

                                Starttime.setBackgroundResource(R.drawable.round_background_test);
                                Endtime.setBackgroundResource(0);
                                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                                    @Override
                                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                        String startHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;

                                        String startMiute = minute < 10 ? "0" + minute : "" + minute;
                                        if (hourOfDay >= 12) {
                                            Starttime.setText("오후 " + startHourOfDay + " : " + startMiute);
                                            hourOfDay += 1;
                                            if (hourOfDay == 24) {
                                                String EndstartHourOfDay = "00";
                                                Endtime.setText("오전 " + (EndstartHourOfDay) + " : " + startMiute);
                                            } else {
                                                String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                                Endtime.setText("오후 " + (EndstartHourOfDay) + " : " + startMiute);
                                            }

                                        } else {
                                            Starttime.setText("오전 " + startHourOfDay + " : " + startMiute);
                                            hourOfDay += 1;
                                            if (hourOfDay == 12) {
                                                String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                                Endtime.setText("오후 " + (EndstartHourOfDay) + " : " + startMiute);
                                            } else {
                                                String EndstartHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                                Endtime.setText("오전 " + (EndstartHourOfDay) + " : " + startMiute);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        //타임피커 종료시간 설정
                        Endtime.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                StartNTimeName.setTextColor(Color.parseColor("#80000000"));
                                Starttime.setTextColor(Color.parseColor("#80000000"));
                                EndtimeName.setTextColor(Color.parseColor("#FFFFFF"));
                                Endtime.setTextColor(Color.parseColor("#FFFFFF"));

                                Starttime.setBackgroundResource(R.drawable.round_background_test);
                                Endtime.setBackgroundResource(0);
                                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                                    @Override
                                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                                        String endHourOfDay = hourOfDay >= 13 ? (hourOfDay - 12 >= 10 ? hourOfDay - 12 + "" : "0" + (hourOfDay - 12)) : hourOfDay >= 10 ? hourOfDay + "" : "0" + hourOfDay;
                                        String endMiute = minute < 10 ? "0" + minute : "" + minute;
                                        if (hourOfDay >= 12) {
                                            Endtime.setText("오후 " + endHourOfDay + " : " + endMiute);
                                        } else {
                                            Endtime.setText("오전 " + endHourOfDay + " : " + endMiute);
                                        }
                                    }
                                });
                            }
                        });
                        for (int i = 0; i < 7; i++) {
                            if (userSchedule.days[i].equals("1")) {
                                buttons[i].setBackgroundResource(R.drawable.shadow_round);
                            } else {
                                buttons[i].setBackgroundResource(0);
                            }


                            int finalI = i;
                            buttons[i].setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (userSchedule.days[finalI].equals("1")) {
                                        userSchedule.days[finalI] = "0";
                                        buttons[finalI].setBackgroundResource(0);
                                    } else {
                                        userSchedule.days[finalI] = "1";
                                        buttons[finalI].setBackgroundResource(R.drawable.shadow_round);
                                    }
                                }
                            });
                        }
                        bottomSheetView.findViewById(R.id.changebutton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int max = Integer.parseInt(userSchedules.size() == 0 ? "1" : userSchedules.get(0).document);
                                for (int i = 1; i < userSchedules.size(); i++) {
                                    max = Math.max(max, Integer.parseInt(userSchedules.get(i).document));
                                }
                                userSchedule.document = String.valueOf(max + 1);
                                userSchedule.state = true;
                                userSchedule.time = timeParse2(Starttime.getText().toString(), Endtime.getText().toString());
                                HashMap<String, Object> schedule = new HashMap<>();
                                schedule.put(Constants.KEY_SCHEDULE_REPEAT, userSchedule.repeat);
                                schedule.put(Constants.KEY_SCHEDULE_DAYS, Arrays.asList(userSchedule.days));
                                schedule.put(Constants.KEY_STATE, userSchedule.state);
                                schedule.put(Constants.KEY_TIMESTAMP, userSchedule.time);
                                database.collection(Constants.KEY_COLLECTION_USERS)
                                        .document(UserID)
                                        .collection(Constants.KEY_SCHEDULE)
                                        .document(userSchedule.document)
                                        .set(schedule).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                recyclerView.setAdapter(null);
                                                ChatbotScheduleAdapter chatbotScheduleAdapter = new ChatbotScheduleAdapter(userSchedules, context);
                                                recyclerView.setAdapter(chatbotScheduleAdapter);
                                            }
                                        });
                                bottomSheetDialog.dismiss();

                            }
                        });

                        bottomSheetView.findViewById(R.id.canclebutton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                bottomSheetDialog.dismiss();
                            }
                        });

                        Starttime.performClick();
                        bottomSheetDialog.setContentView(bottomSheetView);
                        bottomSheetDialog.show();
                    }
                });
                bottomSheetDialog1.setContentView(bottomSheetView);
                bottomSheetDialog1.show();
            }
        });
    }

    private String timeParse2(String start, String end) {
        String[] stemp = start.split(" ");
        String[] etemp = end.split(" ");
        if (stemp[0].equals("오후")) {
            if (Integer.parseInt(stemp[1]) == 12)
                stemp[1] = "12";
            else if (Integer.parseInt(stemp[1]) > 23) {
                stemp[1] = String.valueOf(Integer.parseInt(stemp[1]) - 12);
            } else if(Integer.parseInt(stemp[1]) < 12)
                stemp[1] = String.valueOf(Integer.parseInt(stemp[1]) + 12);
        }
        if (etemp[0].equals("오후")) {
            if (Integer.parseInt(etemp[1]) == 12)
                etemp[1] = "12";
            else if (Integer.parseInt(etemp[1]) > 23) {
                etemp[1] = String.valueOf(Integer.parseInt(etemp[1]) - 12);
            } else if(Integer.parseInt(etemp[1]) < 12)
                etemp[1] = String.valueOf(Integer.parseInt(etemp[1]) + 12);

        }
        return stemp[1] + ":" + stemp[3] + ":" + etemp[1] + ":" + etemp[3];
    }


    private void getScheduleLoader() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(UserID)
                .collection(Constants.KEY_SCHEDULE)
                .addSnapshotListener(eventListener);
    }

    private final com.google.firebase.firestore.EventListener<QuerySnapshot> eventListener = ((value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    UserSchedule user = new UserSchedule();
                    user.document = documentChange.getDocument().getId();
                    List<String> days = (List<String>) documentChange.getDocument().getData().get(Constants.KEY_SCHEDULE);
                    user.days = days.toArray(new String[7]);
                    user.repeat = documentChange.getDocument().getBoolean(Constants.KEY_SCHEDULE_REPEAT);
                    user.time = documentChange.getDocument().getString(Constants.KEY_TIMESTAMP);
                    user.state = documentChange.getDocument().getBoolean(Constants.KEY_STATE);
                    userSchedules.add(user);
                }
            }
        }
    });


    //갤러리 사진 선택
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );


    //이미지 인코딩
    public String encodedImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(ProfileChangeActivity.this, ProfileActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }

    //업데이트 버튼 클릭 함수
    private void Update() {
        //users 컬렉션 업데이트
        setUsersProfileUpdate(UserID);
        //conversions 컬렌션 업데이트
        setProfileUpdate(UserID);
        preferenceManager.putString(Constants.KEY_NAME, binding.username.getText().toString());
        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
    }

    //파이어베이스 내용 업데이트
    private void setProfileUpdate(String ID) {
        //<editor-fold desc="파이어베이스 update코드">
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, ID)
                .get()
                .addOnCompleteListener(consender);

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, ID)
                .get()
                .addOnCompleteListener(conrecive);

        database.collection(Constants.KEY_COLLECTION_CALENDAR)
                .whereEqualTo(Constants.KEY_CALENDARME, ID)
                .get()
                .addOnCompleteListener(calmy);

        database.collection(Constants.KEY_COLLECTION_CALENDAR)
                .whereEqualTo(Constants.KEY_CALENDARFRIEND, ID)
                .get()
                .addOnCompleteListener(calfr);

        database.collection(Constants.KEY_COLLECTION_POST)
                .whereEqualTo(Constants.KEY_USER_ID, ID)
                .get()
                .addOnCompleteListener(post);
        // </editor-fold>
    }

    //파이어베이스에 유저 컬렉션 변경
    private void setUsersProfileUpdate(String ID) {
        DocumentReference database = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS).document(ID);
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.username.getText().toString());
        user.put(Constants.KEY_IMAGE, encodedImage);
        database.update(user);
    }


//<editor-fold desc="파이어베이스 update코드">


    //파이어베이스 users의내용 변경
    private final OnCompleteListener<QuerySnapshot> consender = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                String conversionId = task.getResult().getDocuments().get(i).getId();
                DocumentReference database = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_SENDER_NAME, binding.username.getText().toString());
                user.put(Constants.KEY_SENDER_IMAGE, encodedImage);
                database.update(user);
            }

        }
    };

    //파이어베이스 users의내용 변경
    private final OnCompleteListener<QuerySnapshot> conrecive = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                String conversionId = task.getResult().getDocuments().get(i).getId();
                DocumentReference database = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_RECEIVER_NAME, binding.username.getText().toString());
                user.put(Constants.KEY_RECEIVER_IMAGE, encodedImage);
                database.update(user);
            }

        }
    };

    //파이어베이스 calendar의내용 변경
    private final OnCompleteListener<QuerySnapshot> calmy = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                String conversionId = task.getResult().getDocuments().get(i).getId();
                DocumentReference database = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CALENDAR).document(conversionId);
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_MYNAME, binding.username.getText().toString());
                database.update(user);
            }

        }
    };

    //파이어베이스 calendar의내용 변경
    private final OnCompleteListener<QuerySnapshot> calfr = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                String conversionId = task.getResult().getDocuments().get(i).getId();
                DocumentReference database = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_CALENDAR).document(conversionId);
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_FRIENDNAME, binding.username.getText().toString());
                database.update(user);
            }

        }
    };

    //파이어베이스 post의내용 변경
    private final OnCompleteListener<QuerySnapshot> post = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            for (int i = 0; i < task.getResult().getDocuments().size(); i++) {
                String conversionId = task.getResult().getDocuments().get(i).getId();
                DocumentReference database = FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_POST).document(conversionId);
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_NAME, binding.username.getText().toString());
                user.put(Constants.KEY_IMAGE, encodedImage);
                database.update(user);
            }

        }
    };


    // </editor-fold>
}