package com.ducktapedapps.updoot.repository;

import android.app.Application;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.model.thing;

import io.reactivex.Single;

public class submissionRepo {
    private static final String TAG = "submissionRepo";
    private UpdootComponent updootComponent;

    public submissionRepo(Application application) {
        updootComponent = ((UpdootApplication) application).getUpdootComponent();
    }

    public Single<thing> loadNextPage(String sort, String nextPage) {
        return updootComponent
                .getRedditAPI()
                .flatMap(redditAPI -> redditAPI.getFrontPage(sort, nextPage));
    }
}
