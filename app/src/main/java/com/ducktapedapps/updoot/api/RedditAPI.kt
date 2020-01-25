package com.ducktapedapps.updoot.api

import com.ducktapedapps.updoot.model.Account
import com.ducktapedapps.updoot.model.CommentListing
import com.ducktapedapps.updoot.model.SearchListing
import com.ducktapedapps.updoot.model.SubmissionListing
import retrofit2.http.*

interface RedditAPI {

    @GET("/api/v1/me")
    suspend fun userIdentity(): Account

    @GET("{subreddit}/{sort}")
    suspend fun getSubreddit(
            @Path("subreddit") subreddit: String?,
            @Path("sort") sort: String,
            @Query("t") time: String?,
            @Query("after") after: String?): SubmissionListing

    @FormUrlEncoded
    @POST("/api/save")
    suspend fun save(
            @Field("id") id: String
    ): String

    @FormUrlEncoded
    @POST("/api/unsave")
    suspend fun unsave(
            @Field("id") id: String
    ): String

    @GET("{subreddit}/comments/{id}")
    suspend fun getComments(
            @Path("subreddit") subreddit: String,
            @Path("id") submissions_id: String
    ): CommentListing

    @GET("/api/morechildren")
    suspend fun getMoreChildren(
            @Query("children") children: String,
            @Query("link_id") link_id: String,
            @Query("api_type") type: String = "json"
    ): String

    @FormUrlEncoded
    @POST("/api/vote")
    suspend fun castVote(
            @Field("id") thing_id: String,
            @Field("dir") vote_direction: Int
    ): String

    @GET("/subreddits/search")
    suspend fun search(
            @Query("q") query: String
    ): SearchListing?
}

