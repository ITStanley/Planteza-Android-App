package com.example.planteza;

public class ReadWriteUserDetails {
    public  String name;
    public  String email;
    public  String phone;
    public  double latitude;
    public  double longitude;

    //constructor
    public ReadWriteUserDetails(){}

    //getters ans setters
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
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

    public ReadWriteUserDetails(String userName, String userEmail, String userPhone, double userLatitude, double userLongitude){
        this.name = userName;
        this.email = userEmail;
        this.phone = userPhone;
        this.latitude = userLatitude;
        this.longitude = userLongitude;
    }
}
