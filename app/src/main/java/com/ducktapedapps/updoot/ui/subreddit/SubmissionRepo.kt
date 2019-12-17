package com.ducktapedapps.updoot.ui.subreddit

import android.app.Application
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.di.UpdootComponent
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.ListingData
import com.ducktapedapps.updoot.model.Thing
import io.reactivex.Completable
import io.reactivex.Single

class SubmissionRepo(application: Application) {
    private val updootComponent: UpdootComponent = (application as UpdootApplication).updootComponent

    fun loadNextPage(subreddit: String?, sort: String, time: String?, nextPage: String?): Single<Pair<List<LinkData>, String?>> {
        return updootComponent
                .redditAPI
                .flatMap { redditAPI -> redditAPI.getSubreddit(subreddit, sort, time, nextPage) }
                .map { t: Thing ->
                    val submissions = mutableListOf<LinkData>()
                    if (t.data is ListingData) {
                        for (child in t.data.children) submissions.add(child.data as LinkData)
                    } else throw Exception("Unsupported JSON response")
                    Pair(submissions.toList(), t.data.after)
                }
    }

    fun save(submission: LinkData): Completable {
        return if (submission.saved)
            updootComponent
                    .redditAPI
                    .flatMapCompletable { redditAPI -> redditAPI.unsave(submission.name) }
        else updootComponent
                .redditAPI
                .flatMapCompletable { redditAPI -> redditAPI.save(submission.name) }
    }

    fun castVote(submission: LinkData, direction: Int): Completable {
        return updootComponent
                .redditAPI
                .flatMapCompletable { redditAPI ->
                    val dir = when (direction) {
                        1 -> if (submission.likes == null || !submission.likes) 1 else 0
                        -1 -> if (submission.likes == null || submission.likes) -1 else 0
                        else -> direction
                    }
                    redditAPI.castVote(submission.name, dir)
                }
    }

}

