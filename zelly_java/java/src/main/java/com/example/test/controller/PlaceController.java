package com.example.test.controller;

import com.example.test.entity.Place;
import com.example.test.entity.PlaceCount;
import com.example.test.repository.PlaceRepository;
import com.google.gson.Gson;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class PlaceController {
    @Autowired
    PlaceRepository placeRepository;

    @ResponseBody
    @GetMapping("place/add")
    public void AddPlace(@RequestParam(value = "lat", required = false) String lat, @RequestParam(value = "lon", required = false) String lon, @RequestParam(value = "placename", required = false) String placeName, @RequestParam(value = "adminarea", required = false) String adminArea, @RequestParam(value = "locality", required = false) String locality, @RequestParam(value = "sublocality", required = false) String subLocality, @RequestParam(value = "thoroughfare", required = false) String thoroughfare, @RequestParam(value = "age", required = false) String age, @RequestParam(value = "hashtags", required = false) String hashTags, @RequestParam(value = "image", required = false) String image) {


        Place place = Place.builder().lat(Double.parseDouble(lat)).lon(Double.parseDouble(lon)).placeName(placeName).adminArea(adminArea).locality(locality).subLocality(subLocality).thoroughfare(thoroughfare).age(Integer.parseInt(age)).hashTags(hashTags).image(image).build();            //Create!

        placeRepository.save(place);
    }

    @ResponseBody
    @GetMapping("place/searchall")
    public String SearchAllPlace() {
        String json = new Gson().toJson(placeRepository.selectAll());
        return json;
    }

    @ResponseBody
    @GetMapping("place/search")
    public String SearchPlace(@RequestParam(value = "adminarea", required = false) String adminArea, @RequestParam(value = "age", required = false) String age, @RequestParam(value = "hashtags", required = false) String hashTags) {
        List<Place> places = placeRepository.findAll();
        List<Place> searchPlaces = new ArrayList<>();

        System.out.println(adminArea + ", " + age + ", " + hashTags);

        for (Place place : places) {
            //다 채웠을 경우
            if (age != "" && hashTags != "" && Integer.parseInt(age) <= place.getAge() && place.getAge() <= Integer.parseInt(age) + 9) {
                String[] areas = adminArea.split(",");

                for (String area : areas) {
                    if (place.getAdminArea().equals(area)) {
                        hashTags = hashTags.replace("$", "#");
                        String[] strings = hashTags.split("#");
                        int count = 0;
                        for (String s : strings) {
                            if (place.getHashTags().contains(s)) {
                                count++;
                            }
                        }
                        if (count == strings.length) {
                            searchPlaces.add(place);
                        }
                    }
                }
            }

            //나이 없을 경우
            else if (hashTags != "" && age.equals("0")) {
                String[] areas = adminArea.split(",");

                for (String area : areas) {
                    if (place.getAdminArea().equals(area)) {
                        hashTags = hashTags.replace("$", "#");
                        String[] strings = hashTags.split("#");
                        int count = 0;
                        for (String s : strings) {
                            if (place.getHashTags().contains(s)) {
                                count++;
                            }
                        }
                        if (count == strings.length) {
                            searchPlaces.add(place);
                        }
                    }
                }
            }
            //해시태그 없을 경우
            if (age != "" && hashTags == "" && Integer.parseInt(age) <= place.getAge() && place.getAge() <= Integer.parseInt(age) + 9) {
                String[] areas = adminArea.split(",");

                for (String area : areas) {
                    if (place.getAdminArea().equals(area)) {
                        searchPlaces.add(place);
                    }
                }
            }
            //지역만 채웠을 경우
            if (age.equals("0") && hashTags == "") {
                String[] areas = adminArea.split(",");

                for (String area : areas) {
                    if (place.getAdminArea().equals(area)) {
                        searchPlaces.add(place);
                    }
                }
            }
        }

        String json = new Gson().toJson(RecommendPlace(searchPlaces));

        System.out.println(json);
        return json;
    }

    public List<Place> RecommendPlace(List<Place> searchPlaces) {
        List<PlaceCount> popularPlaces = new ArrayList();
        List<Place> recommendPlace = new ArrayList<>();

        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Place searchPlace : searchPlaces) {
            Integer count = 0;
            if (!searchPlace.getSubLocality().equals("null")) {
                count = map.get(searchPlace.getPlaceName().replace(" ", "") + "$" + searchPlace.getSubLocality());
                if (count == null) {
                    map.put(searchPlace.getPlaceName().replace(" ", "") + "$" + searchPlace.getSubLocality(), 1);
                } else {
                    map.put(searchPlace.getPlaceName().replace(" ", "") + "$" + searchPlace.getSubLocality(), count + 1);
                }
            } else if (!searchPlace.getLocality().equals("null")) {
                count = map.get(searchPlace.getPlaceName().replace(" ", "") + "$" + searchPlace.getLocality());
                if (count == null) {
                    map.put(searchPlace.getPlaceName().replace(" ", "") + "$" + searchPlace.getLocality(), 1);
                } else {
                    map.put(searchPlace.getPlaceName().replace(" ", "") + "$" + searchPlace.getLocality(), count + 1);
                }
            }

        }

        for (String key : map.keySet()) {
            popularPlaces.add(new PlaceCount(key, map.get(key)));
        }

        Collections.sort(popularPlaces);

        //갯수
        if (popularPlaces.size() > 11) {
            for (int i = popularPlaces.size() - 1; i > 9; i--) {
                popularPlaces.remove(i);
            }
        }

        for (Place searchPlace : searchPlaces) {
            for (PlaceCount popularPlace : popularPlaces) {
                if (!searchPlace.getSubLocality().equals("null") &&
                        (searchPlace.getPlaceName() + "$" + searchPlace.getSubLocality()).equals(popularPlace.placeName)) {
                    recommendPlace.add(searchPlace);
                } else if (!searchPlace.getLocality().equals("null") &&
                        (searchPlace.getPlaceName() + "$" + searchPlace.getLocality()).equals(popularPlace.placeName)) {
                    recommendPlace.add(searchPlace);
                }
            }
        }
        Collections.sort(recommendPlace, (obj1, obj2) -> obj1.getPlaceName().compareTo(obj2.getPlaceName()));

        return recommendPlace;
    }
}
