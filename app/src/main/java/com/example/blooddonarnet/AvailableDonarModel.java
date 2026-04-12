package com.example.blooddonarnet;

public class AvailableDonarModel {

    public String name;
    public String phone;
    public String bloodGroup;
    public double latitude;
    public double longitude;

    public String uid;
    private float distance;

    public AvailableDonarModel() {}

    public AvailableDonarModel(String name, String phone, String bloodGroup, double lat, double lng) {
        this.name = name;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.latitude = lat;
        this.longitude = lng;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getBloodGroup() { return bloodGroup; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }
}