package com.ducktapedapps.updoot.subreddit

import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.SubredditPrefsDAO
import com.ducktapedapps.updoot.subreddit.SubredditSorting.Hot
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

interface GetSubredditPreferencesUseCase {

    fun getSubredditPrefsFlow(subredditName: String): Flow<SubredditPrefs>

}

class GetSubredditPreferencesUseCaseImpl @Inject constructor(
    private val subredditPrefsDAO: SubredditPrefsDAO,
) : GetSubredditPreferencesUseCase {

    override fun getSubredditPrefsFlow(subredditName: String): Flow<SubredditPrefs> =
        subredditPrefsDAO
            .observeSubredditPrefs(subredditName)
            .transform {
                if (it == null) subredditPrefsDAO
                    .insertSubredditPrefs(
                        SubredditPrefs(
                            subredditName = subredditName,
                            viewType = COMPACT,
                            subredditSorting = Hot
                        )
                    )
                else emit(it)
            }

}