package com.ducktapedapps.updoot.repository

import android.app.Application
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.di.UpdootComponent
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Thing
import io.reactivex.Completable
import io.reactivex.Single

class SubmissionRepo(application: Application) {
    private val updootComponent: UpdootComponent = (application as UpdootApplication).updootComponent

    fun loadNextPage(subreddit: String?, sort: String, time: String?, nextPage: String?): Single<Thing> {
        return updootComponent
                .redditAPI
                .flatMap { redditAPI -> redditAPI.getSubreddit(subreddit, sort, time, nextPage) }
    }

    fun save(submission: LinkData): Completable {
        return updootComponent
                .redditAPI
                .flatMapCompletable { redditAPI -> redditAPI.save(submission.name) }
    }

    fun castVote(submission: LinkData, direction: Int): Completable {
        return updootComponent
                .redditAPI
                .flatMapCompletable { redditAPI ->
                    when (direction) {
                        1 -> if (submission.likes == null || !submission.likes) redditAPI.castVote(submission.name, 1) else redditAPI.castVote(submission.name, 0)
                        -1 -> if (submission.likes == null || submission.likes) redditAPI.castVote(submission.name, -1) else redditAPI.castVote(submission.name, 0)
                        else -> redditAPI.castVote(submission.name, direction)
                    }
                }
    }

}

