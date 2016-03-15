package com.hvzhub.app.API.model.Login;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    private String email;
    private String password;

    @SerializedName("stay_logged_in")
    private boolean stayLoggedIn;

    public LoginRequest(String email, String password, boolean stayLoggedIn) {
        this.email = email;
        this.password = password;
        this.stayLoggedIn = stayLoggedIn;
    }
}
