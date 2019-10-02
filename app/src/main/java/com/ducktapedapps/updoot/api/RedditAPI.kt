package com.ducktapedapps.updoot.api

import com.ducktapedapps.updoot.model.Account
import com.ducktapedapps.updoot.model.Thing
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface RedditAPI {

    @get:GET("/api/v1/me")
    val userIdentity: Single<Account>

    @GET("{subreddit}/{sort}")
    fun getSubreddit(
            @Path("subreddit") subreddit: String,
            @Path("sort") sort: String,
            @Query("t") time: String,
            @Query("after") after: String): Single<Thing>

    @FormUrlEncoded
    @POST("/api/vote")
    fun castVote(
            @Field("id") thing_id: String,
            @Field("dir") vote_direction: Int
    ): Completable

    @FormUrlEncoded
    @POST("/api/save")
    fun save(
            @Field("id") id: String
    ): Completable

    @FormUrlEncoded
    @POST("/api/unsave")
    fun unsave(
            @Field("id") id: String
    ): Completable

    @GET("{subreddit}/comments/{id}")
    fun getComments(
            @Path("subreddit") subreddit: String,
            @Path("id") submissions_id: String
    ): Single<Thing>
}

