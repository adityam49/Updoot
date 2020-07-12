package com.ducktapedapps.updoot.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.SubmissionUiType

@Entity
data class SubredditPrefs(
        @ForeignKey(entity = Subreddit::class, parentColumns = ["display_name"], childColumns = ["subreddit_name"], onDelete = ForeignKey.CASCADE)
        @PrimaryKey val subreddit_name: String,
        val viewType: SubmissionUiType,
        val subredditSorting: SubredditSorting
)