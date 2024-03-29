package com.ducktapedapps.updoot.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ducktapedapps.updoot.comments.CommentPrefManager
import com.ducktapedapps.updoot.common.ThemeManager
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.UpdootDB
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.search.SearchPrefsManager
import com.ducktapedapps.updoot.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.Constants.UPDOOT_DB
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.accountManagement.CurrentAccountNameManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
abstract class PersistenceModule {
    @Binds
    abstract fun bindThemeManager(dataStore: UpdootDataStore): ThemeManager

    @Binds
    abstract fun bindCurrentAccountNameManager(dataStore: UpdootDataStore): CurrentAccountNameManager

    @Binds
    abstract fun bindCommentsPrefManager(dataStore: UpdootDataStore): CommentPrefManager

    @Binds
    abstract fun bindSearchPrefs(dataStore: UpdootDataStore): SearchPrefsManager
    
    companion object {
        private lateinit var updootDb: UpdootDB

        @DelicateCoroutinesApi
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
                            LocalSubreddit(
                                subredditName = FRONTPAGE,
                                icon = "",
                                subscribers = -1,
                                shortDescription = "The front page of the internet",
                                created = Date(1137566705),
                                lastUpdated = Date(),
                                longDescription = ""
                            )
                        )
                        updootDb.subredditPrefsDAO().insertSubredditPrefs(
                            SubredditPrefs(
                                //reddit's api directs to frontpage if no subreddit name is specified
                                subredditName = FRONTPAGE,
                                viewType = PostViewType.COMPACT,
                                subredditSorting = SubredditSorting.Hot
                            )
                        )
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