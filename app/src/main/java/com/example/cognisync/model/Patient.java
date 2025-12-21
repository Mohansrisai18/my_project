package com.example.cognisync.model;

import com.google.gson.annotations.SerializedName;

public class Patient {

    @SerializedName("user_id")   // ✅ MUST MATCH BACKEND
    private String userId;

    private int age;
    private String gender;
    private String email;
    private String password;

    public Patient(String userId, int age, String gender, String email, String password) {
        this.userId = userId;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
