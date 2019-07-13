package com.ducktapedapps.updoot.api;

import com.ducktapedapps.updoot.model.account;
import com.ducktapedapps.updoot.model.thing;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface redditAPI {

    @GET("/api/v1/me")
    Single<account> getUserIdentity();

    @GET("r/{subreddit}")
    Single<thing> getSubreddit(@Path("subreddit") String subreddit);

    @GET("{sort}")
    Single<thing> getFrontPage(
            @Path("sort") String userId,
            @Query("after") String after
    );

}

