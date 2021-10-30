package com.ducktapedapps.updoot.search

import com.ducktapedapps.updoot.utils.ThemeType
import kotlinx.coroutines.flow.Flow

interface SearchPrefsManager {
    fun includeNsfwSearchResults(): Flow<Boolean>

    suspend fun toggleNsfwResultsPrefs()
}