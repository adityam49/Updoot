package com.ducktapedapps.updoot.ui.subreddit

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.SubredditPrefs
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.common.InfiniteScrollVM
import com.ducktapedapps.updoot.utils.SubmissionUiType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class SubmissionsVM @ViewModelInject constructor(
        private val submissionRepo: SubmissionRepo,
        @Assisted savedStateHandle: SavedStateHandle
) : ViewModel(), InfiniteScrollVM {
    private val subreddit: String = savedStateHandle.get<String>(SubredditFragment.SUBREDDIT_KEY)!!

    private val _isLoading = MutableStateFlow(false)
    override val isLoading: StateFlow<Boolean> = _isLoading

    private val _toastMessage: MutableSharedFlow<String> = MutableSharedFlow()
    val toastMessage: SharedFlow<String> = _toastMessage

    private val subredditPrefs: Flow<SubredditPrefs> = submissionRepo
            .subredditPrefs(subreddit)
            .transform { if (it != null) emit(it) else submissionRepo.saveDefaultSubredditPrefs(subreddit) }

    val sorting: StateFlow<SubredditSorting?> = subredditPrefs
            .map { it.subredditSorting }
            .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Lazily,
                    initialValue = null
            )

    val postViewType: StateFlow<SubmissionUiType> = subredditPrefs
            .map { it.viewType }
            .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Lazily,
                    initialValue = SubmissionUiType.COMPACT
            )


    private val _feedPagesOfIds: MutableStateFlow<Map<String?, List<String>>> = MutableStateFlow(emptyMap())
    val feedPages: StateFlow<List<LinkData>> = _feedPagesOfIds
            .map {
                it.values.flatten()
            }.flatMapLatest { pages ->
                if (pages.isNotEmpty())
                    submissionRepo.observeCachedSubmissions(pages)
                else emptyFlow()
            }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.Lazily,
                    initialValue = emptyList()
            )

    var lastScrollPosition: Int = 0

    val subredditInfo: StateFlow<Subreddit?> = submissionRepo.subredditInfo(subreddit)
            .catch { _toastMessage.emit("Something went wrong! : ${it.message}") }
            .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            sorting.filterNotNull().onEach { reload() }.collect()
        }
    }

    override fun loadPage() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = submissionRepo.getPage(
                        subreddit = subreddit,
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
                _toastMessage.emit("Something went wrong! : ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }

    }

    override fun hasNextPage() = _feedPagesOfIds.value.keys.lastOrNull() != null

    fun reload() {
        _feedPagesOfIds.value = emptyMap()
        loadPage()
    }

    fun setPostViewType(type: SubmissionUiType) {
        viewModelScope.launch { submissionRepo.setPostViewType(subreddit, type) }
    }

    fun changeSort(newSubredditSorting: SubredditSorting) {
        viewModelScope.launch { submissionRepo.changeSort(subreddit, newSubredditSorting) }
    }

    fun upVote(name: String) {
        viewModelScope.launch { submissionRepo.vote(name, 1) }
    }

    fun downVote(name: String) {
        viewModelScope.launch { submissionRepo.vote(name, -1) }
    }

    fun save(id: String) {
        viewModelScope.launch { submissionRepo.save(id) }
    }
}