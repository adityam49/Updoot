package com.ducktapedapps.updoot.api;

import com.ducktapedapps.updoot.model.account;
import com.ducktapedapps.updoot.model.thing;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface redditAPI {

    @GET("/api/v1/me")
    Single<account> getUserIdentity();

    @GET("{subreddit}/{sort}")
    Single<thing> getSubreddit(
            @Path("subreddit") String subreddit,
            @Path("sort") String sort,
            @Query("t") String time,
            @Query("after") String after);

    @FormUrlEncoded
    @POST("/api/vote")
    Completable castVote(
            @Field("id") String thing_id,
            @Field("dir") int vote_direction
    );

    @FormUrlEncoded
    @POST("/api/save")
    Completable save(
            @Field("id") String id
    );

    @FormUrlEncoded
    @POST("/api/unsave")
    Completable unsave(
            @Field("id") String id
    );

    @GET("{subreddit}/comments/{id}")
    Single<thing> getComments(
            @Path("subreddit") String subreddit,
            @Path("id") String submissions_id
    );

}

