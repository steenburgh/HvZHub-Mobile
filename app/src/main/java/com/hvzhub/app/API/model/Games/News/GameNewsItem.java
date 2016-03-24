package com.hvzhub.app.API.model.Games.News;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class GameNewsItem {
    public static final int ROUND_END = -2;
    public static final int ROUND_START = -1;
    public static final int JOIN = 0;
    public static final int TAG = 1;
    public static final int TAG_WITH_ASSISTANTS = 2;
    public static final int TAG_OZ = 3;
    public static final int TAG_OZ_WITH_ASSISTANTS = 4;
    public static final int OZ_REVEAL = 5;

    public int type;

    @SerializedName("dt")
    public Date timestamp;

    @SerializedName("p0name")
    public String player0Name;

    @SerializedName("p1name")
    public String player1Name;

    public List<Player> assistants;
}
