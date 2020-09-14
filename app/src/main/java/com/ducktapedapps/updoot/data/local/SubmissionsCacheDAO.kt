package com.ducktapedapps.updoot.data.local

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.ducktapedapps.updoot.data.local.model.LinkData
import kotlinx.coroutines.flow.Flow

@Dao
interface SubmissionsCacheDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(data: LinkData)

    @Query("DELETE FROM LinkData WHERE id IS :id AND subredditName IS :subreddit")
    suspend fun deleteSubmission(id: String, subreddit: String)

    @RawQuery(observedEntities = [LinkData::class])
    fun observeCachedSubmissions(query: SupportSQLiteQuery): Flow<List<LinkData>>

    @Query("SELECT * FROM LinkData")
    suspend fun getAllCachedSubmissions(): List<LinkData>

    @Query("SELECT * FROM LinkData WHERE id is :id")
    suspend fun getLinkData(id: String): LinkData

    @Query("SELECT * FROM LinkData WHERE id is :id")
    fun observeLinkData(id: String): Flow<LinkData>
}