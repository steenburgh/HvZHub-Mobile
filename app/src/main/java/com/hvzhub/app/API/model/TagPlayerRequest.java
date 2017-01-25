package com.hvzhub.app.API.model;

import com.google.gson.annotations.SerializedName;
import com.hvzhub.app.API.API;

import java.util.Date;

public class TagPlayerRequest {
    @SerializedName("tagged_code")
    public String taggedCode;

    @SerializedName("tagged_on")
    public Date taggedOn;

    @SerializedName("tag_lat")
    public Double Lat;

    @SerializedName("tag_long")
    public Double Long;

    public String uuid;

    public TagPlayerRequest(Uuid uuid, String taggedCode, Date taggedOnDateUtc, double Lat, double Long) {
        this.taggedCode = taggedCode;
        this.taggedOn = taggedOnDateUtc;
        this.uuid = uuid.uuid;
        this.Lat = Lat;
        this.Long = Long;
    }

    public TagPlayerRequest(Uuid uuid, String taggedCode, Date taggedOnDateUtc) {
        this.taggedCode = taggedCode;
        this.taggedOn = taggedOnDateUtc;
        this.uuid = uuid.uuid;
    }
}
