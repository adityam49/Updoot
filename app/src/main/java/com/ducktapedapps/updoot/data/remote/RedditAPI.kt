package com.ducktapedapps.updoot.data.remote

import com.ducktapedapps.updoot.data.remote.model.*
import com.ducktapedapps.updoot.data.remote.moshiAdapters.InconsistentApiResponse
import com.ducktapedapps.updoot.utils.Constants
import retrofit2.http.*

interface RedditAPI {

    @GET("/api/v1/me")
    suspend fun userIdentity(): Account

    @GET("{subreddit}/{sort}")
    suspend fun getSubmissions(
        @Path("subreddit") subreddit: String?,
        @Path("sort") sort: String,
        @Query("t") time: String?,
        @Query("after") after: String? = null
    ): Listing<LinkData>

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
    ): List<Listing<RedditThing>>

    @GET("/api/morechildren")
    @InconsistentApiResponse
    suspend fun getMoreChildren(
        @Query("api_type") api_type: String = "json",
        @Query("link_id") link_id: String,
        @Query("children") children: String
    ): Listing<Comment>

    @FormUrlEncoded
    @POST("/api/vote")
    suspend fun castVote(
        @Field("id") thing_id: String,
        @Field("dir") vote_direction: Int
    ): retrofit2.Response<Unit>

    @GET("/subreddits/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("include_over_18") includeOver18: Boolean = false,
    ): Listing<RemoteSubreddit>

    @GET("r/{subreddit}/about")
    suspend fun getSubredditInfo(
        @Path("subreddit") subreddit: String,
    ): RemoteSubreddit

    @POST("/api/subscribe")
    suspend fun subscribe(
        @Query("action") action: String,
        @Query("sr_name") subredditName: String,
    ): retrofit2.Response<Unit>

    @GET("/subreddits/mine/subscriber")
    suspend fun getSubscribedSubreddits(
        @Query("after") after: String? = null,
    ): Listing<RemoteSubreddit>

    @GET("user/{username}/overview")
    suspend fun getUserOverView(
        @Path("username") username: String,
        @Query("after") after: String? = null
    ): Listing<RedditThing>

    @GET("user/{username}/submitted")
    suspend fun getUserSubmittedPosts(
        @Path("username") username: String,
        @Query("after") after: String? = null
    ): Listing<LinkData>

    @GET("user/{username}/comments")
    suspend fun getUserComments(
        @Path("username") username: String,
        @Query("after") after: String? = null
    ): Listing<Comment.CommentData>

    @GET("user/{username}/upvoted")
    suspend fun getUserUpVotedThings(
        @Path("username") username: String,
        @Query("after") after: String? = null
    ): Listing<RedditThing>

    @GET("user/{username}/downvoted")
    suspend fun getUserDownVotedThings(
        @Path("username") username: String,
        @Query("after") after: String? = null
    ): Listing<RedditThing>

    @GET("user/{username}/gilded")
    suspend fun getUserGildedThings(
        @Path("username") username: String,
        @Query("after") after: String? = null
    ): Listing<RedditThing>

    @GET("user/{username}/saved")
    suspend fun getUserSavedThings(
        @Path("username") username: String,
        @Query("after") after: String? = null
    ): Listing<RedditThing>

    @GET("api/v1/user/{username}/trophies")
    suspend fun getUserTrophies(
        @Path("username") userName: String,
    ): TrophyList

    @GET("api/multi/mine?expand_srs=true")
    suspend fun getUserMultiReddits(): List<LabeledMulti>
}

