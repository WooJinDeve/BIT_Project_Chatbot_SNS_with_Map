//유저액티비티

package com.test.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.chatapp.adapters.UsersAdapter;
import com.test.chatapp.databinding.ActivityUserBinding;
import com.test.chatapp.listeners.UserListener;
import com.test.chatapp.models.User;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserActivity extends BaseActivity implements UserListener {

    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    //초기화
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListeners();
        getFriendUsers();
    }

    //초기화
    private void init(){
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    //클릭 이벤트
    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.followBtn.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), FollowActivity.class)));
    }

    //친구찾기
    private void getFriendUsers() {
        try {
            database.collection(Constants.KEY_COLLECTION_USERS)
                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                    .collection(Constants.KEY_FRIEND)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            List<User> users = new ArrayList<>();
                            loading(false);
                            for (QueryDocumentSnapshot queryDocumentSnapshot : querySnapshot) {
                                User user = new User();
                                user.id = queryDocumentSnapshot.getId();
                                user.state = queryDocumentSnapshot.getString(Constants.KEY_STATE);
                                users.add(user);
                            }

                            database.collection(Constants.KEY_COLLECTION_USERS)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot querySnapshot1) {
                                            List<User> requestUsers = new ArrayList<>();
                                            List<User> friendUsers = new ArrayList<>();
                                            for (QueryDocumentSnapshot queryDocumentSnapshot : querySnapshot1) {
                                                for (User user : users) {
                                                    if (user.id.equals(queryDocumentSnapshot.getId())) {
                                                        User us = new User();
                                                        us.id = queryDocumentSnapshot.getId();
                                                        us.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                                        us.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                                        us.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                                        us.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                                        us.state = user.state;
                                                        us.receiveId = preferenceManager.getString(Constants.KEY_USER_ID);
                                                        if (us.state.equals("false")) {
                                                            requestUsers.add(us);
                                                        } else if (us.state.equals("true")) {
                                                            friendUsers.add(us);
                                                        }
                                                    }
                                                }
                                            }
                                            if (requestUsers.size() > 0) {
                                                UsersAdapter requestAdapter = new UsersAdapter(requestUsers, UserActivity.this);
                                                binding.followRecyclerView.setAdapter(requestAdapter);
                                                binding.followRecyclerView.setVisibility(View.VISIBLE);
                                                binding.followText.setVisibility(View.VISIBLE);
                                            }
                                            if (friendUsers.size() > 0) {
                                                UsersAdapter friendAdapter = new UsersAdapter(friendUsers, UserActivity.this);
                                                binding.friendRecyclerView.setAdapter(friendAdapter);
                                                binding.friendRecyclerView.setVisibility(View.VISIBLE);
                                                binding.friendText.setText("친구 " + friendUsers.size() + "명");
                                            }
                                        }

                                    });
                        }
                    });
        } catch (Exception exceptione) {
            Toast.makeText(getApplicationContext(), "실패", Toast.LENGTH_SHORT).show();
        }
    }

    //뒤로가기 버튼(폰자체 뒤로가기 버튼) 클릭시 지정된 액티비티로 이동
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(UserActivity.this, MainActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }

    //로딩 관련
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //검색된 유저 클릭시 ChatActivity로 이동
    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}