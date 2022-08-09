package com.test.chatapp.models;

public class Calendar {
    public String message, date;
    public String friend, friend_name, my_name, me;
    public String documentid;
    public Double lat, lon;
    public String explain;
    public String starthour, startminute;
    public String endhour, endminute;

    public int getParseIntToHour() {
        return Integer.parseInt(starthour);
    }
}
