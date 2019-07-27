package com.ducktapedapps.updoot;

import android.app.Application;
import android.content.SharedPreferences;

import com.ducktapedapps.updoot.di.ApplicationModule;
import com.ducktapedapps.updoot.di.DaggerUpdootComponent;
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.utils.constants;
import com.facebook.stetho.Stetho;

import java.util.UUID;

public class UpdootApplication extends Application {
    private UpdootComponent mUpdootComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
        getUpdootComponent().inject(this);
        createDeviceId();
    }


    private void createDeviceId() {
        SharedPreferences sharedPreferences = getUpdootComponent().getSharedPreferences();
        if (sharedPreferences.getString(constants.DEVICE_ID_KEY, null) == null) {
            sharedPreferences
                    .edit()
                    .putString(constants.DEVICE_ID_KEY, UUID.randomUUID().toString())
                    .apply();
        }
    }

    public UpdootComponent getUpdootComponent() {
        if (mUpdootComponent == null) {
            mUpdootComponent = DaggerUpdootComponent
                    .builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return mUpdootComponent;
    }
}
