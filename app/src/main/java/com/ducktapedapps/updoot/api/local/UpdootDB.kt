package com.ducktapedapps.updoot.api.local

import androidx.room.*
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.model.SubredditPrefs
import com.ducktapedapps.updoot.utils.SubredditPrefsConverter

/**
 * Stores subreddit's metadata
 */

@TypeConverters(SubredditPrefsConverter::class)
@Database(entities = [Subreddit::class, SubredditSubscription::class, SubredditPrefs::class, LinkData::class], version = 1, exportSchema = false)
abstract class SubredditDB : RoomDatabase() {
    abstract fun subredditDAO(): SubredditDAO
    abstract fun subredditPrefsDAO(): SubredditPrefsDAO
    abstract fun submissionsCacheDAO(): SubmissionsCacheDAO
}

@Entity(primaryKeys = ["subredditName", "userName"])
data class SubredditSubscription(
        @ForeignKey(entity = Subreddit::class, parentColumns = ["subredditName"], childColumns = ["display_name"], onDelete = ForeignKey.NO_ACTION)
        val subredditName: String,
        val userName: String
)