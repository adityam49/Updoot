package com.ducktapedapps.updoot.ui.subreddit

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.model.LocalSubreddit
import com.ducktapedapps.updoot.utils.SubmissionUiType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SubredditVMImpl @ViewModelInject constructor(
        private val submissionRepo: SubmissionRepo,
        @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel(), ISubredditVM {
    override val subredditName: String = savedStateHandle.get<String>(SubredditFragment.SUBREDDIT_KEY)!!

    override val isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    override val toastMessage: MutableSharedFlow<String> = MutableSharedFlow()

    private val subredditPrefs: Flow<SubredditPrefs> = submissionRepo
            .subredditPrefs(subredditName)
            .transform { if (it != null) emit(it) else submissionRepo.saveDefaultSubredditPrefs(subredditName) }

    override val sorting: StateFlow<SubredditSorting?> = subredditPrefs
            .map { it.subredditSorting }
            .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Lazily,
                    initialValue = null
            )

    override val postViewType: StateFlow<SubmissionUiType> = subredditPrefs
            .map { it.viewType }
            .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Lazily,
                    initialValue = SubmissionUiType.COMPACT
            )


    private val _feedPagesOfIds: MutableStateFlow<Map<String?, List<String>>> = MutableStateFlow(emptyMap())
    override val feedPages: StateFlow<List<PostUiModel>> = _feedPagesOfIds
            .map {
                it.values.flatten()
            }.flatMapLatest { pages ->
                if (pages.isNotEmpty())
                    submissionRepo.observeCachedSubmissions(pages)
                else emptyFlow()
            }.map { listOfPosts ->
                listOfPosts.map { post -> post.toUiModel() }
            }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Lazily,
                    initialValue = emptyList()
            )

    override var lastScrollPosition: Int = 0

    override val subredditInfo: StateFlow<LocalSubreddit?> = submissionRepo.subredditInfo(subredditName)
            .catch { toastMessage.emit("Something went wrong! : ${it.message}") }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        sorting
                .filterNotNull()
                .onEach { reload() }
                .launchIn(viewModelScope)
    }

    override fun loadPage() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                val result = submissionRepo.getPage(
                        subreddit = subredditName,
                        nextPageKey = _feedPagesOfIds.value.keys.lastOrNull(),
                        sorting = sorting.value!!
                )

                submissionRepo.cacheSubmissions(result.children)

                _feedPagesOfIds.value = _feedPagesOfIds
                        .value
                        .toMutableMap()
                        .apply {
                            put(result.after, result.children.map { it.name })
                        }
            } catch (e: Exception) {
                e.printStackTrace()
                toastMessage.emit("Something went wrong! : ${e.message}")
            } finally {
                isLoading.value = false
            }

        }

    }

    override fun hasNextPage() = _feedPagesOfIds.value.keys.lastOrNull() != null

    override fun reload() {
        _feedPagesOfIds.value = emptyMap()
        loadPage()
    }

    override fun setPostViewType(type: SubmissionUiType) {
        viewModelScope.launch { submissionRepo.setPostViewType(subredditName, type) }
    }

    override fun changeSort(newSubredditSorting: SubredditSorting) {
        viewModelScope.launch { submissionRepo.changeSort(subredditName, newSubredditSorting) }
    }

    override fun upVote(id: String) {}

    override fun downVote(id: String) {}

    override fun save(id: String) {}
}