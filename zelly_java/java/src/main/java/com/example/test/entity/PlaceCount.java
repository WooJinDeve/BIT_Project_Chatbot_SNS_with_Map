package com.example.test.entity;

public class PlaceCount implements Comparable<PlaceCount> {
    public String placeName;
    public int count;

    public PlaceCount(String placeName) {
        this.placeName = placeName;
    }

    public PlaceCount(String placeName, int count) {
        this.placeName = placeName;
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public int compareTo(PlaceCount s) {
        if (this.count > s.getCount()) {
            return -1;
        } else if (this.count < s.getCount()) {
            return 1;
        }
        return 0;
    }
}
