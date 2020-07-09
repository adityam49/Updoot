package com.ducktapedapps.updoot.api.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducktapedapps.updoot.model.Subreddit

@Dao
interface SubredditDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubreddit(subreddit: Subreddit)

    @Query("SELECT * FROM Subreddit WHERE isTrending = 1")
    fun observeTrendingSubs(): LiveData<List<Subreddit>>

    @Query("SELECT * FROM Subreddit WHERE isTrending = 1")
    suspend fun getTrendingSubs(): List<Subreddit>

    @Query("SELECT * FROM Subreddit WHERE display_name IS :name")
    suspend fun getSubreddit(name: String): Subreddit?

    @Query("SELECT * FROM Subreddit WHERE display_name LIKE  '%' || :keyword ||'%' ORDER BY  subscribers DESC")
    fun observeSubredditWithKeyword(keyword: String): LiveData<List<Subreddit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubredditSubscription)

    @Query("SELECT * FROM SubredditSubscription,Subreddit WHERE subredditName==display_name AND userName = :user")
    fun observeSubscribedSubredditsFor(user: String): LiveData<List<Subreddit>>
}