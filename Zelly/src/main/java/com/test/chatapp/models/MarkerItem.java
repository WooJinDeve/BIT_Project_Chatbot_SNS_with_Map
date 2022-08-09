package com.test.chatapp.models;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.test.chatapp.activities.GoogleMapActivity;

public class MarkerItem implements ClusterItem {
    String postId;
    double lat;
    double lon;
    String userId;
    String name;
    String image;
    String postContents;
    BitmapDescriptor icon;
    boolean pinCheck; //맵에서 자신이 찍은 핀일 경우 true 아닐 경우 false
    String hashTags;


    public MarkerItem(String postId, double lat, double lon, String userId, String name, String image, String postContents, boolean pinCheck, BitmapDescriptor icon, String hashTags) {
        this.postId = postId;
        this.lat = lat;
        this.lon = lon;
        this.userId = userId;
        this.name = name;
        this.image = image;
        this.postContents = postContents;
        this.pinCheck = pinCheck;
        this.icon = icon;
        this.hashTags = hashTags;
    }

    public String getPostContents() {
        return postContents;
    }

    public String getPostId() {
        return postId;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getName() {
        return name;
    }

    public BitmapDescriptor getIcon() {
        return icon;
    }

    public String getUserId() {
        return userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean getPinCheck() {
        return pinCheck;
    }

    public String getHashTags() {
        return hashTags;
    }

    @Override
    public LatLng getPosition() {
        LatLng position = new LatLng(lat, lon);
        return position;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSnippet() {
        return postContents + "\n/split" + postId;
    }
}
