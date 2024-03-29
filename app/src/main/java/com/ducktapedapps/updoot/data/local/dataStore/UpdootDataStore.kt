package com.ducktapedapps.updoot.data.local.dataStore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ducktapedapps.updoot.comments.CommentPrefManager
import com.ducktapedapps.updoot.common.ThemeManager
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.CURRENT_ACCOUNT_NAME
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.DEVICE_ID
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.SHOW_SINGLE_THREAD
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.SHOW_SINGLE_THREAD_COLOR
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.THEME_KEY
import com.ducktapedapps.updoot.data.local.dataStore.UpdootDataStore.PrefKeys.showNsfwResults
import com.ducktapedapps.updoot.search.SearchPrefsManager
import com.ducktapedapps.updoot.utils.Constants.ANON_USER
import com.ducktapedapps.updoot.utils.ThemeType
import com.ducktapedapps.updoot.utils.accountManagement.CurrentAccountNameManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdootDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : CurrentAccountNameManager,
    ThemeManager,
    CommentPrefManager,
    SearchPrefsManager {
    private val Context.dataStore by preferencesDataStore(
        name = DATA_STORE_NAME
    )

    override fun themeType(): Flow<ThemeType> = context.dataStore.data
        .map { it[THEME_KEY] ?: ThemeType.AUTO.ordinal }
        .map { ThemeType.values()[it] }

    override suspend fun setThemeType(newType: ThemeType) {
        context.dataStore.edit { it[THEME_KEY] = newType.ordinal }
    }

    override fun showSingleThread(): Flow<Boolean> = context.dataStore.data
        .map { it[SHOW_SINGLE_THREAD] ?: true }

    override suspend fun toggleSingleThread() {
        context.dataStore.edit { it[SHOW_SINGLE_THREAD] = !(it[SHOW_SINGLE_THREAD] ?: true) }
    }

    override fun showSingleThreadColor(): Flow<Boolean> = context.dataStore.data
        .map { it[SHOW_SINGLE_THREAD_COLOR] ?: true }

    override suspend fun toggleSingleThreadColor() {
        context.dataStore.edit {
            it[SHOW_SINGLE_THREAD_COLOR] = !(it[SHOW_SINGLE_THREAD_COLOR] ?: true)
        }
    }

    override fun currentAccountName(): Flow<String> = context.dataStore.data.map {
        it[CURRENT_ACCOUNT_NAME] ?: ANON_USER
    }

    override fun deviceId(): Flow<String> = context.dataStore
        .data
        .transform {
            val deviceId = it[DEVICE_ID]
            if (deviceId == null) context.dataStore.edit { prefs ->
                prefs[DEVICE_ID] = UUID.randomUUID().toString()
            }
            else emit(deviceId)
        }

    override suspend fun setCurrentAccountName(user: String) {
        context.dataStore.edit {
            it[CURRENT_ACCOUNT_NAME] = user
        }
    }

    private object PrefKeys {
        val showNsfwResults = booleanPreferencesKey("show_nsfw_results")
        val DEVICE_ID = stringPreferencesKey("device_id")
        val CURRENT_ACCOUNT_NAME = stringPreferencesKey("currentAccount_key")
        val THEME_KEY = intPreferencesKey("theme_key")
        val SHOW_SINGLE_THREAD = booleanPreferencesKey("show_single_thread")
        val SHOW_SINGLE_THREAD_COLOR = booleanPreferencesKey("show_single_thread_color")
    }

    private companion object {
        const val DATA_STORE_NAME = "updoot_data_store"
    }

    override fun includeNsfwSearchResults(): Flow<Boolean> = context
        .dataStore
        .data
        .transform {
            val currentValue = it[showNsfwResults]
            if (currentValue == null) context.dataStore.edit { prefs ->
                prefs[showNsfwResults] = false
            } else {
                emit(currentValue)
            }
        }

    override suspend fun toggleNsfwResultsPrefs() {
        context.dataStore.edit { prefs ->
            prefs[showNsfwResults] = prefs[showNsfwResults] != true
        }
    }
}


