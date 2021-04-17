package com.ducktapedapps.updoot.ui.user

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.FullComment
import com.ducktapedapps.updoot.ui.subreddit.PostUiModel
import com.ducktapedapps.updoot.ui.user.UserSection.*
import com.ducktapedapps.updoot.utils.PagingModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface UserViewModel {
    val userName: String

    val sections: StateFlow<List<UserSection>>

    val currentSection: StateFlow<UserSection>

    val content: StateFlow<PagingModel<List<UserContent>>>

    fun loadPage()

    fun setSection(section: UserSection)
}


class UserViewModelImpl @ViewModelInject constructor(
    private val loadUserCommentsUseCase: GetUserCommentsUseCase,
    getUseSectionsUseCase: GetUserSectionsUseCase,
    @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel(), UserViewModel {
    override val userName = savedStateHandle.get<String>(UserFragment.USERNAME_KEY)!!

    override val sections: StateFlow<List<UserSection>> = getUseSectionsUseCase
        .getUserSections(userName)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    override val currentSection: MutableStateFlow<UserSection> = MutableStateFlow(Comments)

    override val content: StateFlow<PagingModel<List<UserContent>>> = combine(
        currentSection,
        loadUserCommentsUseCase.pagingModel
    ) { currentSectionValue, userComments ->
        when (currentSectionValue) {
            Comments -> userComments.copy(content = userComments.content.map { comment ->
                UserContent.UserComment(
                    comment.data
                )
            })
            else -> PagingModel(emptyList<UserContent>(), PagingModel.Footer.End)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PagingModel(emptyList(), PagingModel.Footer.End)
    )

    override fun loadPage() {
        viewModelScope.launch {
            when (currentSection.value) {
                Comments -> loadUserCommentsUseCase.loadNextPage(userName)
                else -> Unit
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