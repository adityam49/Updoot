package com.ducktapedapps.updoot.di;

import com.ducktapedapps.updoot.BuildConfig;
import com.ducktapedapps.updoot.model.CommentData;
import com.ducktapedapps.updoot.model.Thing;
import com.ducktapedapps.updoot.utils.CommentDeserializer;
import com.ducktapedapps.updoot.utils.Constants;
import com.ducktapedapps.updoot.utils.ThingDeserializer;
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
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
        okHttpClient.addNetworkInterceptor(tokenInterceptor);


        okHttpClient.addNetworkInterceptor(chain -> {
            //as per reddit api guidelines to include proper user agent
            String userAgent = "android:com.ducktapedapps.updoot:" + BuildConfig.VERSION_NAME + " (by /u/nothoneypot)";
            Request request = chain.request().newBuilder().addHeader("User-Agent", userAgent).build();
            return chain.proceed(request);
        });

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
                .registerTypeAdapter(Thing.class, new ThingDeserializer())
                .registerTypeAdapter(CommentData.class, new CommentDeserializer());
        return gsonBuilder.create();
    }

    @Singleton
    @Provides
    static Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
    }
}
