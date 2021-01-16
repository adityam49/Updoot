package com.ducktapedapps.updoot.data.local.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.createDataStore
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.SHOW_SINGLE_THREAD
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.SHOW_SINGLE_THREAD_COLOR
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.THEME_KEY
import com.ducktapedapps.updoot.utils.ThemeType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdootDataStore @Inject constructor(
        @ApplicationContext context: Context
) {
    private val dataStore = context.createDataStore(
            name = DATA_STORE_NAME,
    )

    fun themeType(): Flow<ThemeType> = dataStore.data
            .map { it[THEME_KEY] ?: ThemeType.AUTO.ordinal }
            .map { ThemeType.values()[it] }

    suspend fun setThemeType(newType: ThemeType) {
        dataStore.edit { it[THEME_KEY] = newType.ordinal }
    }

    fun showSingleThread(): Flow<Boolean> = dataStore.data
            .map { it[SHOW_SINGLE_THREAD] ?: true }

    suspend fun toggleSingleThread() {
        dataStore.edit { it[SHOW_SINGLE_THREAD] = !(it[SHOW_SINGLE_THREAD] ?: true) }
    }

    fun showSingleThreadColor(): Flow<Boolean> = dataStore.data
            .map { it[SHOW_SINGLE_THREAD_COLOR] ?: true }

    suspend fun toggleSingleThreadColor() {
        dataStore.edit { it[SHOW_SINGLE_THREAD_COLOR] = !(it[SHOW_SINGLE_THREAD_COLOR] ?: true) }
    }


    private object PrefKeys {
        val THEME_KEY = intPreferencesKey("theme_key")
        val SHOW_SINGLE_THREAD = booleanPreferencesKey("show_single_thread")
        val SHOW_SINGLE_THREAD_COLOR = booleanPreferencesKey("show_single_thread_color")
    }

    private companion object {
        const val DATA_STORE_NAME = "updoot_data_store"
    }
}


