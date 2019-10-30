package com.ducktapedapps.updoot.repository

import android.app.Application
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.di.UpdootComponent
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.ListingData
import com.ducktapedapps.updoot.model.Thing
import io.reactivex.Single
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
}

