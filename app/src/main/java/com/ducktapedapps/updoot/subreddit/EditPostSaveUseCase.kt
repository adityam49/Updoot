package com.ducktapedapps.updoot.subreddit

import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import javax.inject.Inject

interface EditPostSaveUseCase {
    suspend fun toggleSavePost(postId: String, isPostCurrentlySaved: Boolean)
}

class EditPostSaveUseCaseImpl @Inject constructor(
    private val postDAO: PostDAO,
    private val redditClient: RedditClient,
) : EditPostSaveUseCase {
    override suspend fun toggleSavePost(postId: String, isPostCurrentlySaved: Boolean) {
        val api = redditClient.api()

        val result = if (isPostCurrentlySaved) api.unSave(postId) else api.save(postId)
        if (result.isSuccessful)
            postDAO.setSavePost(id = postId, postSaved = !isPostCurrentlySaved)
    }
}