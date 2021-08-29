package com.ducktapedapps.updoot.user

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.FullComment
import com.ducktapedapps.updoot.data.remote.model.Trophy
import com.ducktapedapps.updoot.subreddit.PostUiModel
import com.ducktapedapps.updoot.subreddit.toUiModel
import com.ducktapedapps.updoot.user.UserSection.*
import com.ducktapedapps.updoot.utils.PagingModel
import com.ducktapedapps.updoot.utils.RedditItem
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

interface UserViewModel {
    val userName: String

    val sections: StateFlow<List<UserSection>>

    val userTrophies: StateFlow<List<Trophy>>

    val currentSection: StateFlow<UserSection>

    val content: StateFlow<PagingModel<List<UserContent>>>

    fun loadPage()

    fun setSection(section: UserSection)
}

@HiltViewModel
class UserViewModelImpl @Inject constructor(
    private val getUserOverviewUseCase: GetUserOverviewUseCase,
    private val getUserCommentsUseCase: GetUserCommentsUseCase,
    private val getUserPostsUseCase: GetUserPostsUseCase,
    private val getUserUpVotedUseCase: GetUserUpVotedUseCase,
    private val getUserDownVotedUseCase: GetUserDownVotedUseCase,
    private val getUserGildedUseCase: GetUserGildedUseCase,
    private val getUserSavedUseCase: GetUserSavedUseCase,
    private val getUserTrophiesUseCase: GetUserTrophiesUseCase,
    getUseSectionsUseCase: GetUserSectionsUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), UserViewModel {
    override val userName = savedStateHandle.get<String>(UserFragment.USERNAME_KEY)!!

    override val sections: StateFlow<List<UserSection>> = getUseSectionsUseCase
        .getUserSections(userName)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    override val userTrophies: StateFlow<List<Trophy>> = getUserTrophiesUseCase
        .trophies
        .onStart {
            getUserTrophiesUseCase.loadUserTrophies(userName)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    override val currentSection: MutableStateFlow<UserSection> = MutableStateFlow(OverView)

    override val content: StateFlow<PagingModel<List<UserContent>>> =
        currentSection
            .onEach { loadPage() }
            .flatMapLatest { section ->
                when (section) {
                    OverView -> getUserOverviewUseCase.pagingModel
                    Comments -> getUserCommentsUseCase.pagingModel
                    Posts -> getUserPostsUseCase.pagingModel
                    UpVoted -> getUserUpVotedUseCase.pagingModel
                    DownVoted -> getUserDownVotedUseCase.pagingModel
                    Gilded -> getUserGildedUseCase.pagingModel
                    Saved -> getUserSavedUseCase.pagingModel
                }
            }.map {
                PagingModel(
                    it.content.map { element ->
                        when (element) {
                            is RedditItem.CommentData -> UserContent.UserComment(element.data)
                            is RedditItem.PostData -> UserContent.UserPost(element.data.toUiModel())
                        }
                    },
                    it.footer
                )
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                PagingModel(emptyList(), PagingModel.Footer.End)
            )

    override fun loadPage() {
        viewModelScope.launch {
            when (currentSection.value) {
                OverView -> getUserOverviewUseCase.loadNextPage(userName)
                Comments -> getUserCommentsUseCase.loadNextPage(userName)
                Posts -> getUserPostsUseCase.loadNextPage(userName)
                UpVoted -> getUserUpVotedUseCase.loadNextPage(userName)
                DownVoted -> getUserDownVotedUseCase.loadNextPage(userName)
                Saved -> getUserSavedUseCase.loadNextPage(userName)
                Gilded -> getUserGildedUseCase.loadNextPage(userName)
            }
        }
    }

    override fun setSection(section: UserSection) {
        currentSection.value = section
    }
}

sealed class UserContent {
    data class UserPost(val data: PostUiModel) : UserContent()
    data class UserComment(val data: FullComment) : UserContent()
}

enum class UserSection {
    OverView, Comments, Posts, UpVoted, DownVoted, Gilded, Saved
}

fun getNonUserSpecificSections() = listOf(
    OverView, Posts, Comments, Gilded
)

fun getAllUserSections() = values().toList()