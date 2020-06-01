package com.ducktapedapps.updoot.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ducktapedapps.updoot.utils.SortTimePeriod
import com.ducktapedapps.updoot.utils.Sorting
import com.ducktapedapps.updoot.utils.SubmissionUiType

/**
 *   Represents the metadata about subreddit which is to be cached to
 *   persistent storage
 *   @param viewType is user preference of submission view type on subreddit basis
 *   @param sorting is user preference default sorting on subreddit basis
 */

@Entity
data class SubredditPrefs(
        @PrimaryKey val subredditName: String,
        val viewType: SubmissionUiType,
        val sorting: Sorting,
        val sortTimePeriod: SortTimePeriod?
)