package com.ducktapedapps.updoot.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.SubmissionUiType

@Entity
data class SubredditPrefs(
        @PrimaryKey val subredditName: String,
        val viewType: SubmissionUiType,
        val subredditSorting: SubredditSorting
)