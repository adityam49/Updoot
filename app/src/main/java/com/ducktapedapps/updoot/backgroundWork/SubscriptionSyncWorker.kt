package com.ducktapedapps.updoot.backgroundWork

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.navDrawer.UpdateUserSubscriptionUseCase
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountManager
import com.ducktapedapps.updoot.utils.accountManagement.UpdootAccountsProvider
import com.ducktapedapps.updoot.utils.createNotificationChannel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@HiltWorker
class SubscriptionSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val updateUserSubscriptionUseCase: UpdateUserSubscriptionUseCase,
    private val updootAccountManager: UpdootAccountManager,
    private val updootAccountsProvider: UpdootAccountsProvider,
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result = try {
        val accountsToSync = mutableListOf<String>().apply {
            val accountSyncRequestedFor = inputData.getString(ACCOUNT_NAME_KEY)
            this += if (accountSyncRequestedFor == null)
                findAllLoggedInUsers()
            else
                listOf(accountSyncRequestedFor)
        }
        buildNotificationAndShow(
            "Found ${accountsToSync.size} accounts",
            "names are :$accountsToSync",
            context
        )
        accountsToSync.asReversed().forEach { user ->
            updootAccountManager.setCurrentAccount(user)
            val notificationId = Random.nextInt()
            buildNotificationAndShow(
                title = "Syncing $user's subreddits",
                context = context,
                id = notificationId
            )
            val successFull = updateUserSubscriptionUseCase.updateUserSubscription(user)
            buildNotificationAndShow(
                title = if (successFull) "Finished syncing $user's subreddits" else "Error syncing $user's subreddits",
                context = context,
                id = notificationId
            )
        }
        Result.success(workDataOf(SYNC_UPDATED_COUNT_KEY to 0)) //TODO remove this
    } catch (e: Exception) {
        Result.failure()
    }

    private suspend fun findAllLoggedInUsers(): List<String> =
        updootAccountsProvider.getLoggedInUsers().first().map { it.name }

    private fun buildNotificationAndShow(
        title: String,
        message: String = "",
        context: Context,
        id: Int = Random.nextInt()
    ) {
        context.createNotificationChannel()
        val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_subreddit_default_24dp)
            .setContentTitle(title)
            .apply {
                setContentText(message)
            }
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    companion object {
        const val ACCOUNT_NAME_KEY = "account_name_key"
        const val ONE_OFF_SYNC_JOB = "one_off_sync_job"
        const val SUBSCRIPTION_SYNC_TAG = "sub_sync_tag"
        const val SYNC_UPDATED_COUNT_KEY = "sync_update_count_key"
    }
}

fun WorkManager.enqueueSubscriptionSyncWork() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) constraints.setRequiresDeviceIdle(true)
    val workRequest = PeriodicWorkRequestBuilder<SubscriptionSyncWorker>(1, TimeUnit.DAYS)
        .addTag(SubscriptionSyncWorker.SUBSCRIPTION_SYNC_TAG)
        .setConstraints(constraints.build()).build()

    enqueueUniquePeriodicWork(
        SubscriptionSyncWorker.SUBSCRIPTION_SYNC_TAG,
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}

fun WorkManager.enqueueOneOffSubscriptionsSyncFor(account: String) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
    val workRequest = OneTimeWorkRequestBuilder<SubscriptionSyncWorker>()
        .addTag(SubscriptionSyncWorker.ONE_OFF_SYNC_JOB)
        .setInputData(workDataOf(SubscriptionSyncWorker.ACCOUNT_NAME_KEY to account))
        .setConstraints(constraints.build()).build()
    enqueueUniqueWork(
        SubscriptionSyncWorker.ONE_OFF_SYNC_JOB,
        ExistingWorkPolicy.REPLACE,
        workRequest
    )
}