package com.ducktapedapps.updoot.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.ducktapedapps.updoot.utils.constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private Application mApplication;

    public ApplicationModule(Application application) {
        this.mApplication = application;
    }

    @Provides
    @Singleton
    Application application() {
        return mApplication;
    }

    @Singleton
    @Provides
    SharedPreferences provideSharedPreferences() {
        return mApplication.getSharedPreferences(constants.TOKEN_SHARED_PREFS_KEY, Context.MODE_PRIVATE);
    }

}
