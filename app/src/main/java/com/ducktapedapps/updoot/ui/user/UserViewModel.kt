package com.ducktapedapps.updoot.ui.user

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.Listing
import com.ducktapedapps.updoot.data.local.model.RedditThing
import com.ducktapedapps.updoot.ui.user.UserSection.*
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserViewModel @ViewModelInject constructor(
        private val redditClient: RedditClient,
        @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {
    companion object {
        const val USERNAME_KEY = "user_name_key"
    }

    val userName: String = savedStateHandle.get<String>(USERNAME_KEY)!!

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val requestDispatcher = MutableSharedFlow<NextPageRequest>()

    private val _currentSection: MutableStateFlow<UserSection> = MutableStateFlow(OverView)
    val currentSection: StateFlow<UserSection> = _currentSection

    private val _content: MutableStateFlow<List<Listing<out RedditThing>>> = MutableStateFlow(emptyList())
    val content: StateFlow<List<RedditThing>> = _content.map {
        it.flatMap { listing -> listing.children }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        observeRequests().launchIn(viewModelScope)
        loadPage()
    }

    fun reload() {
        _content.value = emptyList()
        loadPage()
    }

    //    TODO : add currently logged in user as a sharedflow and combine with this flow
    private fun observeRequests(): Flow<List<Listing<out RedditThing>>> =
            combine(currentSection, requestDispatcher) { userSection, request ->
                _content.value + getPage(userSection, request.nextPageKey)
            }.onEach {
                _content.value = it
            }

    fun loadPage() {
        viewModelScope.launch {
            if (_content.value.isEmpty())
                requestDispatcher.emit(
                        NextPageRequest(nextPageKey = null)
                )
            else if (_content.value.last().after != null)
                requestDispatcher.emit(
                        NextPageRequest(nextPageKey = _content.value.last().after)
                )
        }
    }

    private suspend fun getPage(section: UserSection, after: String?): Listing<out RedditThing> {
        _loading.value = true
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
            _loading.value = false
        }
    }

    fun setSection(section: UserSection) {
        _content.value = emptyList()
        _currentSection.value = section
    }
}

data class NextPageRequest(val nextPageKey: String?)

enum class UserSection {
    OverView, Comments, Posts, UpVoted, DownVoted, Gilded, Saved
}