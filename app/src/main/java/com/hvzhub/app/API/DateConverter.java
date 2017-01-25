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

    private static DateConverter mInstance;

    public DateConverter() {
    }

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
        // Unfortunately, there isn't a good ISO 8601 parser in java right
        // now so we have to do a some of the parsing on our own
        dateStr = cleanDateStr(dateStr);

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PREFERRED);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.parse(dateStr);
    }

    /**
     * Cleans up a dateString such that it looks like:
     * "2016-04-02T23:05:42.340Z"
     */
    private String cleanDateStr(String dateStr) {
        String[] dateStrSplit = dateStr.split("\\.");
        if (dateStrSplit.length == 1) {
            // Handle the case where a string looks like:
            // "2016-04-02T23:05:42Z"
            return dateStr.replace("Z", "") + ".000Z";
        }
        else if (dateStrSplit[1].length() > 4) {
            // Handle the case where a string looks like:
            // "2016-04-02T23:05:42.340714Z"
            StringBuilder sb = new StringBuilder(dateStr.length() - 3);
            sb.append(dateStrSplit[0]);
            sb.append('.');
            sb.append(dateStrSplit[1].substring(0, 3));
            sb.append('Z');
            return sb.toString();
        }
        else {
            // String is already in the format we want
            return dateStr;
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
