package com.ducktapedapps.updoot.di;

import android.util.Log;

import com.ducktapedapps.updoot.BuildConfig;
import com.ducktapedapps.updoot.model.thing;
import com.ducktapedapps.updoot.utils.TokenInterceptor;
import com.ducktapedapps.updoot.utils.constants;
import com.ducktapedapps.updoot.utils.thingDeserializer;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
class NetworkModule {
    private static final String TAG = "NetworkModule";

    @Singleton
    @Provides
    static TokenInterceptor provideTokenInterceptor() {
        return new TokenInterceptor();
    }

    @Singleton
    @Provides
    static OkHttpClient provideOkHttpClient(TokenInterceptor tokenInterceptor) {
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        okHttpClient.addNetworkInterceptor(new StethoInterceptor());
        okHttpClient.addNetworkInterceptor(tokenInterceptor);
        okHttpClient.authenticator((route, response) -> {
                    Log.i(TAG, "provideOkHttpClient: " + response.toString());
                    if (response.request().header("Authorization") != null) {
                        return null; // Give up, we've already attempted to authenticate.
                    }
                    String credential = Credentials.basic(constants.client_id, "");
                    return response.request().newBuilder()
                            .header("Authorization", credential)
                            .build();
                }
        );

        //for logging requests
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BASIC);
            okHttpClient.addInterceptor(httpLoggingInterceptor);
        }
        return okHttpClient.build();
    }

    @Reusable
    @Provides
    static Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(thing.class, new thingDeserializer());
        return gsonBuilder.create();
    }

    @Singleton
    @Provides
    static Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(constants.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();


    }
}
