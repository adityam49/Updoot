package com.ducktapedapps.updoot.common

import com.ducktapedapps.updoot.utils.ThemeType
import kotlinx.coroutines.flow.Flow

interface ThemeManager {

    fun themeType(): Flow<ThemeType>

    suspend fun setThemeType(newType: ThemeType)

}