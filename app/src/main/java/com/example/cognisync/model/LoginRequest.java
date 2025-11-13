package com.example.cognisync.model;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Optional: getters (if your retrofit uses serialization with getter access)
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
