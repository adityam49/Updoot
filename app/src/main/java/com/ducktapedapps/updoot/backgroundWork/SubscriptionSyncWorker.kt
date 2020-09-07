package com.ducktapedapps.updoot.backgroundWork

import android.accounts.AccountManager
import android.content.Context
import android.os.Build
import androidx.work.*
import com.ducktapedapps.updoot.api.local.SubredditDAO
import com.ducktapedapps.updoot.api.local.SubredditSubscription
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import com.ducktapedapps.updoot.utils.createNotification
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
        createNotification("Found ${accounts.size} accounts", "names are :$accounts", context)
        accounts.forEach { user ->
            val notificationId = Random.nextInt()
            createNotification("Syncing $user's subreddits", "Found ${getSubredditCount(user)}'s subreddits", context, notificationId)
            val subs = loadUserSubscribedSubreddits(user)
            subs.forEach { subreddit ->
                subredditDAO.apply {
                    insertSubreddit(subreddit)
                    insertSubscription(SubredditSubscription(subreddit.display_name, user))
                }
            }
            createNotification("Finished syncing $user's subreddits", "Updated ${subs.size} subreddits", context, notificationId)
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
            val fetchedSubs = mutableListOf<Subreddit>().apply {
                addAll(result.subreddits)
            }

            var after: String? = result.after
            while (after != null) {
                result = redditAPI.getSubscribedSubreddits(after)
                fetchedSubs.addAll(result.subreddits)
                after = result.after
            }
            return fetchedSubs
        } catch (e: Exception) {
            throw Exception("Unable to fetch subscription for user $user", e)
        }
    }

    private fun getSubredditCount(user: String) =
            subredditDAO.subscribedSubredditsFor(user).size

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
            .setConstraints(constraints.build()).build()

    WorkManager
            .getInstance(this)
            .enqueueUniquePeriodicWork(
                    SubscriptionSyncWorker.SUBSCRIPTION_SYNC_TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
            )
}