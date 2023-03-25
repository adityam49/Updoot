package com.ducktapedapps.updoot

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.WorkManager
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.updoot.backgroundWork.enqueueCleanUpWork
import com.ducktapedapps.updoot.backgroundWork.enqueueSubscriptionSyncWork
import com.ducktapedapps.updoot.home.HomeScreen
import com.ducktapedapps.updoot.theme.UpdootTheme
import com.ducktapedapps.updoot.utils.ThemeType.DARK
import com.ducktapedapps.updoot.utils.ThemeType.LIGHT
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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
            UpdootTheme {
                HomeScreen(
                    activityViewModel = viewModel,
                    publishEvent = { viewModel.sendEvent(it) })
            }
        }
        collectViewModelFlows()
    }

    private fun collectViewModelFlows() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.theme.collect {
                        setDefaultNightMode(
                            when (it) {
                                DARK -> MODE_NIGHT_YES
                                LIGHT -> MODE_NIGHT_NO
                                else -> MODE_NIGHT_FOLLOW_SYSTEM
                            }
                        )
                    }
                }
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