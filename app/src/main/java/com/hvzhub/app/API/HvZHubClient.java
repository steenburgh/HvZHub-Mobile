package com.hvzhub.app.API;

import com.hvzhub.app.API.model.Chapters.ChapterListContainer;
import com.hvzhub.app.API.model.Code;
import com.hvzhub.app.API.model.Login.LoginRequest;
import com.hvzhub.app.API.model.Login.Session;
import com.hvzhub.app.API.model.Status;
import com.hvzhub.app.API.model.Uuid;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface HvZHubClient {
    @POST("login")
    Call<Session> login(@Body LoginRequest loginRequest);

    @POST("chapters")
    Call<ChapterListContainer> chapters(@Body Uuid uuid);

    @POST("chapters/{chapter_url}/join")
    Call<Status> join(
            @Path("chapter_url") String chapterUrl,
            @Body Uuid uuid
    );

    @POST("games/{id}/my_code")
    Call<Code> getMyCode(
            @Path("id") int id,
            @Body Uuid uuid
    );
}
