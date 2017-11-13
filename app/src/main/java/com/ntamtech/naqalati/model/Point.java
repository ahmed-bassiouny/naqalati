package com.ntamtech.naqalati.model;

/**
 * Created by bassiouny on 13/11/17.
 */

public class Point {
    private Double Lat;
    private Double Lng;
    private String locationString;

    public Double getLat() {
        return Lat;
    }

    public void setLat(Double lat) {
        Lat = lat;
    }

    public Double getLng() {
        return Lng;
    }

    public void setLng(Double lng) {
        Lng = lng;
    }

    public String getLocationString() {
        return locationString;
    }

    public void setLocationString(String locationString) {
        this.locationString = locationString;
    }
}
