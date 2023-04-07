package com.ducktapedapps.updoot.data.local

import androidx.room.*
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.data.remote.LinkModel
import com.ducktapedapps.updoot.subreddit.SubredditSorting
import com.ducktapedapps.updoot.utils.DateConverter
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.SubredditPrefsConverter

@TypeConverters(SubredditPrefsConverter::class, DateConverter::class)
@Database(
    entities = [
        LocalSubreddit::class,
        SubredditSubscription::class,
        SubredditPrefs::class,
        Post::class,
        LinkModel::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class UpdootDB : RoomDatabase() {
    abstract fun subredditDAO(): SubredditDAO
    abstract fun subredditPrefsDAO(): SubredditPrefsDAO
    abstract fun submissionsCacheDAO(): PostDAO
    abstract fun linkMetaDataCacheDAO(): LinkMetaDataDAO
}

@Entity(
    primaryKeys = ["subredditName", "userName"],
    foreignKeys = [
        ForeignKey(
            entity = LocalSubreddit::class,
            parentColumns = ["subredditName"],
            childColumns = ["subredditName"],
            onDelete = ForeignKey.NO_ACTION
        )
    ]
)
data class SubredditSubscription(
    val subredditName: String,
    val userName: String,
)

@Entity
data class SubredditPrefs(
    @PrimaryKey val subredditName: String,
    val viewType: PostViewType,
    val subredditSorting: SubredditSorting,
) {
    constructor() : this(
        subredditName = "",
        viewType = PostViewType.LARGE,
        subredditSorting = SubredditSorting.Hot
    )
}