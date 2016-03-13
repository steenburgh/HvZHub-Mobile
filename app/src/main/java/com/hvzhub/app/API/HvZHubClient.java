package com.hvzhub.app.API;

import com.hvzhub.app.API.model.ChapterListContainer;
import com.hvzhub.app.API.model.Login.LoginRequest;
import com.hvzhub.app.API.model.Login.Session;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface HvZHubClient {
    @GET("chapters")
    Call<ChapterListContainer> chapters();

    @POST("login")
    Call<Session> login(@Body LoginRequest loginRequest);
}
