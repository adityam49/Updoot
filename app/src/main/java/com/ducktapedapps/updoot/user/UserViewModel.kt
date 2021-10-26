package com.ducktapedapps.updoot.user

import androidx.compose.runtime.Stable
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
    val viewState: StateFlow<ViewState>

    fun setUserName(user: String)

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
) : ViewModel(), UserViewModel {
    private val userName: MutableStateFlow<String?> = MutableStateFlow(null)
    private val currentSection: MutableStateFlow<UserSection> = MutableStateFlow(OverView)
    private val sections: Flow<List<UserSection>> = userName
        .filterNotNull()
        .flatMapLatest {
            getUseSectionsUseCase
                .getUserSections(it)
        }

    private val userTrophies: Flow<List<Trophy>> = userName
        .filterNotNull()
        .flatMapLatest {
            getUserTrophiesUseCase
                .trophies
                .onStart {
                    getUserTrophiesUseCase.loadUserTrophies(it)
                }
        }


    private val content: StateFlow<PagingModel<List<UserContent>>> =
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
    override val viewState: StateFlow<ViewState> = combine(
        userName.filterNotNull(), content, sections, currentSection, userTrophies
    ) { name, contentValue, sectionsValue, currentSectionValue, userTrophiesValue ->
        ViewState(
            userName = name,
            content = contentValue,
            sections = sectionsValue,
            currentSection = currentSectionValue,
            userTrophies = userTrophiesValue
        )
    }.stateIn(
        viewModelScope, SharingStarted.Lazily, ViewState(
            userName = "",
            content = content.value,
            sections = listOf(OverView),
            currentSection = OverView,
            userTrophies = emptyList()
        )
    )

    override fun loadPage() {
        viewModelScope.launch {
            userName.value?.let {
                when (currentSection.value) {
                    OverView -> getUserOverviewUseCase.loadNextPage(it)
                    Comments -> getUserCommentsUseCase.loadNextPage(it)
                    Posts -> getUserPostsUseCase.loadNextPage(it)
                    UpVoted -> getUserUpVotedUseCase.loadNextPage(it)
                    DownVoted -> getUserDownVotedUseCase.loadNextPage(it)
                    Saved -> getUserSavedUseCase.loadNextPage(it)
                    Gilded -> getUserGildedUseCase.loadNextPage(it)
                }
            }
        }
    }

    override fun setUserName(user: String) {
        userName.value = user
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

data class ViewState(
    val userName: String,
    val sections: List<UserSection>,
    val userTrophies: List<Trophy>,
    val currentSection: UserSection,
    val content: PagingModel<List<UserContent>>,
)