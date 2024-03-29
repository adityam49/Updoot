package com.ducktapedapps.updoot.backgroundWork

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.data.local.model.Post
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.createNotificationChannel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@HiltWorker
class CacheCleanUpWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workParas: WorkerParameters,
    private val postCacheDAO: PostDAO,
    private val subredditDAO: SubredditDAO,
) : CoroutineWorker(context, workParas) {
    override suspend fun doWork(): Result =
        try {
            findAndRemoveStaleSubmissions()
            findAndRemoveStaleSubreddits()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            showFailureReportNotification(e)
            Result.failure()
        }

    @Throws(Exception::class)
    private suspend fun findAndRemoveStaleSubmissions() {
        var totalSubmissions: Int
        var submissionsRemoved: Int
        postCacheDAO.getCachedPosts().also { totalSubmissions = it.size }
            .filter { it.isStale() }
            .also { submissionsRemoved = it.size }
            .forEach { postCacheDAO.deletePost(it.id, it.subredditName) }
        showSubmissionsRemovedNotification(totalSubmissions, submissionsRemoved)
    }

    private fun showSubmissionsRemovedNotification(total: Int, removed: Int) =
        with(context) {
            buildNotificationAndShow(
                resources.getString(R.string.Submission_cache_cleanup),
                resources.getString(
                    R.string.total_remove_submissions,
                    total,
                    removed,
                    total - removed
                ),
                this
            )
        }

    private fun Post.isStale(): Boolean =
        SUBMISSION_STALE_THRESHOLD_IN_HOURS < TimeUnit.HOURS.toHours(Date().time - lastUpdated.time)

    private suspend fun findAndRemoveStaleSubreddits() {
        var subredditsRemovedCount: Int
        subredditDAO.getNonSubscribedSubreddits()
            .filter { it.isStale() && it.subredditName != FRONTPAGE }
            .also { subredditsRemovedCount = it.size }
            .forEach { subredditDAO.deleteSubreddit(it.subredditName) }
        showSubredditsRemovedNotification(subredditsRemovedCount)
    }

    private fun showSubredditsRemovedNotification(count: Int) {
        with(context) {
            buildNotificationAndShow(
                getString(R.string.subreddit_cache_removed),
                getString(R.string.subreddit_removed_count, count),
                context
            )
        }
    }

    private fun LocalSubreddit?.isStale(): Boolean {
        return this == null ||
                SUBREDDIT_STALE_THRESHOLD_IN_HOURS < TimeUnit.HOURS.toHours(Date().time - lastUpdated.time)
    }

    private fun showFailureReportNotification(e: Exception) = with(context) {
        buildNotificationAndShow(
            resources.getString(R.string.something_went_wrong),
            e.message.toString(),
            this
        )
    }

    private fun buildNotificationAndShow(
        title: String,
        message: String,
        context: Context,
        id: Int = Random.nextInt()
    ) {
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
        const val CACHED_SUBMISSIONS_CLEANUP_WORKER_TAG = "cached_submissions_cleanup_worker_tag"
        private const val SUBREDDIT_STALE_THRESHOLD_IN_HOURS = 48
        private const val SUBMISSION_STALE_THRESHOLD_IN_HOURS = 6
    }
}

fun WorkManager.enqueueCleanUpWork() {
    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) constraints.setRequiresDeviceIdle(true)

    val workRequest = PeriodicWorkRequestBuilder<CacheCleanUpWorker>(1, TimeUnit.DAYS)
        .addTag(CacheCleanUpWorker.CACHED_SUBMISSIONS_CLEANUP_WORKER_TAG)
        .setConstraints(constraints.build())
        .build()
    enqueueUniquePeriodicWork(
        CacheCleanUpWorker.CACHED_SUBMISSIONS_CLEANUP_WORKER_TAG,
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}