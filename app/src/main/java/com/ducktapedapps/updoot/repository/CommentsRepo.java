package com.ducktapedapps.updoot.repository;

import android.app.Application;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.model.Thing;

import javax.inject.Singleton;

import io.reactivex.Single;

@Singleton
public class CommentsRepo {

    private static final String TAG = "SubmissionRepo";
    private UpdootComponent updootComponent;

    public CommentsRepo(Application application) {
        updootComponent = ((UpdootApplication) application).getUpdootComponent();
    }

    public Single<Thing> loadComments(String subreddit, String submission_id) {
        return updootComponent
                .getRedditAPI()
                .flatMap(redditAPI -> redditAPI.getComments(subreddit, submission_id));
    }
}

