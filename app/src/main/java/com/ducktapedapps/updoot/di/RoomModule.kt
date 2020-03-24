package com.ducktapedapps.updoot.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ducktapedapps.updoot.api.local.SubredditDB
import com.ducktapedapps.updoot.api.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.api.local.SubredditPrefsDB
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.model.SubredditPrefs
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.Constants.SUBREDDIT_DB
import com.ducktapedapps.updoot.utils.Constants.SUBREDDIT_PREFS_DB
import com.ducktapedapps.updoot.utils.Sorting
import com.ducktapedapps.updoot.utils.SubmissionUiType
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Singleton


@Module
class RoomModule {

    private lateinit var subredditDb: SubredditDB
    private lateinit var subredditPrefsDb: SubredditPrefsDB

    @Singleton
    @Provides
    fun provideSubredditPrefsDB(application: Application): SubredditPrefsDB {
        subredditPrefsDb = Room.databaseBuilder(
                application,
                SubredditPrefsDB::class.java,
                SUBREDDIT_PREFS_DB
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                //TODO : find a way to inject a viewModelScope into dagger ??
                GlobalScope.launch {
                    if (db is SubredditPrefsDB)
                        subredditPrefsDb.subredditPrefsDAO().insertSubredditPrefs(
                                SubredditPrefs(
                                        //reddit's api directs to frontpage if no subreddit name is specified
                                        subredditName = FRONTPAGE,
                                        viewType = SubmissionUiType.COMPACT,
                                        sorting = Sorting.HOT
                                ))
                }
            }

        })
                .build()
        return subredditPrefsDb
    }

    @Provides
    fun providesSubredditPrefsDAO(db: SubredditPrefsDB): SubredditPrefsDAO = db.subredditPrefsDAO()

    @Provides
    @Singleton
    fun provideSubredditDB(application: Application): SubredditDB {
        subredditDb = Room.databaseBuilder(
                application,
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
                                    isTrending = 0
                            )
                    )
                }
            }
        }).build()
        return subredditDb
    }

    @Provides
    fun provideSubredditDAO(db: SubredditDB) = db.subredditDAO()

}