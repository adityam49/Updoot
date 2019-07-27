package com.ducktapedapps.updoot.repository;

import android.app.Application;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.model.thing;

import javax.inject.Singleton;

import io.reactivex.Single;

@Singleton
public class submissionRepo {
    private static final String TAG = "submissionRepo";
    private UpdootComponent updootComponent;

    public submissionRepo(Application application) {
        updootComponent = ((UpdootApplication) application).getUpdootComponent();
    }

    public Single<thing> loadNextPage(String subReddit, String sort, String nextPage) {
        return updootComponent
                .getRedditAPI()
                .flatMap(redditAPI -> redditAPI.getSubreddit(subReddit, sort, nextPage));
    }
}
