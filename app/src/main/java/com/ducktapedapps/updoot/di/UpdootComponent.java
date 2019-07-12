package com.ducktapedapps.updoot.di;

import android.content.SharedPreferences;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.api.authAPI;
import com.ducktapedapps.updoot.api.redditAPI;
import com.ducktapedapps.updoot.utils.TokenInterceptor;

import javax.inject.Singleton;

import dagger.Component;
import io.reactivex.Single;

@Singleton
@Component(modules = {NetworkModule.class, ApiModule.class, ApplicationModule.class})
public interface UpdootComponent {
    authAPI getAuthAPI();

    SharedPreferences getSharedPreferences();

    void inject(UpdootApplication updootApp);

    TokenInterceptor getTokenInterceptor();

    Single<redditAPI> getRedditAPI();
}
