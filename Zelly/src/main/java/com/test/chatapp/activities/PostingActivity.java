//포스팅 액티비티

package com.test.chatapp.activities;

import static com.test.chatapp.activities.GoogleMapActivity.friendsList;
import static com.test.chatapp.activities.GoogleMapActivity.users;
import static com.test.chatapp.utilities.Constants.KEY_NAME;
import static com.test.chatapp.utilities.Constants.postingHashTag;
import static com.test.chatapp.utilities.getAddress.getAddress;
import static com.test.chatapp.utilities.getAddress.getDetailAddress;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.test.chatapp.adapters.ChatAdapter;
import com.test.chatapp.adapters.FriendAdapter;
import com.test.chatapp.adapters.HashTagAdapter;
import com.test.chatapp.databinding.ActivityPostingBinding;
import com.test.chatapp.httpserver.HttpClient;
import com.test.chatapp.models.User;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;
import com.google.android.exoplayer2.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class PostingActivity extends AppCompatActivity {
    private ActivityPostingBinding binding;
    private PreferenceManager preferenceManager;

    //파이어 베이스 업로드를 위한 변수
    private Uri selectedImageUri;
    private String path;
    boolean privatekey;
    private final int GET_GALLERY_IMAGE = 200;
    private final int GET_GALLERY_VIDEO = 300;
    private LocationManager locationManager;
    static public ArrayList<String> hashTags;

    private PlayerView pv;
    private SimpleExoPlayer player;

    private String placeName;

    Double gmLat; //구글 맵에서 가져온 좌표
    Double gmLon;


    public void sendPostData(Double lat, Double lon) {
        try {
            if (!placeName.isEmpty()) {
                Address address = getAddress(this, lat, lon);

                int age = 0;
                for (User everyUser : users
                ) {
                    if (everyUser.id.equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                        age = everyUser.age;
                    }
                }

                Collections.sort(hashTags, (obj1, obj2) -> obj1.compareTo(obj2));

                String request = "place/add?lat=" + lat + "&lon=" + lon + "&placename=" + placeName + "&adminarea=" + address.getAdminArea()
                        + "&locality=" + address.getLocality() + "&sublocality=" + address.getSubLocality()
                        + "&thoroughfare=" + address.getThoroughfare() + "&age=" + age
                        + "&hashtags=" + hashTags.toString().replace("[", "").replace(", ", "").replace("]", "").replace("#", "$")
                        + "&image=" + path;
                HttpClient httpClient = new HttpClient(request);
                Thread th = new Thread(httpClient);
                th.start();
                String result = null;

                long start = System.currentTimeMillis();

                while (result == null) {
                    result = httpClient.getResult();
                    long end = System.currentTimeMillis();
                    if (end - start > 5000) {
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        double latitude = location.getLatitude(); //위도
        double longitude = location.getLongitude(); //경도

        Address address = getAddress(this, latitude, longitude);
        binding.location.setText(address.getAddressLine(0));

        hashTags = new ArrayList<>();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        Intent getIntent = getIntent();
        if (getIntent.getExtras() != null) {
            gmLat = getIntent.getExtras().getDouble("Lat");
            gmLon = getIntent.getExtras().getDouble("Lon");
            address = getAddress(this, gmLat, gmLon);
            binding.location.setText(address.getAddressLine(0));
        }

        //이전화면 이동
        binding.postBack.setOnClickListener(v -> finish());

        //사진 선택
        binding.addImageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
                binding.addImageTextView.setVisibility(View.INVISIBLE);
            }
        });

        binding.addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, GET_GALLERY_VIDEO);
                binding.addVideoTextView.setVisibility(View.INVISIBLE);
            }
        });

        Switch repeat = binding.privatekey;

        Switch videoclick = binding.videokey;
        //나만보기
        repeat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    privatekey = true;
                } else {
                    privatekey = false;
                }
            }
        });
        videoclick.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.addImageTextView.setVisibility(View.GONE);
                    binding.addVideoTextView.setVisibility(View.VISIBLE);
                    binding.addImage.setVisibility(View.GONE);
                    binding.addImage.setImageURI(null);
                    binding.addVideo.setVisibility(View.VISIBLE);
                } else {
                    binding.addImageTextView.setVisibility(View.VISIBLE);
                    binding.addVideoTextView.setVisibility(View.GONE);
                    binding.addVideo.setVisibility(View.GONE);
                    binding.addVideo.hideController();
                    if (player != null) {
                        player.clearVideoSurface();
                        player.clearAuxEffectInfo();
                    }

                    binding.addImage.setVisibility(View.VISIBLE);
                }

            }
        });

        //게시 버튼
        binding.postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(PostingActivity.this, new String[]{
                            android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                    Toast.makeText(getApplicationContext(), "위치 정보 권한이 허용되지 않았습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    if (selectedImageUri == null) {
                        Toast.makeText(getApplicationContext(), "게시물을 작성해주세요!", Toast.LENGTH_SHORT).show();
                    } else {
                        loading(true);
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        String user_key = preferenceManager.getString(Constants.KEY_USER_ID);
                        String user_name = preferenceManager.getString(KEY_NAME);
                        double latitude = location.getLatitude(); //위도
                        double longitude = location.getLongitude(); //경도

                        String postcontents = binding.postText.getText().toString();
                        placeName = binding.placeName.getText().toString();

                        FirebaseFirestore database = FirebaseFirestore.getInstance();
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.KEY_USER_ID, user_key);
                        user.put(Constants.KEY_NAME, user_name);
                        user.put(Constants.KEY_PRIVATEKEY, privatekey);
                        user.put(Constants.KEY_POSTCONTENTS, postcontents);
                        user.put(Constants.KEY_PLACENAME, placeName);
                        user.put(Constants.KEY_POSTIMAGE, path);
                        user.put(Constants.KEY_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                        user.put(Constants.KEY_TIMESTAMP, new Date());
                        Collections.sort(hashTags, (obj1, obj2) -> obj1.compareTo(obj2));
                        user.put(Constants.KEY_POSTING_HASHTAGS, String.join("", hashTags));
                        user.put("message", Arrays.asList());
                        user.put("postLike", Arrays.asList());

                        if (gmLat != null && gmLon != null) {
                            user.put(Constants.KEY_POSTLAT, gmLat);
                            user.put(Constants.KEY_POSTLON, gmLon);
                        } else {
                            user.put(Constants.KEY_POSTLAT, latitude);
                            user.put(Constants.KEY_POSTLON, longitude);
                        }

                        if (placeName != null && !path.contains("postvideo")) {
                            if (gmLat != null && gmLon != null) {
                                sendPostData(gmLat, gmLon);
                            } else {
                                sendPostData(latitude, longitude);
                            }
                        }

                        database.collection(Constants.KEY_COLLECTION_POST)
                                .add(user)
                                .addOnSuccessListener(documentReference -> {
                                    FireBaseStorageUpload();
                                    loading(false);
                                    finish();
                                })
                                .addOnFailureListener(exception -> {
                                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                                    loading(false);
                                });
                    }
                }
            }
        });

        //리사이클러뷰
        hashTagAdd();
    }

    //뒤로가기 버튼(폰자체 뒤로가기 버튼) 클릭시 지정된 액티비티로 이동
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //stopPlay(); //이 액티비티에서 종료되어야 하는 활동 종료시켜주는 함수
        Intent intent = new Intent(PostingActivity.this, GoogleMapActivity.class); //지금 액티비티에서 다른 액티비티로 이동하는 인텐트 설정
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    //인텐트 플래그 설정
        startActivity(intent);  //인텐트 이동
        finish();   //현재 액티비티 종료
    }

    //해시태그 추가
    private void hashTagAdd() {
        HashTagAdapter hashTagAdapter = new HashTagAdapter(postingHashTag);
        binding.hashTagRecyclerView.setAdapter(hashTagAdapter);
    }

    //갤러리에서 사진 선택시 실행
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            try {
                selectedImageUri = data.getData(); // 전역 변수
                binding.addImage.setImageURI(selectedImageUri);
                path = "post/" + "image/" + preferenceManager.getString(Constants.KEY_USER_ID) + "_" + getImageDateTime(new Date());

            } catch (Exception e) {
                binding.addImageTextView.setVisibility(View.VISIBLE);
            }
        }
        if (requestCode == 300) {
            try {
                pv = binding.addVideo;

                selectedImageUri = data.getData(); // 전역 변수
                player = new SimpleExoPlayer.Builder(this.getApplicationContext()).build();
                //플레이어뷰에게 플레이어 설정
                pv.setPlayer(player);
                //비디오데이터 소스를 관리하는 DataSource 객체를 만들어주는 팩토리 객체 생성
                DataSource.Factory factory = new DefaultDataSourceFactory(this, "Ex89VideoAndExoPlayer");
                //비디오데이터를 Uri로 부터 추출해서 DataSource객체 (CD or LP판 같은 ) 생성
                ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(selectedImageUri))));

                //만들어진 비디오데이터 소스객체인 mediaSource를
                //플레이어 객체에게 전당하여 준비하도록!![ 로딩하도록 !!]
                player.prepare(mediaSource);


                path = "postvideo/" + preferenceManager.getString(Constants.KEY_USER_ID) + "_" + getImageDateTime(new Date());

            } catch (Exception e) {
                binding.addImageTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    String getRealPathFromUri(Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    //사진을 파이어베이스 스토리지에 업로드
    private void FireBaseStorageUpload() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child(path);

        UploadTask uploadTask = riversRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            }
        });
    }

    //이미지 올린 시간
    private String getImageDateTime(Date date) {
        return new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.getDefault()).format(date);
    }

    //로딩 프로그레스바 띄우기
    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


}