package com.ducktapedapps.updoot.data.local

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.ducktapedapps.updoot.data.local.model.LinkData
import kotlinx.coroutines.flow.Flow

@Dao
interface SubmissionsCacheDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(data: LinkData)

    @Query("DELETE FROM LinkData WHERE name IS :name AND subredditName IS :subreddit")
    suspend fun deleteSubmission(name: String, subreddit: String)

    @RawQuery(observedEntities = [LinkData::class])
    fun observeCachedSubmissions(query: SupportSQLiteQuery): Flow<List<LinkData>>

    @Query("SELECT * FROM LinkData")
    suspend fun getAllCachedSubmissions(): List<LinkData>

    @Query("SELECT * FROM LinkData WHERE name is :name")
    suspend fun getLinkData(name: String): LinkData

    @Query("SELECT * FROM LinkData WHERE name is :name")
    fun observeLinkData(name: String): Flow<LinkData>
}