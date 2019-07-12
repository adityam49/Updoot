package com.ducktapedapps.updoot.di;

import com.ducktapedapps.updoot.BuildConfig;
import com.ducktapedapps.updoot.api.authAPI;
import com.ducktapedapps.updoot.api.redditAPI;
import com.ducktapedapps.updoot.model.Token;
import com.ducktapedapps.updoot.model.thing;
import com.ducktapedapps.updoot.utils.TokenDeserializer;
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
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
class NetworkModule {
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

        //for logging requests
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.level(HttpLoggingInterceptor.Level.BODY);
            okHttpClient.addInterceptor(httpLoggingInterceptor);
        }
        return okHttpClient.build();
    }

    @Reusable
    @Provides
    static Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Token.class, new TokenDeserializer())
                .registerTypeAdapter(thing.class, new thingDeserializer());
        return gsonBuilder.create();
    }

    @Singleton
    @Provides
    static Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(constants.baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();


    }
}
