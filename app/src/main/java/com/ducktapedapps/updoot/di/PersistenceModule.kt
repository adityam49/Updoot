package com.ducktapedapps.updoot.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.UpdootDB
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.comments.ICommentPrefManager
import com.ducktapedapps.updoot.ui.common.IThemeManager
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.Constants.UPDOOT_DB
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.accountManagement.ICurrentAccountNameManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
abstract class PersistenceModule {
    @Binds
    abstract fun bindThemeManager(dataStore: UpdootDataStore): IThemeManager

    @Binds
    abstract fun bindCurrentAccountNameManager(dataStore: UpdootDataStore): ICurrentAccountNameManager

    @Binds
    abstract fun bindCommentsPrefManager(dataStore: UpdootDataStore): ICommentPrefManager

    companion object {
        private lateinit var updootDb: UpdootDB

        @Provides
        @Singleton
        fun provideDB(@ApplicationContext context: Context): UpdootDB {
            updootDb = Room.databaseBuilder(
                    context.applicationContext,
                    UpdootDB::class.java,
                    UPDOOT_DB
            ).addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    GlobalScope.launch {
                        updootDb.subredditDAO().insertSubreddit(
                                Subreddit(
                                        display_name = FRONTPAGE,
                                        community_icon = "",
                                        subscribers = -1,
                                        active_user_count = -1,
                                        public_description = "The front page of the internet",
                                        created = 1137566705,
                                        lastUpdated = System.currentTimeMillis(),
                                        description = ""
                                )
                        )
                        updootDb.subredditPrefsDAO().insertSubredditPrefs(
                                SubredditPrefs(
                                        //reddit's api directs to frontpage if no subreddit name is specified
                                        subreddit_name = FRONTPAGE,
                                        viewType = SubmissionUiType.COMPACT,
                                        subredditSorting = SubredditSorting.Hot
                                ))
                    }
                }
            }).build()
            return updootDb
        }

        @Provides
        fun provideSubredditDAO(db: UpdootDB) = db.subredditDAO()

        @Provides
        fun provideSubredditPrefsDAO(db: UpdootDB) = db.subredditPrefsDAO()

        @Provides
        fun provideSubmissionsCacheDAO(db: UpdootDB) = db.submissionsCacheDAO()

        @Provides
        fun provideUrlMetaDataDAO(db: UpdootDB) = db.linkMetaDataCacheDAO()
    }
}