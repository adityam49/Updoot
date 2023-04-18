package com.ducktapedapps.updoot.subreddit

import com.ducktapedapps.updoot.data.local.PostDAO
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import javax.inject.Inject

interface EditPostVoteUseCase {
    suspend fun changeVote(
        id: String,
        currentVote: Boolean?,
        intendedVote:Boolean,
    )
}

class EditPostVoteUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
    private val postDAO: PostDAO,
) : EditPostVoteUseCase {
    override suspend fun changeVote(
        id: String,
        currentVote: Boolean?,
        intendedVote: Boolean
    ) {
        val direction = when{
            currentVote == true && intendedVote -> 0
            currentVote == true && !intendedVote -> -1
            currentVote == false && !intendedVote -> 0
            currentVote == false && intendedVote -> 1
            currentVote == null && intendedVote -> 1
            currentVote == null && !intendedVote -> -1
            else -> 0
        }
        val api = redditClient.api()
        val result = api.castVote(
            id, direction
        )
        if(result.isSuccessful)
            postDAO.setVote(id,when(direction){
                1 -> true
                -1 -> false
                else -> null
            })
    }
}