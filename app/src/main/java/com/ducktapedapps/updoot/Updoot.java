package com.ducktapedapps.updoot;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class Updoot extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
