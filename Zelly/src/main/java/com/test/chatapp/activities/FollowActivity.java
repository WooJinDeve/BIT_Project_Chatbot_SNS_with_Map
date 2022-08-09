package com.test.chatapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.chatapp.databinding.ActivityFollowBinding;
import com.test.chatapp.databinding.ActivityUserBinding;
import com.test.chatapp.models.Calendar;
import com.test.chatapp.models.User;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.util.Base64;
import java.util.HashMap;

public class FollowActivity extends BaseActivity{

    private ActivityFollowBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String receiveUser = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFollowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
        userFollow();
    }

    //초기화
    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
    }

    //뒤로가기 버튼(폰자체 뒤로가기 버튼) 클릭 시 지정된 액티비티로 이동
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(FollowActivity.this, UserActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }

    //클릭이벤트
    private void setListener(){
        binding.selectButton.setOnClickListener(v -> getUser());
        binding.friendFollow.setOnClickListener(v -> userFollow());

        //뒤로가기 버튼 클릭 이벤트 처리
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FollowActivity.this, UserActivity.class);
                startActivity(intent);
            }
        });

        binding.imageBack.setOnClickListener(v->finish());
    }

    //유저검색
    private void getUser(){
        if (binding.selectId.getText().toString().isEmpty())
            return;

        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_FRIEND)
                .whereEqualTo(Constants.KEY_EMAIL, binding.selectId.getText().toString())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots.size() == 1) {
                            Toast.makeText(getApplicationContext(), "이미 친구이거나 수락 대기상태입니다.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else {
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .whereEqualTo(Constants.KEY_EMAIL, binding.selectId.getText().toString())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            try{
                                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                                if (documentSnapshot.getId().equals(preferenceManager.getString(Constants.KEY_USER_ID)))
                                                    return;

                                                receiveUser  = documentSnapshot.getId();
                                                binding.imageProfile.setImageBitmap(getUserImage(documentSnapshot.getString(Constants.KEY_IMAGE)));
                                                binding.textName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                                                binding.textEmail.setText(documentSnapshot.getString(Constants.KEY_EMAIL));

                                                binding.imageProfile.setVisibility(View.VISIBLE);
                                                binding.textName.setVisibility(View.VISIBLE);
                                                binding.textEmail.setVisibility(View.VISIBLE);
                                                binding.friendFollow.setVisibility(View.VISIBLE);

                                            } catch (Exception exception){
                                                Toast.makeText(getApplicationContext(), "존재하지 않는 이메일입니다.",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    //친구추가
    private void userFollow(){
        try {
            if (receiveUser  == null)
                return;
            HashMap<String, Object> receiveuser = new HashMap<>();
            receiveuser.put(Constants.KEY_STATE, "done");
            receiveuser.put(Constants.KEY_EMAIL, binding.textEmail.getText().toString());

            HashMap<String, Object> senderUser = new HashMap<>();
            senderUser.put(Constants.KEY_STATE, "false");
            senderUser.put(Constants.KEY_EMAIL,preferenceManager.getString(Constants.KEY_EMAIL));

            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .collection(Constants.KEY_FRIEND)
                    .document(receiveUser)
                    .set(receiveuser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .document(receiveUser)
                                    .collection(Constants.KEY_FRIEND)
                                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                    .set(senderUser)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            binding.imageProfile.setVisibility(View.GONE);
                                            binding.textName.setVisibility(View.GONE);
                                            binding.textEmail.setVisibility(View.GONE);
                                            binding.friendFollow.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    });
        }catch (Exception e){}
    }

    //이미지 디코딩
    private Bitmap getUserImage(String encodedImage) {
        byte[] bytes = Base64.getDecoder().decode(encodedImage);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

