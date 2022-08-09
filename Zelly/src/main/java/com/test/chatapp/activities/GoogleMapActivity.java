//구글맵 액티비티(메인 화면)
package com.test.chatapp.activities;

import static com.test.chatapp.utilities.Constants.postingHashTag;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.test.chatapp.R;
import com.test.chatapp.adapters.ChatAdapter;
import com.test.chatapp.adapters.FriendAdapter;
import com.test.chatapp.adapters.HashTagAdapter;
import com.test.chatapp.databinding.ActivityGoogleMapBinding;
import com.test.chatapp.databinding.CircleMarkerBinding;
import com.test.chatapp.databinding.ItemContainerFriendBinding;
import com.test.chatapp.models.MarkerItem;
import com.test.chatapp.models.Post;
import com.test.chatapp.models.User;
import com.test.chatapp.utilities.Constants;
import com.test.chatapp.utilities.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GoogleMapActivity extends AppCompatActivity {
    public Context context = this;

    //로그캣 사용 설정
    private static final String TAG = "GoogleMapActivity";
    private ActivityGoogleMapBinding binding;
    private ItemContainerFriendBinding friendBinding;
    private CircleMarkerBinding circleMarkerBinding;

    private Geocoder geocoder;
    private FirebaseFirestore database;
    private FirebaseStorage storage;
    private int REQUEST_CODE = 100;
    private FriendAdapter friendAdapter;
    private HashTagAdapter hashTagAdapter;

    public static ArrayList<User> friends;
    public static ArrayList<User> friendsList;
    public static ArrayList<User> selectedFriends;

    ImageView imageView;
    PreferenceManager preferenceManager;

    //객체 선언
    SupportMapFragment mapFragment;
    GoogleMap map;
    StaggeredGridLayoutManager staggeredGridLayoutManager;

    ClusterManager<MarkerItem> clusterManager;
    public static ArrayList<MarkerItem> firebaseMarker; //파이어베이스
    public static ArrayList<User> users;
    public static ArrayList<String> selectedHashTags;
    static public ArrayList<User> bottomSheetFriends;
    private FriendAdapter BottomSheetFriendAdapter;
    ArrayList<User> filteredUsers;
    ArrayList<MarkerItem> markerItems; //새로 찍는 거
    ArrayList<MarkerItem> tempMarkers;
    ArrayList<Integer> ageRange;

    //intent 값 전달
    int intentValue;
    int GOOGLE_MAP_SELECT_LOCATION = 100;
    int GOOGLE_MAP_SELECT_LOCATION_CALENDAR = 200;
    public static int GOOGLEMAP_PLACE = 300;
    public static final int GOOGLEMAP_FRIEND_LIST = 1;
    public static final int BOTTOMSHEET_FRIEND_LIST = 2;
    LatLng clickMapLatLng;

    private LocationManager locationManager;

    int selectFilter;

    int zellyClick;

    int markerDeleter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoogleMapBinding.inflate(getLayoutInflater());
        circleMarkerBinding = CircleMarkerBinding.inflate(getLayoutInflater());
        friendBinding = ItemContainerFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //초기화
        init();

        //지도 프래그먼트 설정
        googlemapload();

        //게시물 가져오기
        listenFriendsList();
        getUsersInfo();

        //버튼
        setListener();

        Intent getIntent = getIntent();
        if (getIntent.getExtras() != null) {
            if (getIntent.getExtras().getInt("selectLocation") == GOOGLE_MAP_SELECT_LOCATION || getIntent.getExtras().getInt("selectLocation") == GOOGLE_MAP_SELECT_LOCATION_CALENDAR) {
                intentValue = getIntent().getExtras().getInt("selectLocation");
                Toast.makeText(getApplicationContext(), "약속 장소를 지도 상에서 클릭하세요.", Toast.LENGTH_SHORT).show();
                binding.selectId.setVisibility(View.GONE);
                chatBotMakeSchedule();
            } else if (getIntent.getExtras().getInt("selectLocation") == GOOGLEMAP_PLACE) {
                intentValue = GOOGLEMAP_PLACE;
                chatBotMakeSchedule();
            }
        }
    }

    //초기화
    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseMarker = new ArrayList<MarkerItem>();
        markerItems = new ArrayList<MarkerItem>();
        imageView = circleMarkerBinding.circleImageProfile;
        friends = new ArrayList<>();
        friendsList = new ArrayList<>();
        selectedHashTags = new ArrayList<String>();
        ageRange = new ArrayList<>();
        bottomSheetFriends = new ArrayList<>();

        friendAdapter = new FriendAdapter(
                friends
        );

        BottomSheetFriendAdapter = new FriendAdapter(
                bottomSheetFriends
        );

        binding.friendRecyclerView.setAdapter(friendAdapter);
        binding.friendRecyclerView.bringToFront();
        binding.selectId.bringToFront();
        binding.googleMapFilter.bringToFront();

        zellyClick = 0;
        tempMarkers = new ArrayList<>();
        users = new ArrayList<>();
        filteredUsers = new ArrayList<>();
        selectedFriends = new ArrayList<>();

        selectFilter = 0;
        markerDeleter = 0;

        Collections.sort(postingHashTag, (obj1, obj2) -> obj1.compareTo(obj2));

        StaggeredGridLayoutManager staggeredGridLayoutManager
                = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);


        hashTagAdapter = new HashTagAdapter(
                postingHashTag
        );
    }


    //구글 검색하고 텍스트 변경
    private void getGoogleSearch() {
        try {
            if (!getIntent().getStringExtra("data").isEmpty()) {
                binding.searchText.setText(getIntent().getStringExtra("data"));
                binding.searchButton.performClick();
            }
        } catch (Exception exception) {
        }
    }

    //구글맵 로드
    private void googlemapload() {
        Context context = this;
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d(TAG, "onMapReady: ");
                map = googleMap;
                map.setMyLocationEnabled(true); //내위치 버튼
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.getUiSettings().setMapToolbarEnabled(false);

                geocoder = new Geocoder(GoogleMapActivity.this);

                UiSettings uiSettings = map.getUiSettings();

                binding.searchText.bringToFront();
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng point) {
                        binding.postButton.setVisibility(View.INVISIBLE);
                        binding.scheduleButton.setVisibility(View.INVISIBLE);
                        binding.unfoldZellyLayout.setVisibility(View.GONE);
                        zellyClick = 0;
                        binding.postButton.setVisibility(View.VISIBLE);
                        binding.scheduleButton.setVisibility(View.VISIBLE);
                        binding.selectLocationPostingButton.setVisibility(View.GONE);
                        binding.selectLocationPostingCancle.setVisibility(View.VISIBLE);

                        map.clear();
                        setUpClusterer();

                        MarkerOptions mOptions = new MarkerOptions();

                        imageView.setImageBitmap(getBitmapFromEncodedString(preferenceManager.getString(Constants.KEY_IMAGE)));

                        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();

                        bitmap = getRoundedCornerBitmap(bitmap, 100);
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
                        MarkerItem markerItem = new MarkerItem(null, point.latitude, point.longitude, preferenceManager.getString(Constants.KEY_USER_ID),
                                preferenceManager.getString(Constants.KEY_NAME), preferenceManager.getString(Constants.KEY_IMAGE), null, true, icon, null);
                        markerItems.add(markerItem);
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));

                        for (int i = 0; i < markerItems.size(); i++) {
                            if (markerItems.size() > 1 && markerItems.get(i).getPinCheck() == true && markerItems.get(markerItems.size() - 1).getPinCheck() == true) {
                                markerItems.remove(markerItems.size() - 2);
                            }
                            mOptions.position(new LatLng(markerItems.get(i).getLat(), markerItems.get(i).getLon()));  // LatLng: 위도 경도 쌍을 나타냄
                            map.addMarker(mOptions);    // 마커(핀) 추가

                            if (intentValue != GOOGLE_MAP_SELECT_LOCATION && intentValue != GOOGLE_MAP_SELECT_LOCATION_CALENDAR) {
                                binding.selectLocationPostingButton.setVisibility(View.GONE);
                                map.addMarker(mOptions);    // 마커(핀) 추가
                            }
                        }

                        if (intentValue == GOOGLE_MAP_SELECT_LOCATION || intentValue == GOOGLE_MAP_SELECT_LOCATION_CALENDAR) {
                            binding.appointButton.setVisibility(View.VISIBLE);
                            binding.postButton.setVisibility(View.GONE);
                            binding.scheduleButton.setVisibility(View.GONE);
                            binding.selectLocationPostingButton.setVisibility(View.GONE);
                            binding.selectLocationPostingCancle.setVisibility(View.GONE);
                            clickMapLatLng = point;
                        }
                    }
                });

                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "위치 정보 권한이 허용되지 않았습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (location == null) {
                        Toast.makeText(getApplicationContext(), "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        LatLng Pos = new LatLng(location.getLatitude(), location.getLongitude());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(Pos, 15));
                    }
                }

                clusterManager = new ClusterManager<MarkerItem>(context, map);
                map.setOnCameraIdleListener(clusterManager);
                map.setOnMarkerClickListener(clusterManager);

                listenFriendsList();
                getGoogleSearch();
            }
        });
        MapsInitializer.initialize(this);
    }

    //버튼 안 보이게 하기
    private void chatBotMakeSchedule() {
        binding.postButton.setVisibility(View.GONE);
        binding.refreshButton.setVisibility(View.GONE);
        binding.friendRecyclerView.setVisibility(View.GONE);
        binding.selectId.setVisibility(View.GONE);
        binding.googleMapFilter.setVisibility(View.GONE);
        binding.unfoldZelly.setVisibility(View.GONE);
        binding.feedButton.setVisibility(View.GONE);
        binding.chatButton.setVisibility(View.GONE);
        if (intentValue == GOOGLEMAP_PLACE) {
            binding.postButton.setVisibility(View.GONE);
            binding.scheduleButton.setVisibility(View.GONE);
            binding.selectLocationPostingButton.setVisibility(View.VISIBLE);
            binding.selectLocationPostingCancle.setVisibility(View.GONE);
            binding.friendRecyclerView.setVisibility(View.VISIBLE);
            binding.selectId.setVisibility(View.VISIBLE);
            binding.googleMapFilter.setVisibility(View.VISIBLE);
        }
    }

    //마커 넣음
    private void setUpClusterer() {
        try {
            map.clear();
            clusterManager = new ClusterManager<MarkerItem>(this, map);

            clusterManager.setRenderer(new OwnIconRendered(getApplicationContext(), map, clusterManager));
            if (tempMarkers.isEmpty() && ageRange.isEmpty() && selectedHashTags.isEmpty()) {
                clusterManager.addItems(firebaseMarker);
            } else {
                clusterManager.addItems(tempMarkers);
            }
            if (intentValue == GOOGLE_MAP_SELECT_LOCATION || intentValue == GOOGLE_MAP_SELECT_LOCATION_CALENDAR) {
                binding.selectId.setVisibility(View.GONE);
            } else {
                binding.selectId.setVisibility(View.VISIBLE);
            }

            map.setOnCameraIdleListener(clusterManager);
            map.setOnMarkerClickListener(clusterManager);
            map.setOnInfoWindowClickListener(clusterManager);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    //클릭 이벤트 처리
    private void setListener() {
        binding.chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapActivity.this, MainActivity.class);
                setUpClusterer();
                binding.selectLocationPostingButton.setVisibility(View.INVISIBLE);
                startActivity(intent);
            }
        });

        binding.feedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapActivity.this, ReceivePostActivity.class);
                setUpClusterer();
                binding.selectLocationPostingButton.setVisibility(View.INVISIBLE);
                startActivity(intent);
            }
        });

        binding.feedAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapActivity.this, PostingActivity.class);
                setUpClusterer();
                binding.selectLocationPostingButton.setVisibility(View.INVISIBLE);
                startActivity(intent);
                binding.refreshButton.performClick();
            }
        });

        binding.currentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "위치 정보 권한이 허용되지 않았습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (location == null) {
                        Toast.makeText(getApplicationContext(), "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        LatLng Pos = new LatLng(location.getLatitude(), location.getLongitude());
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(Pos, 15));
                    }
                }
            }
        });

        //검색 버튼 클릭 이벤트
        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchPlace();
            }
        });

        binding.markerDeleter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (markerDeleter == 0) {
                    binding.markerDeleter.setBackgroundResource(R.drawable.shadow_round_color);
                    binding.markerDeleterText.setTextColor(Color.parseColor("#FFFFFF"));
                    clusterManager.clearItems();
                    map.clear();
                    markerDeleter = 1;
                } else {
                    binding.markerDeleter.setBackgroundResource(R.drawable.shadow_round);
                    binding.markerDeleterText.setTextColor(Color.parseColor("#000000"));
                    markerDeleter = 0;
                    setUpClusterer();

                    binding.postButton.setVisibility(View.INVISIBLE);
                    binding.scheduleButton.setVisibility(View.INVISIBLE);
                    binding.selectLocationPostingButton.setVisibility(View.GONE);
                    binding.selectLocationPostingCancle.setVisibility(View.GONE);
                }
            }
        });

        //우측 (+) 버튼 클릭 이벤트
        binding.selectLocationPostingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.postButton.setVisibility(View.VISIBLE);
                binding.scheduleButton.setVisibility(View.VISIBLE);
                binding.selectLocationPostingButton.setVisibility(View.GONE);
                binding.selectLocationPostingCancle.setVisibility(View.VISIBLE);
            }
        });

        //... 버튼 클릭 이벤트
        binding.selectLocationPostingCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.postButton.setVisibility(View.INVISIBLE);
                binding.scheduleButton.setVisibility(View.INVISIBLE);
                binding.selectLocationPostingButton.setVisibility(View.VISIBLE);
                binding.selectLocationPostingCancle.setVisibility(View.GONE);
            }
        });

        //새로고침 버튼 클릭 이벤트
        binding.refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tempMarkers.clear();
                ageRange.clear();
                map.clear();

                listenFriendsList();
                setUpClusterer();
                binding.selectLocationPostingButton.setVisibility(View.INVISIBLE);

                binding.friendRecyclerView.removeAllViewsInLayout();
                binding.friendRecyclerView.setAdapter(friendAdapter);

                selectedFriends.clear();
                selectedHashTags.clear();

                binding.markerDeleter.setBackgroundResource(R.drawable.shadow_round);
                binding.markerDeleterText.setTextColor(Color.parseColor("#000000"));
                setUpClusterer();
                markerDeleter = 0;
            }
        });

        //포스팅버튼 클릭 이벤트
        binding.postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.postButton.setVisibility(View.INVISIBLE);
                binding.scheduleButton.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(GoogleMapActivity.this, PostingActivity.class);
                MarkerItem markerItem = markerItems.get(markerItems.size() - 1);
                intent.putExtra("Lat", markerItem.getLat());
                intent.putExtra("Lon", markerItem.getLon());
                map.clear();
                setUpClusterer();
                binding.selectLocationPostingButton.setVisibility(View.INVISIBLE);
                startActivity(intent);
            }
        });

        //스케쥴 추가 버튼 클릭 이벤트
        binding.scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.postButton.setVisibility(View.INVISIBLE);
                binding.scheduleButton.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(GoogleMapActivity.this, CalendarActivity.class);
                MarkerItem markerItem = markerItems.get(markerItems.size() - 1);
                intent.putExtra("Lat", markerItem.getLat());
                intent.putExtra("Lon", markerItem.getLon());
                setUpClusterer();
                binding.selectLocationPostingButton.setVisibility(View.INVISIBLE);
                startActivity(intent);
            }
        });

        //캘린더에서 장소 추가하기 위해 클릭하는 이벤트 처리
        binding.appointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (intentValue == GOOGLE_MAP_SELECT_LOCATION)
                    ChatAdapter.latLng = clickMapLatLng;
                else if (intentValue == GOOGLE_MAP_SELECT_LOCATION_CALENDAR) {
                    Intent intent = new Intent();
                    CalendarAddActivity.latLng = clickMapLatLng;
                    setResult(2000, intent);
                }
                finish();
            }
        });

        //친구 필터링 클릭 이벤트
        binding.selectId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedIdAddMarker();
                binding.markerDeleter.setBackgroundResource(R.drawable.shadow_round);
                binding.markerDeleterText.setTextColor(Color.parseColor("#000000"));
                markerDeleter = 0;
            }
        });

        binding.unfoldZelly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (zellyClick == 0) {
                    binding.unfoldZellyLayout.setVisibility(View.VISIBLE);
                    zellyClick = 1;
                } else {
                    binding.unfoldZellyLayout.setVisibility(View.GONE);
                    zellyClick = 0;
                }
            }
        });

        binding.recommendPlace.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RecommandPlaceActivity.class)));

        //필터 클릭 이벤트
        binding.googleMapFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        context, R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(
                        R.layout.layout_bottom_googlemap_filter, (LinearLayout) findViewById(R.id.bottomGooglemapFilter)
                );

                RecyclerView friendsRecyclerView = bottomSheetView.findViewById(R.id.friendsRecyclerView);
                friendsRecyclerView.setAdapter(BottomSheetFriendAdapter);

                boolean[] hashtagCheck = new boolean[10];
                LinearLayout[] hashtaglayout = new LinearLayout[10];
                TextView[] hashtagtext = new TextView[10];
                ImageView[] hashtagimage = new ImageView[10];

                hashtaglayout[0] = bottomSheetView.findViewById(R.id.hashTag1);
                hashtaglayout[1] = bottomSheetView.findViewById(R.id.hashTag2);
                hashtaglayout[2] = bottomSheetView.findViewById(R.id.hashTag3);
                hashtaglayout[3] = bottomSheetView.findViewById(R.id.hashTag4);
                hashtaglayout[4] = bottomSheetView.findViewById(R.id.hashTag5);
                hashtaglayout[5] = bottomSheetView.findViewById(R.id.hashTag6);
                hashtaglayout[6] = bottomSheetView.findViewById(R.id.hashTag7);
                hashtaglayout[7] = bottomSheetView.findViewById(R.id.hashTag8);
                hashtaglayout[8] = bottomSheetView.findViewById(R.id.hashTag9);
                hashtaglayout[9] = bottomSheetView.findViewById(R.id.hashTag10);

                hashtagtext[0] = bottomSheetView.findViewById(R.id.hashTagText1);
                hashtagtext[1] = bottomSheetView.findViewById(R.id.hashTagText2);
                hashtagtext[2] = bottomSheetView.findViewById(R.id.hashTagText3);
                hashtagtext[3] = bottomSheetView.findViewById(R.id.hashTagText4);
                hashtagtext[4] = bottomSheetView.findViewById(R.id.hashTagText5);
                hashtagtext[5] = bottomSheetView.findViewById(R.id.hashTagText6);
                hashtagtext[6] = bottomSheetView.findViewById(R.id.hashTagText7);
                hashtagtext[7] = bottomSheetView.findViewById(R.id.hashTagText8);
                hashtagtext[8] = bottomSheetView.findViewById(R.id.hashTagText9);
                hashtagtext[9] = bottomSheetView.findViewById(R.id.hashTagText10);

                hashtagimage[0] = bottomSheetView.findViewById(R.id.hashTagImage1);
                hashtagimage[1] = bottomSheetView.findViewById(R.id.hashTagImage2);
                hashtagimage[2] = bottomSheetView.findViewById(R.id.hashTagImage3);
                hashtagimage[3] = bottomSheetView.findViewById(R.id.hashTagImage4);
                hashtagimage[4] = bottomSheetView.findViewById(R.id.hashTagImage5);
                hashtagimage[5] = bottomSheetView.findViewById(R.id.hashTagImage6);
                hashtagimage[6] = bottomSheetView.findViewById(R.id.hashTagImage7);
                hashtagimage[7] = bottomSheetView.findViewById(R.id.hashTagImage8);
                hashtagimage[8] = bottomSheetView.findViewById(R.id.hashTagImage9);
                hashtagimage[9] = bottomSheetView.findViewById(R.id.hashTagImage10);

                for (int i = 0; i < 10; i++) {
                    hashtagimage[i].setColorFilter(Color.parseColor("#e9e9e9"));
                    hashtagtext[i].setTextColor(Color.parseColor("#e9e9e9"));
                }

                for (int i = 0; i < 10; i++) {
                    final int idx = i;
                    hashtaglayout[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!hashtagCheck[idx]) {
                                hashtagCheck[idx] = true;
                                hashtagtext[idx].setTextColor(Color.parseColor("#288CFF"));
                                hashtagimage[idx].setColorFilter(Color.parseColor("#288CFF"));
                            } else {
                                hashtagCheck[idx] = false;
                                hashtagtext[idx].setTextColor(Color.parseColor("#e9e9e9"));
                                hashtagimage[idx].setColorFilter(Color.parseColor("#e9e9e9"));
                            }

                        }
                    });
                }


                Button[] ageButtons = new Button[5];
                boolean[] ageBool = new boolean[5];

                ageButtons[0] = bottomSheetView.findViewById(R.id.age10);
                ageButtons[1] = bottomSheetView.findViewById(R.id.age20);
                ageButtons[2] = bottomSheetView.findViewById(R.id.age30);
                ageButtons[3] = bottomSheetView.findViewById(R.id.age40);
                ageButtons[4] = bottomSheetView.findViewById(R.id.age50);

                for (int i = 0; i < 5; i++) {
                    final int idx = i;
                    ageButtons[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!ageBool[idx]) {
                                ageButtons[idx].setTextColor(Color.WHITE);
                                ageButtons[idx].setBackgroundResource(R.drawable.shadow_round_color);
                                ageBool[idx] = true;
                            } else {
                                ageButtons[idx].setTextColor(Color.parseColor("#e9e9e9"));
                                ageButtons[idx].setBackgroundResource(R.drawable.shadow_round);
                                ageBool[idx] = false;
                            }
                        }
                    });
                }


                bottomSheetView.findViewById(R.id.changebutton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ageRange.clear();
                        selectedHashTags.clear();

                        for (int i = 0; i < 5; i++) {
                            if (ageBool[i]) {
                                ageRange.add((i + 1) * 10);
                                // 50대
                                if (i == 4) {
                                    ageRange.add(60);
                                    ageRange.add(70);
                                    ageRange.add(80);
                                    ageRange.add(90);
                                }
                            }

                        }

                        for (int i = 0; i < 10; i++) {
                            if (hashtagCheck[i])
                                selectedHashTags.add(hashtagtext[i].getText().toString());
                        }

                        bottomSheetDialog.dismiss();
                        selectedFilterAddMarker();

                        selectedFriends.clear();

                        binding.friendRecyclerView.removeAllViewsInLayout();
                        binding.friendRecyclerView.setAdapter(friendAdapter);

                        binding.markerDeleter.setBackgroundResource(R.drawable.shadow_round);
                        binding.markerDeleterText.setTextColor(Color.parseColor("#000000"));
                        markerDeleter = 0;
                    }
                });

                bottomSheetView.findViewById(R.id.canclebutton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bottomSheetDialog.dismiss();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();
            }
        });

    }

    private void listenFriendsList() {
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_FRIEND)
                .get()
                .addOnCompleteListener(friendsListListener);

    }

    //파이어베이스에서 친구목록 가져오기
    private final OnCompleteListener<QuerySnapshot> friendsListListener = task -> {
        try {
            friendsList.clear();
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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
                            ArrayList<User> friendUsers = new ArrayList<>();
                            for (QueryDocumentSnapshot queryDocumentSnapshot : querySnapshot1) {
                                for (User user : users) {
                                    if (user.id.equals(queryDocumentSnapshot.getId())) {
                                        User us = new User();
                                        us.id = queryDocumentSnapshot.getId();
                                        us.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                        us.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                                        us.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                        us.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                        us.age = Integer.parseInt(queryDocumentSnapshot.getString(Constants.KEY_AGE));
                                        us.state = user.state;
                                        us.receiveId = preferenceManager.getString(Constants.KEY_USER_ID);
                                        if (us.state.equals("true")) {
                                            friendUsers.add(us);
                                        }
                                    }
                                }
                            }
                            User us = new User();

                            us.id = preferenceManager.getString(Constants.KEY_USER_ID);
                            us.name = preferenceManager.getString(Constants.KEY_NAME);
                            us.email = preferenceManager.getString(Constants.KEY_EMAIL);
                            us.image = preferenceManager.getString(Constants.KEY_IMAGE);
                            us.token = preferenceManager.getString(Constants.KEY_FCM_TOKEN);

                            friendUsers.add(us);
                            getFriendsList(friendUsers);
                        }
                    });
        } catch (Exception e) {
            System.out.println(e);
        }
    };

    private void getFriendsList(ArrayList<User> tempFriendsList) {
        friendsList = tempFriendsList;
        listenPost();
    }

    //파이어베이스에서 데이터 가져오기
    private void listenPost() {
        database.collection("post")
                .get()
                .addOnCompleteListener(completeListenerm);
    }

    private final OnCompleteListener<QuerySnapshot> completeListenerm = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            clusterManager.clearItems();
            firebaseMarker.clear();
            try {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Post post = new Post();
                    post.privatekey = document.getBoolean(Constants.KEY_PRIVATEKEY);
                    if (post.privatekey) {
                        if (document.getString(Constants.KEY_USER_ID).equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                            post.postId = document.getId();
                            post.userid = document.getString(Constants.KEY_USER_ID);
                            post.name = document.getString(Constants.KEY_NAME);
                            post.image = document.getString(Constants.KEY_IMAGE);
                            post.postcontents = document.getString(Constants.KEY_POSTCONTENTS);
                            post.lat = document.getDouble(Constants.KEY_POSTLAT);
                            post.lon = document.getDouble(Constants.KEY_POSTLON);
                            post.hashTags = document.getString(Constants.KEY_POSTING_HASHTAGS);
                            post.dateObject = document.getDate(Constants.KEY_TIMESTAMP);

                            imageView.setImageBitmap(getBitmapFromEncodedString(post.image));

                            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                            Bitmap bitmap = drawable.getBitmap();

                            bitmap = getRoundedCornerBitmap(bitmap, 100);
                            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

                            int mySign = 0;

                            for (User us : friendsList
                            ) {
                                if (mySign == 0) {
                                    if (post.userid.equals(us.id)) {
                                        firebaseMarker.add(new MarkerItem(post.postId, post.lat, post.lon, post.userid, post.name, post.image, post.postcontents, false, icon, post.hashTags));

                                        User user = new User();
                                        user.id = post.userid;
                                        user.name = post.name;
                                        user.image = post.image;
                                        user.position = GOOGLEMAP_FRIEND_LIST;
                                        user.timestamp = post.dateObject;

                                        User user2 = new User();
                                        user2.id = post.userid;
                                        user2.name = post.name;
                                        user2.image = post.image;
                                        user2.position = BOTTOMSHEET_FRIEND_LIST;
                                        user2.timestamp = post.dateObject;

                                        int sign = 0;
                                        for (User friend : friends
                                        ) {
                                            if (friend.id.equals(user.id)) {
                                                sign = 1;
                                                break;
                                            }
                                        }

                                        if (sign == 1)
                                            continue;
                                        friends.add(user);
                                        bottomSheetFriends.add(user2);

                                        mySign = 1;
                                    } else {
                                        mySign = 0;
                                        continue;
                                    }
                                }
                            }
                        }
                        continue;
                    }

                    if (!post.privatekey) {
                        post.postId = document.getId();
                        post.userid = document.getString(Constants.KEY_USER_ID);
                        post.name = document.getString(Constants.KEY_NAME);
                        post.image = document.getString(Constants.KEY_IMAGE);
                        post.postcontents = document.getString(Constants.KEY_POSTCONTENTS);
                        post.lat = document.getDouble(Constants.KEY_POSTLAT);
                        post.lon = document.getDouble(Constants.KEY_POSTLON);
                        post.hashTags = document.getString(Constants.KEY_POSTING_HASHTAGS);
                        post.dateObject = document.getDate(Constants.KEY_TIMESTAMP);

                        imageView.setImageBitmap(getBitmapFromEncodedString(post.image));

                        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                        Bitmap bitmap = drawable.getBitmap();

                        bitmap = getRoundedCornerBitmap(bitmap, 100);
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);

                        int mySign = 0;

                        for (User us : friendsList
                        ) {
                            if (mySign == 0) {
                                if (post.userid.equals(us.id)) {
                                    firebaseMarker.add(new MarkerItem(post.postId, post.lat, post.lon, post.userid, post.name, post.image, post.postcontents, false, icon, post.hashTags));

                                    User user = new User();
                                    user.id = post.userid;
                                    user.name = post.name;
                                    user.image = post.image;
                                    user.timestamp = post.dateObject;
                                    user.position = GOOGLEMAP_FRIEND_LIST;

                                    User user2 = new User();
                                    user2.id = post.userid;
                                    user2.name = post.name;
                                    user2.image = post.image;
                                    user2.position = BOTTOMSHEET_FRIEND_LIST;
                                    user2.timestamp = post.dateObject;

                                    int sign = 0;
                                    for (User friend : friends
                                    ) {
                                        if (friend.id.equals(user.id)) {
                                            sign = 1;
                                            break;
                                        }
                                    }

                                    if (sign == 1)
                                        continue;
                                    friends.add(user);
                                    bottomSheetFriends.add(user2);

                                    mySign = 1;
                                } else {
                                    mySign = 0;
                                    continue;
                                }
                            }
                        }
                    }

                }
                clusterManager.addItems(firebaseMarker);

                setUpClusterer();

                Collections.sort(friends, (obj1, obj2) -> obj2.timestamp.compareTo(obj1.timestamp));

                if (friends.size() == 0)
                    friendAdapter.notifyDataSetChanged();
                else
                    friendAdapter.notifyItemRangeInserted(friends.size(), friends.size());

                getFriendsListAndMarkers(friends, firebaseMarker);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    };

    //파이어베이스에서 데이터 가져오기
    private void getUsersInfo() {
        database.collection("users")
                .get()
                .addOnCompleteListener(completeListener);
    }

    private final OnCompleteListener<QuerySnapshot> completeListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {

            try {
                for (QueryDocumentSnapshot document : task.getResult()) {

                    User user = new User();
                    user.id = document.getId();
                    user.email = document.getString(Constants.KEY_EMAIL);
                    user.name = document.getString(Constants.KEY_NAME);
                    user.age = Integer.parseInt(document.getString(Constants.KEY_AGE));

                    users.add(user);
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    };

    //task에서 돌린 데이터를 GoogleMapActivity 변수에 대입하기
    static public void getFriendsListAndMarkers(ArrayList<User> tempFriends, ArrayList<MarkerItem> tempMarkers) {
        friends = tempFriends;
        firebaseMarker = tempMarkers;
    }

    //선택한 id 마커를 지도에 표시하기
    public void selectedIdAddMarker() {
        if (selectedFriends != null) {
            map.clear();
            clusterManager.clearItems();
            tempMarkers.clear();

            for (MarkerItem tempMarker : firebaseMarker) {
                for (User selectedFriend : selectedFriends) {
                    if (selectedFriend.id.equals(tempMarker.getUserId())) {
                        imageView.setImageBitmap(getBitmapFromEncodedString(tempMarker.getImage()));

                        tempMarkers.add(tempMarker);
                    }
                }
            }

            clusterManager.addItems(tempMarkers);
            setUpClusterer();
        } else
            Toast.makeText(getApplicationContext(), "친구를 선택하고 클릭해주세요.", Toast.LENGTH_SHORT).show();
    }

    public void selectedFilterAddMarker() {
        if (selectedHashTags != null || selectedFriends != null || ageRange != null) {
            map.clear();
            clusterManager.clearItems();
            tempMarkers.clear();

            Collections.sort(selectedHashTags, (obj1, obj2) -> obj1.compareTo(obj2));
            String tempHashTag = String.join("", selectedHashTags);

            if (selectedFriends.isEmpty()) {
                if (ageRange.isEmpty()) {
                    for (MarkerItem tempMarker : firebaseMarker) {
                        if (tempMarker.getHashTags().replace("#", "").contains(tempHashTag.replace("#", ""))) {
                            imageView.setImageBitmap(getBitmapFromEncodedString(tempMarker.getImage()));

                            tempMarkers.add(tempMarker);
                        }
                    }
                } else {
                    for (int age : ageRange) {
                        for (User user : users) {
                            for (MarkerItem tempMarker : firebaseMarker) {
                                if (tempMarker.getUserId().equals(user.id)) {
                                    if (age <= user.age && user.age <= age + 9)
                                        if (tempMarker.getHashTags().replace("#", "").contains(tempHashTag.replace("#", ""))) {
                                            imageView.setImageBitmap(getBitmapFromEncodedString(tempMarker.getImage()));

                                            tempMarkers.add(tempMarker);
                                        }
                                }
                            }
                        }
                    }
                }
            } else {
                if (ageRange.isEmpty()) {
                    for (MarkerItem tempMarker : firebaseMarker) {
                        for (User selectedFriend : selectedFriends) {
                            if (selectedFriend.id.equals(tempMarker.getUserId())) {
                                if (tempMarker.getHashTags().replace("#", "").contains(tempHashTag.replace("#", ""))) {
                                    imageView.setImageBitmap(getBitmapFromEncodedString(tempMarker.getImage()));

                                    tempMarkers.add(tempMarker);
                                }
                            }
                        }
                    }
                } else {
                    for (int age : ageRange) {
                        for (User user : users) {
                            for (MarkerItem tempMarker : firebaseMarker) {
                                for (User selectedFriend : selectedFriends) {
                                    if (tempMarker.getUserId().equals(user.id)) {
                                        if (age <= user.age && user.age <= age + 9) {
                                            if (selectedFriend.id.equals(tempMarker.getUserId())) {
                                                if (tempMarker.getHashTags().replace("#", "").contains(tempHashTag.replace("#", ""))) {
                                                    imageView.setImageBitmap(getBitmapFromEncodedString(tempMarker.getImage()));

                                                    tempMarkers.add(tempMarker);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            clusterManager.addItems(tempMarkers);
            setUpClusterer();
        } else
            Toast.makeText(getApplicationContext(), "친구를 선택하고 클릭해주세요.", Toast.LENGTH_SHORT).show();
    }

    private void searchPlace() {
        String str = binding.searchText.getText().toString();
        List<Address> addressList;
        try {
            // editText에 입력한 텍스트(주소, 지역, 장소 등)을 지오 코딩을 이용
            addressList = geocoder.getFromLocationName(
                    str, // 주소
                    5); // 최대 검색 결과 개수

            if (addressList.size() == 0) {
                Toast.makeText(getApplicationContext(), "존재하지 않는 장소입니다,", Toast.LENGTH_SHORT).show();
                return;
            }

            // 콤마를 기준으로 split
            String[] splitStr = addressList.get(0).toString().split(",");
            String address = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소

            String latitude = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // 위도
            String longitude = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // 경도

            // 좌표(위도, 경도) 생성
            LatLng point = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
            // 마커 생성
            MarkerOptions mOptions2 = new MarkerOptions();
            mOptions2.title(str);
            mOptions2.snippet(address);
            mOptions2.position(point);
            // 마커 추가
            markerItems.add(new MarkerItem(null, point.latitude, point.longitude, preferenceManager.getString(Constants.KEY_USER_ID),
                    preferenceManager.getString(Constants.KEY_NAME), preferenceManager.getString(Constants.KEY_IMAGE), null, true, null, null));

            map.addMarker(mOptions2);
            // 해당 좌표로 화면 줌
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));

            binding.postButton.setVisibility(View.VISIBLE);
            binding.scheduleButton.setVisibility(View.VISIBLE);
            binding.selectLocationPostingButton.setVisibility(View.GONE);
            binding.selectLocationPostingCancle.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //스트링 비트맵으로 인코딩
    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    /* 비트맵 모서리 둥글게*/
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int px) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = px;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    class OwnIconRendered extends DefaultClusterRenderer<MarkerItem> {
        String postText;
        String postId;

        public OwnIconRendered(Context context, GoogleMap map,
                               ClusterManager<MarkerItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MarkerItem item, MarkerOptions markerOptions) {
            markerOptions.icon(item.getIcon());
            markerOptions.snippet(item.getSnippet());
            markerOptions.title(item.getTitle());
            super.onBeforeClusterItemRendered(item, markerOptions);

            clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerItem>() {
                @Override
                public boolean onClusterItemClick(MarkerItem item) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(item.getPosition(), 15));
                    String[] data = item.getSnippet().split("\n/split");
                    postText = data[0];
                    postId = data[data.length - 1];
                    return false;
                }
            });

            map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    Intent intent = new Intent(getApplicationContext(), DetailPostActivity.class);
                    intent.putExtra("data", postId);
                    startActivity(intent);
                }
            });
        }
    }
}