package com.ducktapedapps.updoot.ui.common

import com.ducktapedapps.updoot.utils.ThemeType
import kotlinx.coroutines.flow.Flow

interface IThemeManager {

    fun themeType(): Flow<ThemeType>

    suspend fun setThemeType(newType: ThemeType)

}