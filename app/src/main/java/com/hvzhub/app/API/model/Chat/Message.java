package com.hvzhub.app.API.model.Chat;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Message {
    @SerializedName("uid")
    public int userId;

    @SerializedName("n")
    public String name;

    @SerializedName("x")
    public String message;

    @SerializedName("t")
    public Date timestamp;

    @SerializedName("id")
    public int msgId;

    public Message(
            int userId,
            String name,
            String message,
            Date timestamp,
            int msgId
    ) {
        this.userId = userId;
        this.name = name;
        this.message = message;
        this.timestamp = timestamp;
        this.msgId = msgId;
    }

    public Message(com.hvzhub.app.DB.Message dbMessage) {
        this.userId = dbMessage.getUserId();
        this.name = dbMessage.getName();
        this.message = dbMessage.getMessage();
        this.timestamp = dbMessage.getTimestamp();
        this.msgId = dbMessage.getMsgId();
    }
}
