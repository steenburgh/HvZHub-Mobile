package com.hvzhub.app.API.model;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TagPlayerRequest {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    @SerializedName("tagged_code")
    public String taggedCode;

    @SerializedName("tagged_on")
    public String taggedOn;

    public Uuid uuid;

    public TagPlayerRequest(Uuid uuid, String taggedCode, Date taggedOnDateUtc) {
        this.taggedCode = taggedCode;
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar cal = Calendar.getInstance();
        this.taggedOn = dateFormat.format(cal.getTime());
        this.uuid = uuid;
    }
}
