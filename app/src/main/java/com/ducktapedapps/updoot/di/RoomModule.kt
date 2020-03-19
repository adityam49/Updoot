package com.ducktapedapps.updoot.di

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ducktapedapps.updoot.api.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.model.SubredditPrefs
import com.ducktapedapps.updoot.ui.subreddit.SubredditPrefsDB
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.Constants.SUBREDDIT_METADATA_DB
import com.ducktapedapps.updoot.utils.Sorting
import com.ducktapedapps.updoot.utils.SubmissionUiType
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Singleton


@Module
class RoomModule {

    @Singleton
    @Provides
    fun providesRoomDatabase(application: Application): SubredditPrefsDB = Room.databaseBuilder(
                    application,
                    SubredditPrefsDB::class.java,
                    SUBREDDIT_METADATA_DB
            ).addCallback(SubredditPrefsCallback())
            .build()

    /**
     * Callback used to initialize the default frontpage preferences for the first app install
     */
    private class SubredditPrefsCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            //TODO : find a way to inject a viewModelScope into dagger ??
            GlobalScope.launch {
                if (db is SubredditPrefsDB)
                    db.subredditPrefsDAO().insertSubredditPrefs(
                            SubredditPrefs(
                                    //reddit's api directs to frontpage if no subreddit name is specified
                                    subredditName = FRONTPAGE,
                                    viewType = SubmissionUiType.COMPACT,
                                    sorting = Sorting.HOT
                            ))
            }
        }
    }

    @Provides
    @Singleton
    fun providesProductDao(db: SubredditPrefsDB): SubredditPrefsDAO = db.subredditPrefsDAO()
}