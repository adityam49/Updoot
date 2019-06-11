package com.ducktapedapps.updoot.utils;

import com.ducktapedapps.updoot.BuildConfig;
import com.ducktapedapps.updoot.api.endPoint;
import com.ducktapedapps.updoot.api.login;
import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.model.thing;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.GsonBuilder;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.ducktapedapps.updoot.utils.constants.baseUrl;
import static com.ducktapedapps.updoot.utils.constants.client_id;
import static com.ducktapedapps.updoot.utils.constants.token_access_base_url;

public class retrofitClient {

    private static final String TAG = "retrofitClient";

    private static Retrofit createForAuth() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.addNetworkInterceptor(new StethoInterceptor());

        //for logging requests
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(httpLoggingInterceptor);
        }

        builder.addInterceptor(chain -> {

            String credentials = Credentials.basic(client_id, "");
            Request request = chain
                    .request()
                    .newBuilder()
                    .addHeader("Authorization", credentials)
                    .build();
            return chain.proceed(request);
        });


        return new Retrofit.Builder()
                .baseUrl(token_access_base_url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();

    }

    private static Retrofit createForEndPoints(Token token) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.addNetworkInterceptor(new StethoInterceptor());


//        for logging requests
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(httpLoggingInterceptor);
        }

        builder.addInterceptor(chain -> {

            Request request = chain
                    .request()
                    .newBuilder()
                    .addHeader("Authorization", "bearer " + token.getAccess_token())
                    .build();
            return chain.proceed(request);
        });

        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(thing.class, new thingDeserializer());

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .client(builder.build())
                .build();
    }

    public static endPoint createEndPointService(Token token) {
        return retrofitClient
                .createForEndPoints(token)
                .create(endPoint.class);
    }

    public static login createLoginService() {
        return retrofitClient
                .createForAuth()
                .create(login.class);
    }
}
