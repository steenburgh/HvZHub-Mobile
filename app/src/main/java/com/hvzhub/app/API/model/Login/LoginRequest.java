package com.hvzhub.app.API.model.Login;

public class LoginRequest {
    private String email;
    private String password;
    private boolean stayLoggedIn;

    public LoginRequest() {}
    public LoginRequest(String email, String password, boolean stayLoggedIn) {
        this.email = email;
        this.password = password;
        this.stayLoggedIn = stayLoggedIn;
    }
}
