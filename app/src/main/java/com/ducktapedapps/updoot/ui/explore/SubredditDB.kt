package com.ducktapedapps.updoot.ui.explore

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ducktapedapps.updoot.model.Subreddit

/**
 * Stores subreddit's metadata
 */

@Database(entities = [Subreddit::class], version = 1, exportSchema = false)
abstract class SubredditDB : RoomDatabase() {
    abstract fun subredditDAO(): SubredditDAO
}