package com.test.chatapp.activities;

import static android.content.ContentValues.TAG;

import static com.test.chatapp.utilities.getAddress.getAddress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.test.chatapp.adapters.PostAdapter;
import com.test.chatapp.databinding.ActivityPostingBinding;
import com.test.chatapp.databinding.ActivityProfileBinding;
import com.test.chatapp.models.Post;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private String email;
    private List<Post> posts;
    private PostAdapter postAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        listenMessages();
        setlistener();
        loadUserDetails();

    }

    private void setlistener()
    {
        //이전화면 이동
        binding.profileBack.setOnClickListener(v -> finish());
        binding.profilechange.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ProfileChangeActivity.class)));
    }

    //상단부 자신 프로필&이름 출력
    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.profilename.setText(preferenceManager.getString(Constants.KEY_NAME));

        binding.profileemail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        byte[] bytes = Base64.getDecoder().decode(preferenceManager.getString(Constants.KEY_IMAGE));
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }

    //초기화
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(
                posts,
                preferenceManager.getString(Constants.KEY_USER_ID)
        );

        binding.profileRecyclerView.setAdapter(postAdapter);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    //파이어베이스에서 모든 포스팅 가져오기
    private void listenMessages() {
        database.collection(Constants.KEY_COLLECTION_POST)
                .get()
                .addOnCompleteListener(completeListenerm);
    }

    //파이어베이스에서 내 포스팅 가져오기
    private final OnCompleteListener<QuerySnapshot> completeListenerm = task -> {
        try {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                int count = posts.size();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Log.d(TAG, document.getId() + " => " + document.getData());
                    Post post = new Post();
                    if(document.getString(Constants.KEY_USER_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                        post.postId = document.getId();
                        post.userid = document.getString(Constants.KEY_USER_ID);
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
                        post.userMessage = (List<String>) document.get("message");
                        post.postLike = (List<String>) document.get("postLike");
                        posts.add(post);
                    }
                }
                Collections.sort(posts, (obj2, obj1) -> obj1.dateObject.compareTo(obj2.dateObject));
                if (count == 0) {
                    postAdapter.notifyDataSetChanged();
                } else {
                    postAdapter.notifyItemRangeInserted(posts.size(), posts.size());
                    binding.profileRecyclerView.smoothScrollToPosition(posts.size() - 1);
                }
                binding.profileRecyclerView.setVisibility(View.VISIBLE);
            }
            binding.profileRecyclerView.smoothScrollToPosition(0);
        } catch (Exception e) {
            System.out.println(e);
        }
    };

    //뒤로가기 버튼(폰자체 뒤로가기 버튼) 클릭시 지정된 액티비티로 이동
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }

    //날짜 형식 변경
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(date);
    }
}