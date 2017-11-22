package com.bassiouny.naqalati.model;

import java.io.Serializable;

/**
 * Created by bassiouny on 22/11/17.
 */

public class SpecialRequest{
    private String name;
    private String phone;
    private String address;
    private String email;
    private String requestType;
    private String size;
    private String startPoint;
    private String endPoint;
    private String pay;
    private String number;
    private String carType;
    private String numberOfCar;

    public SpecialRequest(String name, String phone, String address, String email, String requestType, String size, String startPoint, String endPoint, String pay, String number, String carType, String numberOfCar) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.email = email;
        this.requestType = requestType;
        this.size = size;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.pay = pay;
        this.number = number;
        this.carType = carType;
        this.numberOfCar = numberOfCar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getPay() {
        return pay;
    }

    public void setPay(String pay) {
        this.pay = pay;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getNumberOfCar() {
        return numberOfCar;
    }

    public void setNumberOfCar(String numberOfCar) {
        this.numberOfCar = numberOfCar;
    }
}
