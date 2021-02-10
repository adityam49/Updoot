package com.ducktapedapps.updoot.ui.user

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.FullComment
import com.ducktapedapps.updoot.data.remote.model.Listing
import com.ducktapedapps.updoot.data.remote.model.RedditThing
import com.ducktapedapps.updoot.ui.subreddit.PostUiModel
import com.ducktapedapps.updoot.ui.user.UserSection.*
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import kotlinx.coroutines.flow.*

//TODO : fix the entire thing
class UserViewModel @ViewModelInject constructor(
        private val redditClient: IRedditClient,
        @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel(), IUserViewModel {
    override val userName = savedStateHandle.get<String>(UserFragment.USERNAME_KEY)!!

    override val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val currentSection: MutableStateFlow<UserSection> = MutableStateFlow(OverView)

    private val _content: MutableStateFlow<Map<String?, List<RedditThing>>> = MutableStateFlow(emptyMap())
    override val content: StateFlow<List<UserContent>> = emptyFlow<List<UserContent>>().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
    )

    private val requestDispatcher = MutableSharedFlow<NextPageRequest>()

    init {
//        observeRequests().launchIn(viewModelScope)
        loadPage()
    }

    override fun reload() {
        _content.value = emptyMap()
        loadPage()
    }

    //TODO : add currently logged in user as a sharedflow and combine with this flow
//    private fun observeRequests(): Flow<List<UserContent>> =
//            combine(currentSection, requestDispatcher) { userSection, request ->
//                _content.value + getPage(userSection, request.nextPageKey)
//            }.onEach {
//                _content.value = it
//            }

    override fun loadPage() {
//        viewModelScope.launch {
//            if (_content.value.isEmpty())
//                requestDispatcher.emit(FirstPageRequest)
//            else if (_content.value.last().after != null)
//                requestDispatcher.emit(
//                        NextPageRequest(nextPageKey = _content.value.last().after)
//                )
//        }
    }

    private suspend fun getPage(section: UserSection, after: String?): Listing<out RedditThing> {
        isLoading.value = true
        val api = redditClient.api()
        return try {
            when (section) {
                OverView -> api.getUserOverView(userName, after)
                Comments -> api.getUserComments(userName, after)
                Posts -> api.getUserSubmittedPosts(userName, after)
                // logged in specific sections : should not be visible to non logged in users
                UpVoted -> api.getUserUpVotedThings(userName, after)
                DownVoted -> api.getUserDownVotedThings(userName, after)
                Gilded -> api.getUserGildedThings(userName, after)
                Saved -> api.getUserSavedThings(userName, after)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Listing(null, emptyList())
        } finally {
            isLoading.value = false
        }
    }

    override fun setSection(section: UserSection) {
        _content.value = emptyMap()
        currentSection.value = section
    }
}

sealed class UserContent {
    data class UserPost(val data: PostUiModel) : UserContent()
    data class UserComment(val data: FullComment) : UserContent()
}

data class NextPageRequest(val nextPageKey: String?)

val FirstPageRequest = NextPageRequest(null)

enum class UserSection {
    OverView, Comments, Posts, UpVoted, DownVoted, Gilded, Saved
}