package com.ducktapedapps.updoot.user

import com.ducktapedapps.updoot.data.remote.model.Trophy
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

interface GetUserTrophiesUseCase {
    val trophies: Flow<List<Trophy>>

    suspend fun loadUserTrophies(userName: String)

}

class GetUserTrophiesUseCaseImpl @Inject constructor(
    private val redditClient: RedditClient,
) : GetUserTrophiesUseCase {
    override val trophies: MutableStateFlow<List<Trophy>> = MutableStateFlow(emptyList())

    override suspend fun loadUserTrophies(userName: String) {
        try {
            val api = redditClient.api()
            val fetchedTrophies = api.getUserTrophies(userName)
            trophies.value = fetchedTrophies.trophies
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}