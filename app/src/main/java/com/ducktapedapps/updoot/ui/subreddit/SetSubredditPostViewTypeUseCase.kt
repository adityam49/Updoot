package com.ducktapedapps.updoot.ui.subreddit

import com.ducktapedapps.updoot.data.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.utils.PostViewType
import javax.inject.Inject

interface SetSubredditPostViewTypeUseCase {

    suspend fun setPostViewType(subreddit: String, viewType: PostViewType)

}

class SetSubredditPostViewTypeUseCaseImpl @Inject constructor(
    private val subredditPrefsDAO: SubredditPrefsDAO
) : SetSubredditPostViewTypeUseCase {
    override suspend fun setPostViewType(subreddit: String, viewType: PostViewType) {
        subredditPrefsDAO.setUIType(newViewType = viewType, subreddit = subreddit)
    }
}