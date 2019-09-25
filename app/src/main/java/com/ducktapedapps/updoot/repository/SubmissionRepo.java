package com.ducktapedapps.updoot.repository;

import android.app.Application;
import android.util.Log;

import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.di.UpdootComponent;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.model.Thing;

import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Single;

@Singleton
public class SubmissionRepo {
    private static final String TAG = "SubmissionRepo";
    private UpdootComponent updootComponent;

    public SubmissionRepo(Application application) {
        updootComponent = ((UpdootApplication) application).getUpdootComponent();
    }

    public Single<Thing> loadNextPage(String subreddit, String sort, String time, String nextPage) {
        return updootComponent
                .getRedditAPI()
                .flatMap(redditAPI -> redditAPI.getSubreddit(subreddit, sort, time, nextPage));
    }

    public Completable save(LinkData linkData) {
        if (!linkData.getSaved()) {
            return updootComponent
                    .getRedditAPI()
                    .flatMapCompletable(redditAPI -> redditAPI.save(linkData.getName()));

        }
        return updootComponent
                .getRedditAPI()
                .flatMapCompletable(redditAPI -> redditAPI.unsave(linkData.getName()));

    }

    public Completable castVote(LinkData linkData, int direction) {
        return updootComponent
                .getRedditAPI()
                .flatMapCompletable(redditAPI -> {
                    Boolean likes = linkData.getLikes();
                    String id = linkData.getName();
                    Log.i(TAG, "castVote: " + likes + " id " + id + " dir " + direction);
                    switch (direction) {
                        case 1:
                            if (likes == null || !likes) return redditAPI.castVote(id, 1);
                            else return redditAPI.castVote(id, 0);
                        case -1:
                            if (likes == null || likes) return redditAPI.castVote(id, -1);
                            else return redditAPI.castVote(id, 0);
                    }
                    return redditAPI.castVote(id, direction);
                });
    }
}
