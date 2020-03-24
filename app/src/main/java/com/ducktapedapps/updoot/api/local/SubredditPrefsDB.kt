package com.ducktapedapps.updoot.api.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ducktapedapps.updoot.model.SubredditPrefs
import com.ducktapedapps.updoot.utils.SubredditPrefsConverter

/**
 * Stores user's preferences on per subreddit basis
 */
@Database(entities = [SubredditPrefs::class], version = 1, exportSchema = false)
@TypeConverters(SubredditPrefsConverter::class)
abstract class SubredditPrefsDB : RoomDatabase() {
    abstract fun subredditPrefsDAO(): SubredditPrefsDAO
}