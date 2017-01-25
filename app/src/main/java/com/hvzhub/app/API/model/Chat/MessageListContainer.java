package com.hvzhub.app.API.model.Chat;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MessageListContainer {
    @SerializedName("msgs")
    public List<Message> messages;
}
