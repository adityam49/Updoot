package com.ducktapedapps.updoot.repository;

import android.app.Application;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.model.thing;

import javax.inject.Singleton;

import io.reactivex.Single;

@Singleton
public class commentsRepo {

    private static final String TAG = "submissionRepo";
    private UpdootComponent updootComponent;

    public commentsRepo(Application application) {
        updootComponent = ((UpdootApplication) application).getUpdootComponent();
    }

    public Single<thing> loadComments(String subreddit, String submission_id) {
        return updootComponent
                .getRedditAPI()
                .flatMap(redditAPI -> redditAPI.getComments(subreddit, submission_id));
    }
}

