package com.ducktapedapps.updoot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkManager
import com.ducktapedapps.updoot.MainActivityActions.SendEvent
import com.ducktapedapps.updoot.backgroundWork.enqueueCleanUpWork
import com.ducktapedapps.updoot.backgroundWork.enqueueSubscriptionSyncWork
import com.ducktapedapps.updoot.home.HomeScreen
import com.ducktapedapps.updoot.theme.UpdootTheme
import com.ducktapedapps.updoot.utils.ThemeType
import com.ducktapedapps.updoot.utils.ThemeType.DARK
import com.ducktapedapps.updoot.utils.ThemeType.LIGHT
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@ExperimentalMaterialNavigationApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ActivityVM by viewModels<ActivityVMImpl>()

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {


            UpdootTheme(
                isDarkTheme = when (viewModel.viewState.collectAsStateWithLifecycle().value.theme) {
                    DARK -> true
                    LIGHT -> false
                    ThemeType.AUTO -> isSystemInDarkTheme()
                }
            ) {
                HomeScreen(
                    activityViewModel = viewModel,
                    publishEvent = { viewModel.doAction(SendEvent(it)) })
            }
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