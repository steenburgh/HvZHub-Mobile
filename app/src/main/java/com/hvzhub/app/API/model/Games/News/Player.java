package com.hvzhub.app.API.model.Games.News;

import com.google.gson.annotations.SerializedName;

public class Player {
    @SerializedName("name")
    public String name;

    @SerializedName("id")
    public int id;

    public Player(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
