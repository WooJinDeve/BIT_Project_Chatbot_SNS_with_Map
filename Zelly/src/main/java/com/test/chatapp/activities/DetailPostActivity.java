//마커클릭시 ->포스트 이동

package com.test.chatapp.activities;

import static com.test.chatapp.utilities.Constants.postingHashTag;
import static com.test.chatapp.utilities.getAddress.getAddress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.test.chatapp.adapters.HashTagAdapter;
import com.test.chatapp.adapters.PostUserMessageAdapter;
import com.test.chatapp.databinding.ActivityDetailImageBinding;
import com.test.chatapp.databinding.ActivityDetailPostBinding;
import com.test.chatapp.databinding.ActivityDetailPostVideoBinding;
import com.test.chatapp.databinding.ItemContainerFeedBinding;
import com.test.chatapp.models.Post;
import com.test.chatapp.utilities.Constants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class DetailPostActivity extends BaseActivity {
    private ActivityDetailPostBinding binding;
    private ActivityDetailPostVideoBinding videobinding;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailPostBinding.inflate(getLayoutInflater());
        videobinding = ActivityDetailPostVideoBinding.inflate(getLayoutInflater());


        database = FirebaseFirestore.getInstance();

        if (getIntent().getStringExtra("data") == null) {
            Toast.makeText(getApplicationContext(), "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            postIdToData(getIntent().getStringExtra("data"));
        }
        setListener();

    }

    //클릭 이벤트 처리
    private void setListener() {
        binding.postBack.setOnClickListener(v -> onBackPressed());
    }

    //파이어베이스에 있는 문서고유번호를 통해 해당 문서 정보 가져오기
    private void postIdToData(String postid) {
        database.collection("post")
                .document(postid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                DocumentSnapshot document = task.getResult();
                                Post post = new Post();
                                post.name = document.getString(Constants.KEY_NAME);
                                post.image = document.getString(Constants.KEY_IMAGE);
                                post.postImage = document.getString(Constants.KEY_POSTIMAGE);
                                post.postcontents = document.getString(Constants.KEY_POSTCONTENTS);
                                post.lat = document.getDouble(Constants.KEY_POSTLAT);
                                post.lon = document.getDouble(Constants.KEY_POSTLON);
                                post.dateTime = getReadableDateTime(document.getDate(Constants.KEY_TIMESTAMP));
                                post.dateObject = document.getDate(Constants.KEY_TIMESTAMP);
                                post.address = getAddress(getApplicationContext(), post.lat, post.lon);
                                post.hashTags = document.getString(Constants.KEY_POSTING_HASHTAGS);
                                post.placeName = document.getString(Constants.KEY_PLACENAME);
                                post.userMessage = (List<String>) document.get("message");
                                post.postLike = (List<String>) document.get("postLike");
                                DetailPost(post.name, post.image, post.postImage, post.postcontents, post.hashTags, post.address, post.dateTime, post.placeName, post.postLike.size(), post.userMessage);
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    }
                });
    }

    //가져온 정보 입력
    private void DetailPost(String name, String image, String postimage, String text, String hashTags, Address address, String datatime,String placeName, int postLike, List<String> userMessage) {
      if(postimage.contains("postvideo")){
          firebaseVideoLoader(videobinding.postVideo.getContext(),postimage,videobinding.postVideo);
          videobinding.imageProfile.setImageBitmap(getBitmapFromEncodedString(image));
          videobinding.postTextName.setText(name);
          videobinding.postTextMessage.setText(text);


          hashTagAdd(hashTags);

          videobinding.postTextLocation.setText(address.getAddressLine(0));
          videobinding.postTextDateTime.setText(datatime);
          videobinding.postLikeNum.setText("좋아요 " + postLike + "개");
          videobinding.postUserName.setText(name);
          videobinding.placeName.setText(placeName);
          PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(userMessage);
          videobinding.userMessageView.setAdapter(postUserMessageAdapter);
          setContentView(videobinding.getRoot());
      }
      else {
          firebaseImageLoader(binding.postImageMessage.getContext(), postimage, binding.postImageMessage);

          binding.imageProfile.setImageBitmap(getBitmapFromEncodedString(image));
          binding.postTextName.setText(name);
          binding.postTextMessage.setText(text);


          hashTagAdd(hashTags);

          binding.postTextLocation.setText(address.getAddressLine(0));
          binding.postTextDateTime.setText(datatime);
          binding.postLikeNum.setText("좋아요 " + postLike + "개");
          binding.postUserName.setText(name);
          binding.placeName.setText(placeName);
          PostUserMessageAdapter postUserMessageAdapter = new PostUserMessageAdapter(userMessage);
          binding.userMessageView.setAdapter(postUserMessageAdapter);
          setContentView(binding.getRoot());
      }
    }

    //해시태그 추가
    private void hashTagAdd(String hashTags) {
        ArrayList<String> hashTagArray = new ArrayList<>();

        String[] tempLists;
        tempLists = hashTags.split("#");

        for (String tempList : tempLists
        ) {
            if (tempList.isEmpty()) {
            } else
                hashTagArray.add(tempList);
        }

        HashTagAdapter hashTagAdapter = new HashTagAdapter(hashTagArray);
        binding.hashTagRecyclerView.setAdapter(hashTagAdapter);
    }



    //날짜 형식 변환
    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(date);
    }

    //이미지 디코딩
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    //파이어베이스 스토리지에서 동영상 원본 가져오기 + 표현
    private static void firebaseVideoLoader(Context context, String Path, PlayerView pv) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child(Path);
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();
                    //플레이어뷰에게 플레이어 설정
                    pv.setPlayer(player);
                    //비디오데이터 소스를 관리하는 DataSource 객체를 만들어주는 팩토리 객체 생성
                    DataSource.Factory factory= new DefaultDataSourceFactory(context,"Ex89VideoAndExoPlayer");
                    //비디오데이터를 Uri로 부터 추출해서 DataSource객체 (CD or LP판 같은 ) 생성
                    ProgressiveMediaSource mediaSource= new ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
                    //만들어진 비디오데이터 소스객체인 mediaSource를
                    //플레이어 객체에게 전당하여 준비하도록!![ 로딩하도록 !!]
                    player.prepare(mediaSource);
                    //로딩이 완료되어 준비가 되었을 때
                    //자동 실행되도록..

                } catch (Exception e) {

                }

            }
        });
    }

    //파이어베이스 스토리지에서 이미지 원본 가져오기 + 표현
    private void firebaseImageLoader(Context context, String Path, ImageView image) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child(Path);
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    Glide.with(context).load(uri).into(image);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
    }
}

