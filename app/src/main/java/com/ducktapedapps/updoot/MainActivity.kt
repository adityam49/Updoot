package com.ducktapedapps.updoot

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkManager
import com.ducktapedapps.navigation.Event
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
    private val viewModel: ActivityVM by viewModels()

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            UpdootTheme(
                isDarkTheme = when (viewModel.theme.collectAsStateWithLifecycle().value) {
                    DARK -> true
                    LIGHT -> false
                    ThemeType.AUTO -> isSystemInDarkTheme()
                }
            ) {
                HomeScreen(
                    activityViewModel = viewModel,
                    publishEvent = { viewModel.sendEvent(it) })
            }
        }

    }

    private fun processEvents(event: Event) {
        when (event) {
            is Event.OpenWebLink -> {
                val intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(event.url)
                )
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }

            is Event.ToastEvent -> Toast.makeText(
                this@MainActivity, event.content, Toast.LENGTH_SHORT
            ).show()

            else -> viewModel.sendEvent(event)

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