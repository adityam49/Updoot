package com.ducktapedapps.updoot.api.local

import androidx.room.*
import com.ducktapedapps.updoot.model.Subreddit

/**
 * Stores subreddit's metadata
 */

@Database(entities = [Subreddit::class, SubredditSubscription::class], version = 1, exportSchema = false)
abstract class SubredditDB : RoomDatabase() {
    abstract fun subredditDAO(): SubredditDAO
}

@Entity
data class SubredditSubscription(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ForeignKey(entity = Subreddit::class, parentColumns = ["subredditName"], childColumns = ["display_name"], onDelete = ForeignKey.NO_ACTION)
        val subredditName: String,
        val userName: String
)