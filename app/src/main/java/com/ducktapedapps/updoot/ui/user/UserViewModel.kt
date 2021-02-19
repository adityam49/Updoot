package com.ducktapedapps.updoot.ui.user

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.model.FullComment
import com.ducktapedapps.updoot.ui.subreddit.PostUiModel
import com.ducktapedapps.updoot.ui.user.UserSection.Comments
import com.ducktapedapps.updoot.utils.Page
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

interface UserViewModel {
    val userName: String

    val sections: StateFlow<List<UserSection>>

    val currentSection: StateFlow<UserSection>

    val content: StateFlow<List<Page<List<UserContent>>>>

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

    override val content: StateFlow<List<Page<List<UserContent>>>> = combine(
        currentSection,
        loadUserCommentsUseCase.pagesOfComments
    ) { currentSectionValue, pagesOfComments ->
        when (currentSectionValue) {
            Comments -> pagesOfComments
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadPage()
    }

    override fun loadPage() {
        viewModelScope.launch(Dispatchers.IO) {
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