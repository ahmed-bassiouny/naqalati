package com.bassiouny.naqalati.model;

/**
 * Created by bassiouny on 15/12/17.
 */

public class File {
    private String userUploadedId;
    private String path;
    private String userType;


    public String getUserUploadedId() {
        return userUploadedId;
    }

    public void setUserUploadedId(String userUploadedId) {
        this.userUploadedId = userUploadedId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
