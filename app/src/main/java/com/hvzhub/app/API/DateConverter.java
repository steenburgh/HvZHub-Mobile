package com.hvzhub.app.API;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateConverter implements JsonDeserializer<Date>, JsonSerializer<Date> {
    private final String DATE_FORMAT_PREFERRED = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private final String DATE_FORMAT_ALT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private static DateConverter mInstance;

    public DateConverter() {}

    public static DateConverter getInstance() {
        if (mInstance == null) {
            mInstance = new DateConverter();
        }
        return mInstance;
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return deserialize(json.getAsString());
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }

    public Date deserialize(String dateStr) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PREFERRED);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            // If the first attempt failed, try with the other format
            dateFormat = new SimpleDateFormat(DATE_FORMAT_ALT);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat.parse(dateStr);
        }
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(serialize(src));
    }

    public String serialize(Date src) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PREFERRED);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        return dateFormat.format(src);
    }
}
