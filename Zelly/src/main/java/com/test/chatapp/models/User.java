package com.test.chatapp.models;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
    public int position;
    public String name, image, email, token, id, state, receiveId;
    public Date timestamp;
    public int age;
}
