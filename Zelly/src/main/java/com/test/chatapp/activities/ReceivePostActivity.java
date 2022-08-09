package com.test.chatapp.activities;

import static android.content.ContentValues.TAG;

import static com.test.chatapp.utilities.getAddress.getAddress;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.test.chatapp.adapters.PostAdapter;
import com.test.chatapp.databinding.ActivityPostViewBinding;
import com.test.chatapp.models.Post;
import com.test.chatapp.models.User;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReceivePostActivity extends BaseActivity {
    private ActivityPostViewBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private List<Post> posts;
    private PostAdapter postAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        listenMessages();
        clickListener();

        binding.postBack.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), GoogleMapActivity.class)));
    }

    //초기화
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(
                posts,
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.postRecyclerView.setAdapter(postAdapter);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    //파이어베이스에서 모든 포스팅 가져오기
    private void listenMessages() {
        database.collection("post")
                .get()
                .addOnCompleteListener(completeListenerm);
    }

    private void clickListener(){
        binding.refreshPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    posts.clear();
                    listenMessages();
                } catch (Exception e){
                    System.out.println(e);
                }
            }
        });
    }

    //뒤로가기 버튼(폰자체 뒤로가기 버튼) 클릭시 지정된 액티비티로 이동
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(ReceivePostActivity.this, GoogleMapActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }

    //파이어베이스에서 모든 포스팅 가져오기
    private final OnCompleteListener<QuerySnapshot> completeListenerm = task -> {
        try {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                int count = posts.size();
                for (QueryDocumentSnapshot document : task.getResult()) {

                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Post post = new Post();
                    post.postId = document.getId();
                    post.userid = document.getString(Constants.KEY_USER_ID);
                    if(post.userid.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                        post.privatekey =  document.getBoolean(Constants.KEY_PRIVATEKEY);
                        post.name = document.getString(Constants.KEY_NAME);
                        post.image = document.getString(Constants.KEY_IMAGE);
                        post.postImage = document.getString(Constants.KEY_POSTIMAGE);
                        post.postcontents = document.getString(Constants.KEY_POSTCONTENTS);
                        post.lat = document.getDouble(Constants.KEY_POSTLAT);
                        post.lon = document.getDouble(Constants.KEY_POSTLON);
                        post.hashTags = document.getString(Constants.KEY_POSTING_HASHTAGS);
                        post.dateTime = getReadableDateTime(document.getDate(Constants.KEY_TIMESTAMP));
                        post.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                        post.address = getAddress(this, post.lat, post.lon);
                        post.placeName = document.getString(Constants.KEY_PLACENAME);
                        post.userMessage = (List<String>) document.get("message");
                        post.postLike = (List<String>) document.get("postLike");
                        posts.add(post);
                        continue;
                    }
                    int sign = 0;
                    for (User us : GoogleMapActivity.friendsList) {
                        if (sign == 0) {
                            if (post.userid.equals(us.id)) {
                                post.privatekey =  document.getBoolean(Constants.KEY_PRIVATEKEY);
                                if(!post.privatekey){
                                    post.name = document.getString(Constants.KEY_NAME);
                                    post.image = document.getString(Constants.KEY_IMAGE);
                                    post.postImage = document.getString(Constants.KEY_POSTIMAGE);
                                    post.postcontents = document.getString(Constants.KEY_POSTCONTENTS);
                                    post.lat = document.getDouble(Constants.KEY_POSTLAT);
                                    post.lon = document.getDouble(Constants.KEY_POSTLON);
                                    post.hashTags = document.getString(Constants.KEY_POSTING_HASHTAGS);
                                    post.dateTime = getReadableDateTime(document.getDate(Constants.KEY_TIMESTAMP));
                                    post.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                    post.address = getAddress(this, post.lat, post.lon);
                                    post.placeName = document.getString(Constants.KEY_PLACENAME);
                                    post.userMessage = (List<String>) document.get("message");
                                    post.postLike = (List<String>) document.get("postLike");
                                    posts.add(post);
                                    sign = 1;
                                }
                            }
                        } else {
                            sign = 0;
                            continue;
                        }
                    }
                }
                Collections.sort(posts, (obj2, obj1) -> obj1.dateObject.compareTo(obj2.dateObject));
                if (count == 0) {
                    postAdapter.notifyDataSetChanged();
                } else {
                    postAdapter.notifyItemRangeInserted(posts.size(), posts.size());
                    binding.postRecyclerView.smoothScrollToPosition(posts.size() - 1);
                }
                binding.postRecyclerView.setVisibility(View.VISIBLE);
            }
            binding.progressBar.setVisibility(View.GONE);
            binding.postRecyclerView.smoothScrollToPosition(0);
        } catch (Exception e) {
            System.out.println(e);
        }
    };

    //날짜 형식 변경
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(date);
    }
}