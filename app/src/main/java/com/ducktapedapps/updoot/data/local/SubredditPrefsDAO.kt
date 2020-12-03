package com.ducktapedapps.updoot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.SubmissionUiType
import kotlinx.coroutines.flow.Flow

@Dao
interface SubredditPrefsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubredditPrefs(subredditPrefs: SubredditPrefs)

    @Query("UPDATE SubredditPrefs SET viewType = :newViewType WHERE subreddit_name IS :subreddit")
    suspend fun setUIType(newViewType: SubmissionUiType, subreddit: String)

    @Query("UPDATE SubredditPrefs SET subredditSorting = :newSubredditSorting WHERE subreddit_name IS :subreddit ")
    suspend fun setSorting(newSubredditSorting: SubredditSorting, subreddit: String)

    @Query("SELECT * FROM SubredditPrefs WHERE subreddit_name IS :subreddit")
    fun observeSubredditPrefs(subreddit: String): Flow<SubredditPrefs?>
}