package com.ducktapedapps.updoot

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.lifecycle.asLiveData
import androidx.work.WorkManager
import com.ducktapedapps.updoot.backgroundWork.enqueueCleanUpWork
import com.ducktapedapps.updoot.backgroundWork.enqueueSubscriptionSyncWork
import com.ducktapedapps.updoot.home.HomeScreen
import com.ducktapedapps.updoot.theme.UpdootTheme
import com.ducktapedapps.updoot.utils.ThemeType.DARK
import com.ducktapedapps.updoot.utils.ThemeType.LIGHT
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@ExperimentalMaterialNavigationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ActivityVM by viewModels()

    @Inject lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpViewModel()
        setContent {
            UpdootTheme { HomeScreen(activityViewModel = viewModel) }
        }
    }

    private fun setUpViewModel() {
        viewModel
            .theme
            .asLiveData()
            .observe(this) { theme ->
                Log.d("MainActivity", "setUpViewModel: ${theme.name}")
                setDefaultNightMode(
                    when (theme) {
                        DARK -> MODE_NIGHT_YES
                        LIGHT -> MODE_NIGHT_NO
                        else -> MODE_NIGHT_FOLLOW_SYSTEM
                    }
                )
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        workManager.apply {
            enqueueCleanUpWork()
            enqueueSubscriptionSyncWork()
        }
    }
}