package com.ducktapedapps.updoot.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ducktapedapps.updoot.data.local.SubredditDB
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.Constants.SUBREDDIT_DB
import com.ducktapedapps.updoot.utils.SubmissionUiType
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Singleton


@Module
class RoomModule {
    private lateinit var subredditDb: SubredditDB

    @Provides
    @Singleton
    fun provideDB(context: Context): SubredditDB {
        subredditDb = Room.databaseBuilder(
                context.applicationContext,
                SubredditDB::class.java,
                SUBREDDIT_DB
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                GlobalScope.launch {
                    subredditDb.subredditDAO().insertSubreddit(
                            Subreddit(
                                    display_name = FRONTPAGE,
                                    community_icon = "",
                                    subscribers = -1,
                                    active_user_count = -1,
                                    public_description = "The front page of the internet",
                                    created = 1137566705,
                                    lastUpdated = System.currentTimeMillis(),
                                    isTrending = 0,
                                    description = ""
                            )
                    )
                    subredditDb.subredditPrefsDAO().insertSubredditPrefs(
                            SubredditPrefs(
                                    //reddit's api directs to frontpage if no subreddit name is specified
                                    subreddit_name = FRONTPAGE,
                                    viewType = SubmissionUiType.COMPACT,
                                    subredditSorting = SubredditSorting.Hot
                            ))
                }
            }
        }).build()
        return subredditDb
    }

    @Provides
    fun provideSubredditDAO(db: SubredditDB) = db.subredditDAO()

    @Provides
    fun provideSubredditPrefsDAO(db: SubredditDB) = db.subredditPrefsDAO()

    @Provides
    fun provideSubmissionsCacheDAO(db: SubredditDB) = db.submissionsCacheDAO()
}