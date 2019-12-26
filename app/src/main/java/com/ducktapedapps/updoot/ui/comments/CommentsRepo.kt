package com.ducktapedapps.updoot.ui.comments

import android.app.Application
import android.util.Log
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.di.UpdootComponent
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.ListingData
import com.ducktapedapps.updoot.model.Thing
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Singleton

@Singleton
class CommentsRepo(application: Application) {
    private val updootComponent: UpdootComponent = (application as UpdootApplication).updootComponent

    fun loadComments(subreddit: String, submission_id: String): Single<List<CommentData>> {
        return updootComponent
                .redditAPI
                .flatMap { redditAPI -> redditAPI.getComments(subreddit, submission_id) }
                .map { t: Thing ->
                    val fetchedComments = mutableListOf<CommentData>()
                    if (t.data is ListingData)
                        if (t.data.children.isNotEmpty())
                            for (child: Thing in t.data.children) {
                                if (child != null)
                                    fetchedComments.add(child.data as CommentData)
                            }
                    fetchedComments
                }
    }

    suspend fun castVote(comment: CommentData, direction: Int) {
        withContext(Dispatchers.IO) {
            try {
                val result = updootComponent.redditAPI.blockingGet()?.castVoteCoroutine(comment.name, direction)
                if (result?.string() != null && result.string() == "{}") {
                    Log.i(this.javaClass.simpleName, "casting vote : success ${result.string()}")
                } else {
                    Log.i(this.javaClass.simpleName, "casting vote : fail ${result?.string()}")

                }
            } catch (exception: Exception) {
                Log.e(this.javaClass.simpleName, "could not cast vote : ", exception)
            }
        }
    }
}

