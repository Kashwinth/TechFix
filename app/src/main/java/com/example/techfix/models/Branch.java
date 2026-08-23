package com.example.techfix.models;

public class Branch {
    private int id;
    private String locationName;
    private String address;
    private double latitude;
    private double longitude;

    public Branch() {}

    public Branch(int id, String locationName, double latitude, double longitude) {
        this(id, locationName, "", latitude, longitude);
    }

    public Branch(int id, String locationName, String address, double latitude, double longitude) {
        this.id = id;
        this.locationName = locationName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
