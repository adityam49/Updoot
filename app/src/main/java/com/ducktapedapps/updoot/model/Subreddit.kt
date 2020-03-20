package com.ducktapedapps.updoot.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
@JsonClass(generateAdapter = true)
data class Subreddit(
        @PrimaryKey val display_name: String,
        val community_icon: String,
        val subscribers: Long,
        val active_user_count: Long,
        val public_description: String,
        val created: Long,
        val lastUpdated: Long?,
        val isTrending: Int = 0 //0 is not trending 1 is trending
) : Parcelable