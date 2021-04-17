package com.ducktapedapps.updoot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducktapedapps.updoot.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.PostViewType
import kotlinx.coroutines.flow.Flow

@Dao
interface SubredditPrefsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubredditPrefs(subredditPrefs: SubredditPrefs)

    @Query("UPDATE SubredditPrefs SET viewType = :newViewType WHERE subredditName IS :subreddit")
    suspend fun setUIType(newViewType: PostViewType, subreddit: String)

    @Query("UPDATE SubredditPrefs SET subredditSorting = :newSubredditSorting WHERE subredditName IS :subreddit ")
    suspend fun setSorting(newSubredditSorting: SubredditSorting, subreddit: String)

    @Query("SELECT * FROM SubredditPrefs WHERE subredditName IS :subreddit")
    fun observeSubredditPrefs(subreddit: String): Flow<SubredditPrefs?>
}