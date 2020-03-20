package com.ducktapedapps.updoot.ui.explore

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducktapedapps.updoot.model.Subreddit

@Dao
interface SubredditDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubreddit(subreddit: Subreddit)

    @Query("SELECT * FROM Subreddit WHERE isTrending = 1")
    suspend fun getTrendingSubreddits(): List<Subreddit>
}