package com.hvzhub.app.API;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hvzhub.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

/**
 * A wrapper for the JSON API
 */
public class API {
    private static API mInstance;
    private static Context mCtx;

    // 10.0.2.2 is the local computer's address for when android is running in an emulator
    private static final String BASE_PATH = "http://10.0.2.2:8080/api/v1/";
    private static final String LOGIN = "login";
    private static final String FIELD_PASSWORD = "password";
    private static final String FIELD_EMAIL = "email";
    private static final String FIELD_SESSION_ID = "uuid";

    private String sessionID = null;

    /* This is implemented as a 'singleton'
     * This means that API is an object that can only be instantiated once
     * This also allows for API to have a context that is provided once, and remains the same
     * throughout the lifetime of the app
     */
    private API(Context context) {
        mCtx = context;
    }

    public static synchronized API getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new API(context);
        }
        return mInstance;
    }

    // TODO: Implement this using 'stay logged in flag'
    public void login(String email, String password, final LoginSuccessListener sl, final LoginErrorListener el) {
        final String REQUEST_URL = BASE_PATH + LOGIN;

        if (!checkConnectivity(el)) {
            return;
        }

        JSONObject data = new JSONObject();
        try {
            data.put(FIELD_PASSWORD, password);
            data.put(FIELD_EMAIL, email);
        } catch (JSONException e) {
            // The two cases a JSONException could be thrown here
            // 1. Null name parameter.
            // 2. NaN or infinite integer parameter
            // These can both never happen unless the code changes drastically
            throw new RuntimeException("This should never happen", e);
        }

        JsonObjectRequest j = new JsonObjectRequest(
                Request.Method.POST,
                REQUEST_URL,
                data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            sessionID = response.getString(FIELD_SESSION_ID);
                        } catch (JSONException jse) {
                            String errorMsg = mCtx.getString(R.string.unexpected_response);
                            String errorHint = mCtx.getString(R.string.unexpected_response_hint);
                            el.onError(HttpURLConnection.HTTP_OK, errorMsg, errorHint);
                        }
                        sl.onLoginSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        int statusCode = -1;
                        String errorMsg = "";
                        String errorHint = "";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            NetworkResponse response = error.networkResponse;

                            try {
                                // Response.data is a byte array, not necessarily a JSONObject
                                // so this call may not be successful
                                JSONObject errorJson = new JSONObject(new String(response.data));
                                if (errorJson.has(FIELD_EMAIL)) {
                                    el.onBadUserName(errorJson.getString(FIELD_EMAIL));
                                    return;
                                } else if (errorJson.has(FIELD_PASSWORD)) {
                                    el.onBadPassword(errorJson.getString(FIELD_PASSWORD));
                                    return;
                                }
                            } catch (JSONException e) {
                                // If we couldn't parse the data as JSON, this doesn't change how we handle the error.
                                // Do nothing
                            }

                            // The data wasn't in the expected format
                            statusCode = response.statusCode;
                            errorMsg = mCtx.getString(R.string.unexpected_response);
                            errorHint = mCtx.getString(R.string.unexpected_response_hint);
                        } else if (error instanceof TimeoutError) {
                            errorMsg = mCtx.getString(R.string.connection_timeout);
                            errorHint = mCtx.getString(R.string.connection_timeout_hint);
                        } else {
                            errorMsg = mCtx.getString(R.string.generic_connection_error);
                            errorHint = mCtx.getString(R.string.generic_connection_error_hint);
                        }
                        el.onError(statusCode, errorMsg, errorHint);
                    }
                }
        );
        APIRequestQueue.getInstance(mCtx).addToRequestQueue(j);
    }

    private boolean checkConnectivity(ErrorListener el) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mCtx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean networkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();

        if (!networkAvailable) {
            String errorMsg = mCtx.getString(R.string.network_not_available);
            String errorHint = mCtx.getString(R.string.network_not_available_hint);
            el.onError(-1, errorMsg, errorHint);
        }
        return networkAvailable;
    }

    public interface ErrorListener {
        /**
         * Called when there is an unexpected error.
         *
         * @param statusCode The HTTP Status code if applicable. -1 if not.
         * @param errorMsg The technical error message.
         * @param errorHint A human readable suggestion on how to solve the problem.
         */
        void onError(int statusCode, String errorMsg, String errorHint);
    }

    public interface LoginErrorListener extends ErrorListener {
        void onBadUserName(String msg);
        void onBadPassword(String msg);
    }

    public interface LoginSuccessListener {
        void onLoginSuccess();
    }
}
