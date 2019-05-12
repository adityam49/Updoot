package com.ducktapedapps.updoot.utils;

import com.ducktapedapps.updoot.BuildConfig;
import com.ducktapedapps.updoot.model.Token;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class retrofitClientGenerator {
    private static final String baseUrl = "https://oauth.reddit.com/";
    private static final String token_access_base_url = "https://www.reddit.com/api/v1/";
    private static final String client_id = "9M6Bbt1AAfnoSg";

    static Retrofit create() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //for logging requests
//        if (BuildConfig.DEBUG) {
//            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            builder.addInterceptor(httpLoggingInterceptor);
//        }

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


    public static Retrofit createForEndPoints(Token token) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        //for logging requests
//        if (BuildConfig.DEBUG) {
//            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
//            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//            builder.addInterceptor(httpLoggingInterceptor);
//        }

        builder.addInterceptor(chain -> {

            Request request = chain
                    .request()
                    .newBuilder()
                    .addHeader("Authorization", "bearer " + token.getAccess_token())
                    .build();
            return chain.proceed(request);
        });


        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();
    }
}
