package com.ducktapedapps.updoot.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.Listing
import com.ducktapedapps.updoot.data.local.model.RedditThing
import com.ducktapedapps.updoot.ui.user.UserSection.*
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class UserViewModel(private val redditClient: RedditClient, private val userName: String) : ViewModel() {
    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    private val _currentSection: MutableStateFlow<UserSection> = MutableStateFlow(OverView)
    val currentSection: StateFlow<UserSection> = _currentSection

    private val _content: MutableStateFlow<Listing<out RedditThing>> = MutableStateFlow(Listing(null, emptyList()))
    val content: StateFlow<Listing<out RedditThing>> = _content

    init {
        currentSection
                .transformSectionToListing()
                .onEach { newListing -> _content.value = newListing }
                .launchIn(viewModelScope)
    }

    private fun StateFlow<UserSection>.transformSectionToListing(): Flow<Listing<out RedditThing>> =
            transform { section ->
                emit(Listing<RedditThing>(null, emptyList()))
                _loading.value = true
                val api = redditClient.api()
                try {
                    emit(when (section) {
                        OverView -> api.getUserOverView(userName)
                        Comments -> api.getUserComments(userName)
                        Posts -> api.getUserSubmittedPosts(userName)
                        UpVoted -> api.getUserUpVotedThings(userName)
                        DownVoted -> api.getUserDownVotedThings(userName)
                        Gilded -> api.getUserGildedThings(userName)
                        Saved -> api.getUserSavedThings(userName)
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _loading.value = false
                }
            }

    fun setSection(section: UserSection) {
        _currentSection.value = section
    }
}

enum class UserSection {
    OverView, Comments, Posts, UpVoted, DownVoted, Gilded, Saved
}

class UserVMFactory @Inject() constructor(
        private val redditClient: RedditClient
) : ViewModelProvider.Factory {
    private lateinit var userName: String
    fun forUser(name: String) {
        userName = name
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            UserViewModel(redditClient, userName) as T
}