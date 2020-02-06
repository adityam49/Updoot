package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.Sorting
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT
import com.ducktapedapps.updoot.utils.SubmissionUiType.LARGE
import com.ducktapedapps.updoot.utils.accountManagement.Reddit
import kotlinx.coroutines.launch
import okio.IOException

class QASSubredditVM constructor(val reddit: Reddit) : ViewModel() {

    fun loadInfo() {
        _subredditInfo.value = null
        viewModelScope.launch {
            subredditName?.let {
                try {
                    val api = reddit.authenticatedAPI()
                    _subredditInfo.value = api.getSubredditInfo(it)
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }
            }
        }
    }

    var subredditName: String? = null
    private val _sorting: MutableLiveData<Sorting> = MutableLiveData(Sorting.HOT)
    val sorting: LiveData<Sorting> = _sorting

    private val _viewType: MutableLiveData<SubmissionUiType> = MutableLiveData(COMPACT)
    val viewType: LiveData<SubmissionUiType> = _viewType

    private val _subredditInfo: MutableLiveData<Subreddit> = MutableLiveData()
    val subredditInfo: LiveData<Subreddit> = _subredditInfo

    fun toggleUi() {
        _viewType.value = if (_viewType.value == COMPACT) LARGE else COMPACT
    }

    fun changeSort(sorting: Sorting) {
        _sorting.value = sorting
    }
}