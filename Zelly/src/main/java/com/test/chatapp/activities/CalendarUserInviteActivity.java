//CalendarUserInviteActivity = 캘린더에서 친구 초대

package com.test.chatapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.test.chatapp.adapters.InviteAdapter;
import com.test.chatapp.adapters.UsersAdapter;
import com.test.chatapp.databinding.ActivityCalendarUserInviteBinding;
import com.test.chatapp.listeners.UserListener;
import com.test.chatapp.models.User;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class CalendarUserInviteActivity extends BaseActivity implements UserListener {
    private ActivityCalendarUserInviteBinding binding;
    private PreferenceManager preferenceManager;
    private String documentid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarUserInviteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        setListeners();
        getUsers();
    }

    //초기화
    private void init() {
        Intent getIntent = getIntent();
        if (getIntent.getExtras() != null) {
            documentid = getIntent.getStringExtra(Constants.KEY_CALENDARDOCUMENT);

        }
    }


    //클릭 이벤트
    private void setListeners() {
        //뒤로가기
        binding.imageBack.setOnClickListener(v -> finish());

    }
    private void getUsers() {
        try {
            FirebaseFirestore database = FirebaseFirestore.getInstance();
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
                                                        if (us.state.equals("true") && !us.id.equals(Constants.KEY_CHATBOT)) {
                                                            friendUsers.add(us);
                                                        }
                                                    }
                                                }
                                            }
                                            if (friendUsers.size() > 0) {
                                                UsersAdapter friendAdapter = new UsersAdapter(friendUsers, CalendarUserInviteActivity.this);
                                                binding.userRecyclerView.setAdapter(friendAdapter);
                                                binding.userRecyclerView.setVisibility(View.VISIBLE);
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


    //로딩 될 때 로딩바 출력
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    //친구 정보 클릭 했을 때 이벤트 처리
    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent();
        CalendarAddActivity.calendar.friend = user.id;
        CalendarAddActivity.calendar.friend_name = user.name;
        CalendarAddActivity.calendar.documentid = documentid;
        setResult(1000, intent);
        finish();
    }
}