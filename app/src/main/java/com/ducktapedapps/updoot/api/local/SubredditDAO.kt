package com.ducktapedapps.updoot.api.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducktapedapps.updoot.model.Subreddit
import kotlinx.coroutines.flow.Flow

@Dao
interface SubredditDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubreddit(subreddit: Subreddit)

    @Query("SELECT * FROM Subreddit WHERE isTrending = 1")
    fun observeTrendingSubs(): Flow<List<Subreddit>>

    @Query("SELECT * FROM Subreddit WHERE isTrending = 1")
    suspend fun getTrendingSubs(): List<Subreddit>

    @Query("SELECT * FROM Subreddit WHERE display_name IS :name")
    fun observeSubredditInfo(name: String): Flow<Subreddit?>

    @Query("SELECT * FROM Subreddit WHERE display_name LIKE  '%' || :keyword ||'%' LIMIT 30")
    fun observeSubredditWithKeyword(keyword: String): Flow<List<Subreddit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubredditSubscription)

    @Query("SELECT * FROM SubredditSubscription,Subreddit WHERE subredditName==display_name AND userName = :user")
    fun observeSubscribedSubredditsFor(user: String): Flow<List<Subreddit>>

    @Query("SELECT * FROM SubredditSubscription,Subreddit WHERE subredditName==display_name AND userName = :user")
    fun subscribedSubredditsFor(user: String): List<Subreddit>

    @Query("SELECT * FROM subreddit WHERE display_name NOT IN (SELECT subredditName from SubredditSubscription)")
    suspend fun getNonSubscribedSubreddits(): List<Subreddit>

    @Query("DELETE  FROM subreddit WHERE display_name is :name")
    suspend fun deleteSubreddit(name: String)
}