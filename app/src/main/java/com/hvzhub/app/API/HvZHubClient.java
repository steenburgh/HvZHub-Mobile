package com.hvzhub.app.API;

import com.hvzhub.app.API.model.APISuccess;
import com.hvzhub.app.API.model.Chapters.ChapterInfo;
import com.hvzhub.app.API.model.Chapters.ChapterListContainer;
import com.hvzhub.app.API.model.Chat.MessageListContainer;
import com.hvzhub.app.API.model.Chat.PostChatResponse;
import com.hvzhub.app.API.model.Chat.PostChatRequest;
import com.hvzhub.app.API.model.Code;
import com.hvzhub.app.API.model.CurrentUser;
import com.hvzhub.app.API.model.Games.GameListContainer;
import com.hvzhub.app.API.model.Games.HeatmapTagContainer;
import com.hvzhub.app.API.model.Games.News.GameNewsContainer;
import com.hvzhub.app.API.model.Games.News.ModUpdateContainer;
import com.hvzhub.app.API.model.Games.News.ModUpdatesContainer;
import com.hvzhub.app.API.model.Games.RecordContainer;
import com.hvzhub.app.API.model.Login.LoginRequest;
import com.hvzhub.app.API.model.Login.Session;
import com.hvzhub.app.API.model.Status;
import com.hvzhub.app.API.model.TagPlayerRequest;
import com.hvzhub.app.API.model.Uuid;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HvZHubClient {
    @POST("login")
    Call<Session> login(@Body LoginRequest loginRequest);

    @POST("currentuser")
    Call<CurrentUser> getCurrentUser(@Body Uuid uuid);

    // Chapter/Game selection
    @POST("chapters")
    Call<ChapterListContainer> getChapters(@Body Uuid uuid);

    @POST("chapters/{chapter_url}")
    Call<ChapterInfo> getChapterInfo(
            @Body Uuid uuid,
            @Path("chapter_url") String chapterUrl
    );

    @POST("chapters/{chapter_url}/join")
    Call<Status> joinChapter(
            @Path("chapter_url") String chapterUrl,
            @Body Uuid uuid
    );

    @POST("games")
    Call<GameListContainer> getGames(
            @Body Uuid uuid
    );



    @POST("games/{id}/join")
    Call<Status> joinGame(
            @Path("id") int gameId,
            @Body Uuid uuid
    );

    // Gameplay
    @POST("games/{id}/my_code")
    Call<Code> getMyCode(
            @Path("id") int id,
            @Body Uuid uuid
    );

    @POST("games/{id}/tag_player")
    Call<APISuccess> reportTag(
            @Path("id") int id,
            @Body TagPlayerRequest tagPlayerRequest
    );

    @POST("games/{id}/post_chat")
    Call<PostChatResponse> postChat(
            @Path("id") int id,
            @Body PostChatRequest postChatRequest
    );

    /**
     *
     * @param gameId
     * @param isHuman Whether or not the user is a human. Must be either "T" or "F"
     * @param initialNum How many messages back to start. Zero indexed.
     * @param numMsgs The total number of messages to return, starting from the initial message and working back
     */
    @POST("games/{id}/get_chat")
    Call<MessageListContainer> getChats(
            @Body Uuid uuid,
            @Path("id") int gameId,
            @Query("h") char isHuman,
            @Query("i") int initialNum,
            @Query("l") int numMsgs
    );

    /**
     * @param numItems If this is -1, the entire list is fetched starting at initialNum
     */
    @POST("games/{id}/news")
    Call<GameNewsContainer> getNews(
            @Body Uuid uuid,
            @Path("id") int gameId,
            @Query("i") int initialNum,
            @Query("l") int numItems
    );

    @POST("games/{id}/mod_updates")
    Call<ModUpdatesContainer> getModUpdates(
            @Body Uuid uuid,
            @Path("id") int gameId/*,
            @Query("i") int initialNum, // As of 4/9/16, these aren't supported by the API yet.
            @Query("l") int numItems*/
    );

    @POST("mod_updates/{id}")
    Call<ModUpdateContainer> getModUpdate(
            @Body Uuid uuid,
            @Path("id") int updateId/*,
            @Query("i") int initialNum, // As of 4/9/16, these aren't supported by the API yet.
            @Query("l") int numItems*/
    );

    @POST("games/{id}/heatmap")
    Call<HeatmapTagContainer> getHeatmap(
            @Body Uuid uuid,
            @Path("id") int gameId
    );
    
    @POST("games/{id}/my_record")
    Call<RecordContainer> getMyRecord(
            @Body Uuid uuid,
            @Path("id") int gameId
    );
}
