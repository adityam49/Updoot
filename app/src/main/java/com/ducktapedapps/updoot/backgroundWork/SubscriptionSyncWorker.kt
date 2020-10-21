package com.ducktapedapps.updoot.backgroundWork

import android.accounts.AccountManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.SubredditSubscription
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.createNotificationChannel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

class SubscriptionSyncWorker(
        workerParameters: WorkerParameters,
        private val subredditDAO: SubredditDAO,
        private val redditClient: RedditClient,
        private val context: Context
) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result = try {
        val accounts = findAllLoggedInUsers()
        buildNotificationAndShow("Found ${accounts.size} accounts", "names are :$accounts", context)
        accounts.reversed().forEach { user ->
            val notificationId = Random.nextInt()
            buildNotificationAndShow("Syncing $user's subreddits", "Found ${getSubredditCount(user)}'s subreddits", context, notificationId)
            val subs = loadUserSubscribedSubreddits(user)
            subs.forEach { subreddit ->
                subredditDAO.apply {
                    insertSubreddit(subreddit)
                    insertSubscription(SubredditSubscription(subreddit.display_name, user))
                }
            }
            buildNotificationAndShow("Finished syncing $user's subreddits", "Updated ${subs.size} subreddits", context, notificationId)
        }
        Result.success()
    } catch (e: Exception) {
        Result.failure()
    }

    private fun findAllLoggedInUsers(): List<String> =
            AccountManager.get(context).accounts.map { it.name }.filter { it != Constants.ANON_USER }


    private suspend fun loadUserSubscribedSubreddits(user: String): List<Subreddit> {
        try {
            redditClient.setCurrentAccount(user)
            val redditAPI = redditClient.api()
            var result = redditAPI.getSubscribedSubreddits(null)
            val allSubs = mutableListOf<Subreddit>().apply {
                addAll(result.children)
            }
            var after: String? = result.after
            while (after != null) {
                result = redditAPI.getSubscribedSubreddits(after)
                allSubs.addAll(result.children)
                after = result.after
            }
            return allSubs
        } catch (e: Exception) {
            throw Exception("Unable to fetch subscription for user $user", e)
        }
    }

    private fun getSubredditCount(user: String) =
            subredditDAO.subscribedSubredditsFor(user).size

    private fun buildNotificationAndShow(title: String, message: String, context: Context, id: Int = Random.nextInt()) {
        context.createNotificationChannel()
        val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_subreddit_default_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    companion object {
        const val SUBSCRIPTION_SYNC_TAG = "sub_sync_tag"
    }
}

class SubscriptionSyncWorkerFactory @Inject constructor(
        private val subredditDAO: SubredditDAO,
        private val redditClient: RedditClient
) : WorkerFactory() {
    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? =
            if (workerClassName == SubscriptionSyncWorker::class.java.name)
                SubscriptionSyncWorker(workerParameters, subredditDAO, redditClient, appContext)
            else null
}

fun Context.enqueueSubscriptionSyncWork() {
    val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) constraints.setRequiresDeviceIdle(true)
    val workRequest = PeriodicWorkRequestBuilder<SubscriptionSyncWorker>(1, TimeUnit.DAYS)
            .addTag(SubscriptionSyncWorker.SUBSCRIPTION_SYNC_TAG)
            .setConstraints(constraints.build()).build()

    WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                    SubscriptionSyncWorker.SUBSCRIPTION_SYNC_TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
            )
}