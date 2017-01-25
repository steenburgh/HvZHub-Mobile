package com.hvzhub.app.API;

import com.hvzhub.app.API.model.APIError;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.hvzhub.app.R;
import com.hvzhub.app.SessionManager;

import java.text.ParseException;
import java.util.Date;

import retrofit2.Response;

/**
 * A wrapper for the JSON API
 */
public class API {
    private static API mInstance;
    private static Context mCtx;
    private HvZHubClient mHvZHubClient = null;

    public static final String DATE_FORMATA = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMATB = "yyyy-MM-dd'T'HH:mm:ss'Z'";

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

    public HvZHubClient getHvZHubClient() {
        if (mHvZHubClient == null) {
            return ServiceGenerator.createService(HvZHubClient.class);
        }
        return mHvZHubClient;
    }

    public static Date dateFromUtcString(String dateStr) throws ParseException {
        return DateConverter.getInstance().deserialize(dateStr);
    }

    public static String utcStringFromDate(Date date) {
        return DateConverter.getInstance().serialize(date);
    }

    /**
     * Check if the response contains a message indicating that the current session id is invalid
     * and log them out if needed.
     *
     * @param context
     * @param response
     * @return  True if the response indicates that the sessionId is valid.
     *          False if the sessionId is invalid and the user has been logged out.
     */
    public static boolean checkForInvalidSessionIdMsg(Context context, Response response) {
        APIError apiError = ErrorUtils.parseError(response);
        String err = apiError.error.toLowerCase();
        if (err.contains(context.getString(R.string.invalid_session_id))) {
            SessionManager.getInstance().logout();
            return false;
        }
        return true;
    }

    public static void displayUnexpectedResponseError(Context ctx, final OnRetryListener listener) {
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        b.setTitle(ctx.getString(R.string.unexpected_response))
                .setMessage(ctx.getString(R.string.unexpected_response_hint))
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.OnRetry();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public static void displayGenericConnectionError(Context ctx, final OnRetryListener listener) {
        AlertDialog.Builder b = new AlertDialog.Builder(ctx);
        b.setTitle(ctx.getString(R.string.generic_connection_error))
                .setMessage(ctx.getString(R.string.generic_connection_error_hint))
                .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.OnRetry();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public interface OnRetryListener {
        void OnRetry();
    }
}
