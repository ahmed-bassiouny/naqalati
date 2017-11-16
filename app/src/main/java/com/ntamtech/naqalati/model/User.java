package com.ntamtech.naqalati.model;

/**
 * Created by bassiouny on 10/11/17.
 */

public class User {

    private String userName;
    private String userAvatar;
    private String userPhone;
    private String userPasswrod;
    private Double lat;
    private Double lng;
    private String currentRequest;
    private RequestStatus requestStatus;
    private String numberID;
    private String address;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserPasswrod() {
        return userPasswrod;
    }

    public void setUserPasswrod(String userPasswrod) {
        this.userPasswrod = userPasswrod;
    }

    public Double getLat() {
        if(lat==null)
            lat=0.0;
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        if(lng==null)
            lng=0.0;
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getCurrentRequest() {
        if(currentRequest==null)
            currentRequest="";
        return currentRequest;
    }

    public void setCurrentRequest(String currentRequest) {
        this.currentRequest = currentRequest;
    }

    public RequestStatus getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(RequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public String getNumberID() {
        if(numberID==null)
            numberID="";
        return numberID;
    }

    public void setNumberID(String numberID) {
        this.numberID = numberID;
    }

    public String getAddress() {
        if(address==null)
            address="";
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
