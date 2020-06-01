package com.ducktapedapps.updoot.api.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ducktapedapps.updoot.model.SubredditPrefs
import com.ducktapedapps.updoot.utils.SortTimePeriod
import com.ducktapedapps.updoot.utils.Sorting
import com.ducktapedapps.updoot.utils.SubmissionUiType

@Dao
interface SubredditPrefsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSubredditPrefs(subredditPrefs: SubredditPrefs)

    @Query("SELECT * FROM SubredditPrefs WHERE subredditName IS :subreddit")
    suspend fun getSubredditPrefs(subreddit: String): SubredditPrefs?

    @Query("UPDATE SubredditPrefs SET viewType = :newViewType WHERE subredditName IS :subreddit")
    suspend fun setUIType(newViewType: SubmissionUiType, subreddit: String)

    @Query("UPDATE SubredditPrefs SET sorting = :newSorting,  sortTimePeriod = :sortPeriod WHERE subredditName IS :subreddit ")
    suspend fun setSorting(newSorting: Sorting, sortPeriod: SortTimePeriod?, subreddit: String)

    @Query("SELECT sorting FROM SubredditPrefs WHERE subredditName IS :subreddit ")
    suspend fun getSubredditSorting(subreddit: String): Sorting

    @Query("SELECT sortTimePeriod FROM SubredditPrefs WHERE subredditName IS :subreddit ")
    suspend fun getSubredditSortPeriod(subreddit: String): SortTimePeriod?
}