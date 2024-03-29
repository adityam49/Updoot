package com.ducktapedapps.updoot.comments

import kotlinx.coroutines.flow.Flow

interface CommentPrefManager {

    fun showSingleThread(): Flow<Boolean>

    suspend fun toggleSingleThread()

    fun showSingleThreadColor(): Flow<Boolean>

    suspend fun toggleSingleThreadColor()

}