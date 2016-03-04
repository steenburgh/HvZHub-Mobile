package com.hvzhub.app.API.model;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest() {}
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
