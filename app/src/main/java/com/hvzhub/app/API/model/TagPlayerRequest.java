package com.hvzhub.app.API.model;

import com.google.gson.annotations.SerializedName;
import com.hvzhub.app.API.API;

import java.util.Date;

public class TagPlayerRequest {
    @SerializedName("tagged_code")
    public String taggedCode;

    @SerializedName("tagged_on")
    public String taggedOn;

    public String uuid;

    public TagPlayerRequest(String uuid, String taggedCode, Date taggedOnDateUtc) {
        this.taggedCode = taggedCode;
        this.taggedOn = API.utcStringFromDate(taggedOnDateUtc);
        this.uuid = uuid;
    }
}
