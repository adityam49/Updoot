package com.ducktapedapps.updoot.backgroundWork.cacheCleanUp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.api.local.submissionsCache.SubmissionsCacheDAO
import com.ducktapedapps.updoot.utils.createNotification
import java.util.*

class SubmissionsCacheCleanUpWorker(
        private val context: Context,
        workParas: WorkerParameters,
        private val submissionsCacheDAO: SubmissionsCacheDAO
) : CoroutineWorker(context, workParas) {
    override suspend fun doWork(): Result =
            try {
                findAndRemoveStaleSubmissions()
                Result.success()
            } catch (e: Exception) {
                showFailureReportNotification(e)
                Result.failure()
            }

    @Throws(Exception::class)
    private suspend fun findAndRemoveStaleSubmissions() {
        val currentTimeInSeconds = Calendar.getInstance().time.time / 1000
        var totalSubmissions: Int
        var submissionsRemoved = 0
        submissionsCacheDAO.getAllCachedSubmissions().apply {
            totalSubmissions = size
            forEach {
                if (isStale(it.lastUpdated ?: 0L, currentTimeInSeconds)) {
                    submissionsCacheDAO.deleteSubmission(it.id, it.subredditName)
                    submissionsRemoved++
                }
            }
        }
        showReportNotification(totalSubmissions, submissionsRemoved)
    }

    private fun showReportNotification(total: Int, removed: Int) =
            with(context) {
                createNotification(
                        resources.getString(R.string.Submission_cache_cleanup),
                        resources.getString(R.string.total_remove_submissions, total, removed, total - removed),
                        this
                )
            }

    private fun showFailureReportNotification(e: Exception) = with(context) {
        createNotification(
                resources.getString(R.string.something_went_wrong),
                e.message.toString(),
                this
        )

    }

    private fun isStale(lastUpdated: Long, currentTime: Long): Boolean =
            STALE_THRESHOLD_IN_HOURS * 60 * 60 < currentTime - lastUpdated


    private companion object {
        const val STALE_THRESHOLD_IN_HOURS = 3
    }
}
