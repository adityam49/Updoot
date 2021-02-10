package com.ducktapedapps.updoot.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ducktapedapps.updoot.data.remote.LinkModel
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkMetaDataDAO {
    @Insert
    suspend fun insertUrlMetaData(linkModel: LinkModel)

    @Query("SELECT * FROM LinkModel where url is :url")
    suspend fun getUrlMetaData(url: String): LinkModel?

    @Query("SELECT * FROM LinkModel where url is :url")
    fun observeUrlMetaData(url: String): Flow<LinkModel?>
}