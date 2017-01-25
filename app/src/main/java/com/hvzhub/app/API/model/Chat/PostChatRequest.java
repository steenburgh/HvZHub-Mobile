package com.hvzhub.app.API.model.Chat;

import com.google.gson.annotations.SerializedName;
import com.hvzhub.app.API.model.Uuid;

public class PostChatRequest {
    public String uuid;

    @SerializedName("user_id")
    public int userId;

    public String text;

    @SerializedName("is_h")
    public boolean isHuman;

    public PostChatRequest(Uuid uuid, int userId, String text, boolean isHuman) {
        this.uuid = uuid.uuid;
        this.userId = userId;
        this.text = text;
        this.isHuman = isHuman;
    }
}
