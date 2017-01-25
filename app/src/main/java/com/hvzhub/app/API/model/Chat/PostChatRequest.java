package com.hvzhub.app.API.model.Chat;

import com.google.gson.annotations.SerializedName;

public class PostChatRequest {
    public String uuid;

    @SerializedName("user_id")
    public int userId;

    public String text;

    @SerializedName("is_h")
    public boolean isHuman;

    public PostChatRequest(String uuid, int userId, String text, boolean isHuman) {
        this.uuid = uuid;
        this.userId = userId;
        this.text = text;
        this.isHuman = isHuman;
    }
}
