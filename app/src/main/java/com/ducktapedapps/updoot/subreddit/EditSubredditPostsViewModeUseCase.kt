package com.ducktapedapps.updoot.subreddit

import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE
import kotlinx.coroutines.flow.first
import javax.inject.Inject

interface EditSubredditPostsViewModeUseCase {
    suspend fun toggleViewType(subredditName: String)
}

class EditSubredditPostsViewModeUseCaseImpl @Inject constructor(
    private val subredditPrefsDAO: SubredditPrefsDAO,
) : EditSubredditPostsViewModeUseCase {
    override suspend fun toggleViewType(subredditName: String) {
        val subredditPref = subredditPrefsDAO.observeSubredditPrefs(subredditName).first()
        with(subredditPref) {
            if (this == null) {
                subredditPrefsDAO.insertSubredditPrefs(
                    SubredditPrefs().copy(subredditName = subredditName)
                )
            } else {
                subredditPrefsDAO.insertSubredditPrefs(
                    copy(viewType = if (viewType == LARGE) COMPACT else LARGE)
                )
            }
        }
    }
}