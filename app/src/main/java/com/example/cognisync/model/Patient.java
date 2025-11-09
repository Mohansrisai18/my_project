package com.example.cognisync.model;

public class Patient {
    private String full_name;
    private int age;
    private String gender;
    private String email;
    private String password;

    public Patient(String full_name, int age, String gender, String email, String password) {
        this.full_name = full_name;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.password = password;
    }
}
