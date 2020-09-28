package com.ducktapedapps.updoot.data.local

import androidx.room.*
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.data.remote.LinkModel
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.SubredditPrefsConverter

@TypeConverters(SubredditPrefsConverter::class)
@Database(entities = [Subreddit::class, SubredditSubscription::class, SubredditPrefs::class, LinkData::class, LinkModel::class], version = 1, exportSchema = false)
abstract class UpdootDB : RoomDatabase() {
    abstract fun subredditDAO(): SubredditDAO
    abstract fun subredditPrefsDAO(): SubredditPrefsDAO
    abstract fun submissionsCacheDAO(): SubmissionsCacheDAO
    abstract fun linkMetaDataCacheDAO(): LinkMetaDataDAO
}

@Entity(primaryKeys = ["subredditName", "userName"])
data class SubredditSubscription(
        @ForeignKey(entity = Subreddit::class, parentColumns = ["subredditName"], childColumns = ["display_name"], onDelete = ForeignKey.NO_ACTION)
        val subredditName: String,
        val userName: String
)

@Entity
data class SubredditPrefs(
        @ForeignKey(entity = Subreddit::class, parentColumns = ["display_name"], childColumns = ["subreddit_name"], onDelete = ForeignKey.CASCADE)
        @PrimaryKey val subreddit_name: String,
        val viewType: SubmissionUiType,
        val subredditSorting: SubredditSorting
)