package com.hvzhub.app.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    public static final String API_BASE_URL = "http://hvzhub.com/api/v1/";

    private static Retrofit retrofit;
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder;

    public static <S> S createService(Class<S> serviceClass) {
        return retrofit().create(serviceClass);
    }

    public static Retrofit retrofit() {
        if (retrofit == null) {
            // TODO: Remove these lines
            // To enable logging, uncomment these lines and import okhttp3.logging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Date.class, DateConverter.getInstance())
                    .create();

            builder = new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson));

            retrofit = builder.client(httpClient.build()).build();
        }
        return retrofit;
    }
}
