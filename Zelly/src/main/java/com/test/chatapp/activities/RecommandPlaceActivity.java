package com.test.chatapp.activities;

import static com.test.chatapp.utilities.getAddress.getAddress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.test.chatapp.R;
import com.test.chatapp.adapters.FilterOptionAdminAreaAdapter;
import com.test.chatapp.adapters.FilterOptionHashTagAdapter;
import com.test.chatapp.adapters.HashTagAdapter;
import com.test.chatapp.adapters.RecommendPlaceAdapter;
import com.test.chatapp.databinding.ActivityRecommandPlaceBinding;
import com.test.chatapp.httpserver.HttpClient;
import com.test.chatapp.models.RecommendPlace;
import com.test.chatapp.models.Restaurant;
import com.test.chatapp.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecommandPlaceActivity extends AppCompatActivity {
    private ActivityRecommandPlaceBinding binding;
    private FilterOptionAdminAreaAdapter filterOptionAdminAreaAdapter;
    private FilterOptionHashTagAdapter hashTagAdapter;
    private RecommendPlaceAdapter recommendPlaceAdapter;

    List<String> filterOptions;
    String postsJason;
    static public List<String> adminAreas;
    static public List<String> hashTags;

    private List<RecommendPlace> recommendPlaces1;
    private List<RecommendPlace> recommendPlaces2;
    private List<RecommendPlace> recommendPlaces3;
    private List<RecommendPlace> recommendPlaces4;
    private List<RecommendPlace> recommendPlaces5;
    private List<RecommendPlace> recommendPlaces6;
    private List<RecommendPlace> recommendPlaces7;
    private List<RecommendPlace> recommendPlaces8;
    private List<RecommendPlace> recommendPlaces9;
    private List<RecommendPlace> recommendPlaces10;

    static public List<String> selectedAdminArea;
    static public List<String> selectedHashTags;
    int selectedAge;

    int regionBtnCheck;
    int hashTagsBtnCheck;
    int ageBtnCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecommandPlaceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        getAllPosts();

        addFilterOption();

        clickListener();
    }

    private void init() {
        filterOptions = new ArrayList<>();
        adminAreas = new ArrayList<>();
        hashTags = new ArrayList<>();

        selectedHashTags = new ArrayList<>();
        selectedAdminArea = new ArrayList<>();

        recommendPlaces1 = new ArrayList<>();
        recommendPlaces2 = new ArrayList<>();
        recommendPlaces3 = new ArrayList<>();
        recommendPlaces4 = new ArrayList<>();
        recommendPlaces5 = new ArrayList<>();
        recommendPlaces6 = new ArrayList<>();
        recommendPlaces7 = new ArrayList<>();
        recommendPlaces8 = new ArrayList<>();
        recommendPlaces9 = new ArrayList<>();
        recommendPlaces10 = new ArrayList<>();

        filterOptionAdminAreaAdapter = new FilterOptionAdminAreaAdapter(
                adminAreas
        );

        hashTagAdapter = new FilterOptionHashTagAdapter(
                hashTags
        );

        recommendPlaceAdapter = new RecommendPlaceAdapter(
                recommendPlaces1
        );

        ageBtnCheck = 0;
        regionBtnCheck = 0;
        hashTagsBtnCheck = 0;
    }

    private void clickListener() {
        binding.imageBack.setOnClickListener(v -> finish());

        binding.regionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.hashTagsRecyclerView.setVisibility(View.GONE);
                binding.ageLayout.setVisibility(View.GONE);
                binding.adminAreaRecyclerView.setVisibility(View.VISIBLE);

                binding.regionBtn.setBackgroundResource(R.drawable.rect_background_border_blue);
                binding.regionText.setTextColor(Color.parseColor("#FFFFFF"));

                binding.hashTagBtn.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.hashTagText.setTextColor(Color.parseColor("#288CFF"));
                binding.ageBtn.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.ageText.setTextColor(Color.parseColor("#288CFF"));
            }
        });

        binding.hashTagBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.adminAreaRecyclerView.setVisibility(View.GONE);
                binding.ageLayout.setVisibility(View.GONE);
                binding.hashTagsRecyclerView.setVisibility(View.VISIBLE);

                binding.hashTagBtn.setBackgroundResource(R.drawable.rect_background_border_blue);
                binding.hashTagText.setTextColor(Color.parseColor("#FFFFFF"));

                binding.regionBtn.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.regionText.setTextColor(Color.parseColor("#288CFF"));
                binding.ageBtn.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.ageText.setTextColor(Color.parseColor("#288CFF"));
            }
        });

        binding.ageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.adminAreaRecyclerView.setVisibility(View.GONE);
                binding.hashTagsRecyclerView.setVisibility(View.GONE);
                binding.ageLayout.setVisibility(View.VISIBLE);

                binding.ageBtn.setBackgroundResource(R.drawable.rect_background_border_blue);
                binding.ageText.setTextColor(Color.parseColor("#FFFFFF"));

                binding.hashTagBtn.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.hashTagText.setTextColor(Color.parseColor("#288CFF"));
                binding.regionBtn.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.regionText.setTextColor(Color.parseColor("#288CFF"));
            }
        });

        binding.tenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAge = 10;
                binding.tenLayout.setBackgroundResource(R.drawable.rect_background_border_blue);
                binding.tenText.setTextColor(Color.parseColor("#FFFFFF"));

                binding.twentyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.twentyText.setTextColor(Color.parseColor("#000000"));
                binding.thirtyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.thirtyText.setTextColor(Color.parseColor("#000000"));
                binding.fortyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.fortyText.setTextColor(Color.parseColor("#000000"));
                binding.fiftyPlusLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.fiftyPlusText.setTextColor(Color.parseColor("#000000"));
            }
        });

        binding.twentyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAge = 20;

                binding.twentyLayout.setBackgroundResource(R.drawable.rect_background_border_blue);
                binding.twentyText.setTextColor(Color.parseColor("#FFFFFF"));

                binding.tenLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.tenText.setTextColor(Color.parseColor("#000000"));
                binding.thirtyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.thirtyText.setTextColor(Color.parseColor("#000000"));
                binding.fortyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.fortyText.setTextColor(Color.parseColor("#000000"));
                binding.fiftyPlusLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.fiftyPlusText.setTextColor(Color.parseColor("#000000"));
            }
        });

        binding.thirtyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAge = 30;

                binding.thirtyLayout.setBackgroundResource(R.drawable.rect_background_border_blue);
                binding.thirtyText.setTextColor(Color.parseColor("#FFFFFF"));

                binding.twentyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.twentyText.setTextColor(Color.parseColor("#000000"));
                binding.tenLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.tenText.setTextColor(Color.parseColor("#000000"));
                binding.fortyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.fortyText.setTextColor(Color.parseColor("#000000"));
                binding.fiftyPlusLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.fiftyPlusText.setTextColor(Color.parseColor("#000000"));
            }
        });

        binding.fortyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAge = 40;

                binding.fortyLayout.setBackgroundResource(R.drawable.rect_background_border_blue);
                binding.fortyText.setTextColor(Color.parseColor("#FFFFFF"));

                binding.twentyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.twentyText.setTextColor(Color.parseColor("#000000"));
                binding.thirtyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.thirtyText.setTextColor(Color.parseColor("#000000"));
                binding.tenLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.tenText.setTextColor(Color.parseColor("#000000"));
                binding.fiftyPlusLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.fiftyPlusText.setTextColor(Color.parseColor("#000000"));
            }
        });

        binding.fiftyPlusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedAge = 50;

                binding.fiftyPlusLayout.setBackgroundResource(R.drawable.rect_background_border_blue);
                binding.fiftyPlusText.setTextColor(Color.parseColor("#FFFFFF"));

                binding.twentyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.twentyText.setTextColor(Color.parseColor("#000000"));
                binding.thirtyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.thirtyText.setTextColor(Color.parseColor("#000000"));
                binding.fortyLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.fortyText.setTextColor(Color.parseColor("#000000"));
                binding.tenLayout.setBackgroundResource(R.drawable.rect_background_border_white);
                binding.tenText.setTextColor(Color.parseColor("#000000"));
            }
        });

        binding.okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedAdminArea.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "지역을 선택하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    String recommendPlaces = getRecommendPlacePosts();
                    recommendPlacesParsing(recommendPlaces);
                }
            }
        });

        binding.recommendLayout1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GoogleMapActivity.class);
                intent.putExtra("recommendLat", recommendPlaces1.get(0).lat);
                intent.putExtra("recommendLon", recommendPlaces1.get(0).lon);
                intent.putExtra("GOOGLE_MAP_RECOMMEND", 300);
                startActivity(intent);
            }
        });
        binding.recommendLayout2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GoogleMapActivity.class);
                intent.putExtra("recommendLat", recommendPlaces2.get(0).lat);
                intent.putExtra("recommendLon", recommendPlaces2.get(0).lon);
                intent.putExtra("GOOGLE_MAP_RECOMMEND", 300);
                startActivity(intent);
            }
        });
        binding.recommendLayout3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GoogleMapActivity.class);
                intent.putExtra("recommendLat", recommendPlaces3.get(0).lat);
                intent.putExtra("recommendLon", recommendPlaces3.get(0).lon);
                intent.putExtra("GOOGLE_MAP_RECOMMEND", 300);
                startActivity(intent);
            }
        });
        binding.recommendLayout4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GoogleMapActivity.class);
                intent.putExtra("recommendLat", recommendPlaces4.get(0).lat);
                intent.putExtra("recommendLon", recommendPlaces4.get(0).lon);
                intent.putExtra("GOOGLE_MAP_RECOMMEND", 300);
                startActivity(intent);
            }
        });
        binding.recommendLayout5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GoogleMapActivity.class);
                intent.putExtra("recommendLat", recommendPlaces5.get(0).lat);
                intent.putExtra("recommendLon", recommendPlaces5.get(0).lon);
                intent.putExtra("GOOGLE_MAP_RECOMMEND", 300);
                startActivity(intent);
            }
        });
    }

    private String getRecommendPlacePosts() {
        try {
            Collections.sort(selectedHashTags, (obj1, obj2) -> obj1.compareTo(obj2));
            String request = "place/search?adminarea=" + selectedAdminArea.toString().replace("[", "").replace(" ", "").replace("]", "")
                    + "&age=" + selectedAge + "&hashtags=" + selectedHashTags.toString().replace("[", "").replace(", ", "").replace("]", "").replace("#", "$");
            HttpClient httpClient = new HttpClient(request);
            Thread th = new Thread(httpClient);
            th.start();
            String result = null;

            long start = System.currentTimeMillis();

            while (result == null) {
                result = httpClient.getResult();
                long end = System.currentTimeMillis();
                if (end - start > 5000) {
                    return null;
                }
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private void getAllPosts() {
        try {
            String request = "place/searchall";
            HttpClient httpclient = new HttpClient(request);
            Thread th = new Thread(httpclient);
            th.start();
            String result = null;

            long start = System.currentTimeMillis();

            while (result == null) {
                result = httpclient.getResult();
                long end = System.currentTimeMillis();
                if (end - start > 5000) {
                    return;
                }
            }
            postsJason = result;
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void addFilterOption() {
        try {
            JSONArray jsonArray = new JSONArray(postsJason);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonobj = jsonArray.getJSONObject(i);
                if (adminAreas.contains(jsonobj.getString("adminArea"))) {

                } else {
                    adminAreas.add(jsonobj.getString("adminArea"));
                }

                String[] hashArrays = jsonobj.getString("hashTags").replace("$", "#").split("#");
                for (String hashArray : hashArrays) {
                    if (hashTags.contains("#" + hashArray)) {

                    } else {
                        System.out.println(jsonobj.getString("hashTags").replace("$", "#"));
                        if (!hashArray.isEmpty())
                            hashTags.add("#" + hashArray);
                    }
                }
            }
            Collections.sort(adminAreas);
            Collections.sort(hashTags);

            filterOptionAdminAreaAdapter = new FilterOptionAdminAreaAdapter(adminAreas);
            binding.adminAreaRecyclerView.setAdapter(filterOptionAdminAreaAdapter);

            hashTagAdapter = new FilterOptionHashTagAdapter(hashTags);
            binding.hashTagsRecyclerView.setAdapter(hashTagAdapter);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void recommendPlacesParsing(String recommendPlaces) {
        try {
            recommendPlaces1.clear();
            recommendPlaces2.clear();
            recommendPlaces3.clear();
            recommendPlaces4.clear();
            recommendPlaces5.clear();
            recommendPlaces6.clear();
            recommendPlaces7.clear();
            recommendPlaces8.clear();
            recommendPlaces9.clear();
            recommendPlaces10.clear();


            binding.recommendLayout1.setVisibility(View.GONE);
            binding.recommendLayout2.setVisibility(View.GONE);
            binding.recommendLayout3.setVisibility(View.GONE);
            binding.recommendLayout4.setVisibility(View.GONE);
            binding.recommendLayout5.setVisibility(View.GONE);
            binding.recommendLayout6.setVisibility(View.GONE);
            binding.recommendLayout7.setVisibility(View.GONE);
            binding.recommendLayout8.setVisibility(View.GONE);
            binding.recommendLayout9.setVisibility(View.GONE);
            binding.recommendLayout10.setVisibility(View.GONE);

            JSONArray jsonArray = new JSONArray(recommendPlaces);

            for (int i = 0; i < jsonArray.length(); i++) {
                RecommendPlace recommendPlace = new RecommendPlace();
                JSONObject jsonobj = jsonArray.getJSONObject(i);
                recommendPlace.idPlace = jsonobj.getInt("idPlace");
                recommendPlace.lat = jsonobj.getDouble("lat");
                recommendPlace.lon = jsonobj.getDouble("lon");
                recommendPlace.placeName = jsonobj.getString("placeName");
                recommendPlace.age = jsonobj.getInt("age");
                recommendPlace.hashTags = jsonobj.getString("hashTags");
                recommendPlace.image = jsonobj.getString("image");
                recommendPlace.address = getAddress(getApplicationContext(), recommendPlace.lat, recommendPlace.lon);

                if (recommendPlaces1.isEmpty()) {
                    recommendPlaces1.add(recommendPlace);
                    continue;
                } else if (recommendPlaces2.isEmpty() && !recommendPlaces1.isEmpty()) {
                    if (recommendPlaces1.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces1.get(0).placeName + "$" + recommendPlaces1.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces2.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces1.get(0).address.getLocality() != null &&
                            !(recommendPlaces1.get(0).placeName + "$" + recommendPlaces1.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces2.add(recommendPlace);
                        continue;
                    }
                } else if (recommendPlaces3.isEmpty() && !recommendPlaces2.isEmpty()) {
                    if (recommendPlaces2.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces2.get(0).placeName + "$" + recommendPlaces2.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces3.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces2.get(0).address.getLocality() != null &&
                            !(recommendPlaces2.get(0).placeName + "$" + recommendPlaces2.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces3.add(recommendPlace);
                        continue;
                    }
                } else if (recommendPlaces4.isEmpty() && !recommendPlaces3.isEmpty()) {
                    if (recommendPlaces3.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces3.get(0).placeName + "$" + recommendPlaces3.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces4.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces3.get(0).address.getLocality() != null &&
                            !(recommendPlaces3.get(0).placeName + "$" + recommendPlaces3.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces4.add(recommendPlace);
                        continue;
                    }
                } else if (recommendPlaces5.isEmpty() && !recommendPlaces4.isEmpty()) {
                    if (recommendPlaces4.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces4.get(0).placeName + "$" + recommendPlaces4.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces5.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces4.get(0).address.getLocality() != null &&
                            !(recommendPlaces4.get(0).placeName + "$" + recommendPlaces4.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces5.add(recommendPlace);
                        continue;
                    }
                } else if (recommendPlaces6.isEmpty() && !recommendPlaces5.isEmpty()) {
                    if (recommendPlaces5.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces5.get(0).placeName + "$" + recommendPlaces5.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces6.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces5.get(0).address.getLocality() != null &&
                            !(recommendPlaces5.get(0).placeName + "$" + recommendPlaces5.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces6.add(recommendPlace);
                        continue;
                    }
                } else if (recommendPlaces7.isEmpty() && !recommendPlaces6.isEmpty()) {
                    if (recommendPlaces6.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces6.get(0).placeName + "$" + recommendPlaces6.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces7.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces6.get(0).address.getLocality() != null &&
                            !(recommendPlaces6.get(0).placeName + "$" + recommendPlaces6.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces7.add(recommendPlace);
                        continue;
                    }
                } else if (recommendPlaces8.isEmpty() && !recommendPlaces7.isEmpty()) {
                    if (recommendPlaces7.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces7.get(0).placeName + "$" + recommendPlaces7.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces8.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces7.get(0).address.getLocality() != null &&
                            !(recommendPlaces7.get(0).placeName + "$" + recommendPlaces7.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces8.add(recommendPlace);
                        continue;
                    }
                } else if (recommendPlaces9.isEmpty() && !recommendPlaces8.isEmpty()) {
                    if (recommendPlaces8.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces8.get(0).placeName + "$" + recommendPlaces8.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces9.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces8.get(0).address.getLocality() != null &&
                            !(recommendPlaces8.get(0).placeName + "$" + recommendPlaces8.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces9.add(recommendPlace);
                        continue;
                    }
                } else if (recommendPlaces10.isEmpty() && !recommendPlaces9.isEmpty()) {
                    if (recommendPlaces9.get(0).address.getSubLocality() != null &&
                            !(recommendPlaces9.get(0).placeName + "$" + recommendPlaces9.get(0).address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality())) {
                        recommendPlaces10.add(recommendPlace);
                        continue;
                    } else if (recommendPlaces9.get(0).address.getLocality() != null &&
                            !(recommendPlaces9.get(0).placeName + "$" + recommendPlaces9.get(0).address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality())) {
                        recommendPlaces10.add(recommendPlace);
                        continue;
                    }
                }

                for (RecommendPlace rp : recommendPlaces1) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces1.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces1.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces2) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces2.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces2.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces3) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces3.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces3.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces4) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces4.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces4.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces5) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces5.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces5.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces6) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces6.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces6.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces7) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces7.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces7.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces8) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces8.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces8.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces9) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces9.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces9.add(recommendPlace);
                        break;
                    }
                }

                for (RecommendPlace rp : recommendPlaces10) {
                    if (rp.address.getSubLocality() != null &&
                            (rp.placeName + "$" + rp.address.getSubLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getSubLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces10.add(recommendPlace);
                        break;
                    } else if (rp.address.getLocality() != null &&
                            (rp.placeName + "$" + rp.address.getLocality()).equals(recommendPlace.placeName + "$" + recommendPlace.address.getLocality()) && rp.idPlace != recommendPlace.idPlace) {
                        recommendPlaces10.add(recommendPlace);
                        break;
                    }
                }
            }
            Collections.shuffle(recommendPlaces1);
            Collections.shuffle(recommendPlaces2);
            Collections.shuffle(recommendPlaces3);
            Collections.shuffle(recommendPlaces4);
            Collections.shuffle(recommendPlaces5);
            Collections.shuffle(recommendPlaces6);
            Collections.shuffle(recommendPlaces7);
            Collections.shuffle(recommendPlaces8);
            Collections.shuffle(recommendPlaces9);
            Collections.shuffle(recommendPlaces10);

            if (!recommendPlaces1.isEmpty()) {
                if (recommendPlaces1.size() > 10) {
                    for (int i = recommendPlaces1.size() - 1; i > 4; i--) {
                        recommendPlaces1.remove(i);
                    }
                }
                binding.recommendLayout1.setVisibility(View.VISIBLE);
                binding.placeName1.setText(recommendPlaces1.get(0).placeName);
                binding.placeLocation1.setText(recommendPlaces1.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces1);
                binding.postRecyclerView1.setAdapter(recommendPlaceAdapter);
            }

            if (!recommendPlaces2.isEmpty()) {
                if (recommendPlaces2.size() > 10) {
                    for (int i = recommendPlaces2.size() - 1; i > 4; i--) {
                        recommendPlaces2.remove(i);
                    }
                }
                binding.recommendLayout2.setVisibility(View.VISIBLE);
                binding.placeName2.setText(recommendPlaces2.get(0).placeName);
                binding.placeLocation2.setText(recommendPlaces2.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces2);
                binding.postRecyclerView2.setAdapter(recommendPlaceAdapter);
            }
            if (!recommendPlaces3.isEmpty()) {
                if (recommendPlaces3.size() > 10) {
                    for (int i = recommendPlaces3.size() - 1; i > 4; i--) {
                        recommendPlaces3.remove(i);
                    }
                }
                binding.recommendLayout3.setVisibility(View.VISIBLE);
                binding.placeName3.setText(recommendPlaces3.get(0).placeName);
                binding.placeLocation3.setText(recommendPlaces3.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces3);
                binding.postRecyclerView3.setAdapter(recommendPlaceAdapter);
            }
            if (!recommendPlaces4.isEmpty()) {
                if (recommendPlaces4.size() > 10) {
                    for (int i = recommendPlaces4.size() - 1; i > 4; i--) {
                        recommendPlaces4.remove(i);
                    }
                }
                binding.recommendLayout4.setVisibility(View.VISIBLE);
                binding.placeName4.setText(recommendPlaces4.get(0).placeName);
                binding.placeLocation4.setText(recommendPlaces4.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces4);
                binding.postRecyclerView4.setAdapter(recommendPlaceAdapter);
            }
            if (!recommendPlaces5.isEmpty()) {
                if (recommendPlaces5.size() > 10) {
                    for (int i = recommendPlaces5.size() - 1; i > 4; i--) {
                        recommendPlaces5.remove(i);
                    }
                }
                binding.recommendLayout5.setVisibility(View.VISIBLE);
                binding.placeName5.setText(recommendPlaces5.get(0).placeName);
                binding.placeLocation5.setText(recommendPlaces5.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces5);
                binding.postRecyclerView5.setAdapter(recommendPlaceAdapter);
            }
            if (!recommendPlaces6.isEmpty()) {
                if (recommendPlaces6.size() > 10) {
                    for (int i = recommendPlaces6.size() - 1; i > 4; i--) {
                        recommendPlaces6.remove(i);
                    }
                }
                binding.recommendLayout6.setVisibility(View.VISIBLE);
                binding.placeName6.setText(recommendPlaces6.get(0).placeName);
                binding.placeLocation6.setText(recommendPlaces6.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces6);
                binding.postRecyclerView6.setAdapter(recommendPlaceAdapter);
            }
            if (!recommendPlaces7.isEmpty()) {
                if (recommendPlaces7.size() > 10) {
                    for (int i = recommendPlaces7.size() - 1; i > 4; i--) {
                        recommendPlaces7.remove(i);
                    }
                }
                binding.recommendLayout7.setVisibility(View.VISIBLE);
                binding.placeName7.setText(recommendPlaces7.get(0).placeName);
                binding.placeLocation7.setText(recommendPlaces7.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces7);
                binding.postRecyclerView7.setAdapter(recommendPlaceAdapter);
            }
            if (!recommendPlaces8.isEmpty()) {
                if (recommendPlaces8.size() > 10) {
                    for (int i = recommendPlaces8.size() - 1; i > 4; i--) {
                        recommendPlaces8.remove(i);
                    }
                }
                binding.recommendLayout8.setVisibility(View.VISIBLE);
                binding.placeName8.setText(recommendPlaces8.get(0).placeName);
                binding.placeLocation8.setText(recommendPlaces8.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces8);
                binding.postRecyclerView8.setAdapter(recommendPlaceAdapter);
            }
            if (!recommendPlaces9.isEmpty()) {
                if (recommendPlaces9.size() > 10) {
                    for (int i = recommendPlaces9.size() - 1; i > 4; i--) {
                        recommendPlaces9.remove(i);
                    }
                }
                binding.recommendLayout9.setVisibility(View.VISIBLE);
                binding.placeName9.setText(recommendPlaces9.get(0).placeName);
                binding.placeLocation9.setText(recommendPlaces9.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces9);
                binding.postRecyclerView9.setAdapter(recommendPlaceAdapter);
            }
            if (!recommendPlaces10.isEmpty()) {
                if (recommendPlaces10.size() > 10) {
                    for (int i = recommendPlaces10.size() - 1; i > 4; i--) {
                        recommendPlaces10.remove(i);
                    }
                }
                binding.recommendLayout10.setVisibility(View.VISIBLE);
                binding.placeName10.setText(recommendPlaces10.get(0).placeName);
                binding.placeLocation10.setText(recommendPlaces10.get(0).address.getAddressLine(0));

                recommendPlaceAdapter = new RecommendPlaceAdapter(recommendPlaces10);
                binding.postRecyclerView10.setAdapter(recommendPlaceAdapter);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}