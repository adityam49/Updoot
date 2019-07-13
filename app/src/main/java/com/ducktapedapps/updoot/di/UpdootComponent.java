package com.ducktapedapps.updoot.di;

import android.content.SharedPreferences;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.api.redditAPI;
import com.ducktapedapps.updoot.ui.LoginActivity;
import com.ducktapedapps.updoot.utils.TokenInterceptor;

import javax.inject.Singleton;

import dagger.Component;
import io.reactivex.Single;

@Singleton
@Component(modules = {
        NetworkModule.class,
        ApiModule.class,
        ApplicationModule.class
})
public interface UpdootComponent {
    void inject(UpdootApplication updootApp);

    void inject(LoginActivity loginActivity);

    SharedPreferences getSharedPreferences();

    TokenInterceptor getTokenInterceptor();

    Single<redditAPI> getRedditAPI();


}

