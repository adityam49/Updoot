package com.ducktapedapps.updoot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import kotlinx.coroutines.flow.Flow

@Dao
interface SubredditDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubreddit(subreddit: LocalSubreddit)

    @Query("SELECT * FROM LocalSubreddit WHERE subredditName IS :name")
    fun observeSubredditInfo(name: String): Flow<LocalSubreddit?>

    @Query("SELECT * FROM LocalSubreddit WHERE subredditName LIKE  '%' || :keyword ||'%' LIMIT 30")
    fun observeSubredditWithKeyword(keyword: String): Flow<List<LocalSubreddit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubredditSubscription)

    @Query("SELECT LocalSubreddit.* FROM LocalSubreddit JOIN SubredditSubscription ON SubredditSubscription.subredditName = LocalSubreddit.subredditName WHERE userName == :user")
    fun observeSubscribedSubredditsFor(user: String): Flow<List<LocalSubreddit>>

    @Query("DELETE FROM SubredditSubscription WHERE userName = :user")
    suspend fun deleteUserSubscriptions(user: String)

    @Query("SELECT COUNT(*) FROM SubredditSubscription WHERE userName = :user")
    suspend fun getSubscriptionCountFor(user: String): Int

    @Query("SELECT * FROM LocalSubreddit WHERE subredditName NOT IN (SELECT subredditName from SubredditSubscription)")
    suspend fun getNonSubscribedSubreddits(): List<LocalSubreddit>

    @Query("SELECT * FROM SubredditSubscription WHERE subredditName = :subredditName AND userName = :currentUser")
    fun observeSubredditSubscription(
        subredditName: String,
        currentUser: String
    ): Flow<SubredditSubscription?>

    @Query("DELETE FROM SubredditSubscription WHERE subredditName = :subredditName AND userName = :currentUser")
    suspend fun deleteSubscription(subredditName: String, currentUser: String)

    @Query("DELETE  FROM LocalSubreddit WHERE subredditName is :name")
    suspend fun deleteSubreddit(name: String)

    @Query("SELECT LocalSubreddit.* FROM LocalSubreddit JOIN TrendingSubreddit ON LocalSubreddit.subredditName == TrendingSubreddit.id")
    fun observeTrendingSubreddits(): Flow<List<LocalSubreddit>>

    @Query("DELETE FROM TrendingSubreddit")
    suspend fun removeAllTrendingSubs()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrendingSubreddit(subreddit: TrendingSubreddit)
}