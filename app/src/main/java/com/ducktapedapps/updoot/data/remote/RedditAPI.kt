package com.ducktapedapps.updoot.data.remote

import com.ducktapedapps.updoot.data.local.model.Account
import com.ducktapedapps.updoot.data.local.model.TrendingSubredditNames
import com.ducktapedapps.updoot.data.local.moshiAdapters.Thing
import com.ducktapedapps.updoot.utils.Constants
import retrofit2.http.*

interface RedditAPI {

    @GET("/api/v1/me")
    suspend fun userIdentity(): Account

    @GET("{subreddit}/{sort}")
    suspend fun getSubreddit(
            @Path("subreddit") subreddit: String?,
            @Path("sort") sort: String,
            @Query("t") time: String?,
            @Query("after") after: String?): Thing

    @FormUrlEncoded
    @POST("/api/save")
    suspend fun save(
            @Field("id") id: String
    ): retrofit2.Response<Unit>

    @FormUrlEncoded
    @POST("/api/unsave")
    suspend fun unSave(
            @Field("id") id: String
    ): retrofit2.Response<Unit>

    @GET("r/{subreddit}/comments/{id}")
    suspend fun getComments(
            @Path("subreddit") subreddit: String,
            @Path("id") submissions_id: String
    ): List<Thing>

    @GET("/api/morechildren")
    suspend fun getMoreChildren(
            @Query("children") children: String,
            @Query("link_id") link_id: String,
            @Query("api_type") type: String = "json"
    ): Thing

    @FormUrlEncoded
    @POST("/api/vote")
    suspend fun castVote(
            @Field("id") thing_id: String,
            @Field("dir") vote_direction: Int
    ): retrofit2.Response<Unit>

    @GET("/subreddits/search")
    suspend fun search(
            @Query("q") query: String
    ): Thing

    @GET("r/{subreddit}/about")
    suspend fun getSubredditInfo(
            @Path("subreddit") subreddit: String
    ): Thing

    @GET
    suspend fun getTrendingSubredditNames(@Url fullUrl: String = Constants.TRENDING_API_URL): TrendingSubredditNames

    @GET("/subreddits/mine/subscriber")
    suspend fun getSubscribedSubreddits(
            @Query("after") after: String?
    ): Thing
}

