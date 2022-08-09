package com.test.chatapp.models;

import android.location.Address;

import java.util.Date;
import java.util.List;

public class Post {
    public String postId, name, postcontents, userid, dateTime, image, placeName;
    public double lat, lon;
    public boolean privatekey;
    public Date dateObject;
    public String postImage;
    public String hashTags;
    public List<String> userMessage;
    public List<String> postLike;
    public Address address;
}
