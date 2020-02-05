package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ducktapedapps.updoot.utils.Sorting
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT
import com.ducktapedapps.updoot.utils.SubmissionUiType.LARGE

class QASSubredditVM : ViewModel() {

    private val _sorting: MutableLiveData<Sorting> = MutableLiveData(Sorting.HOT)
    val sorting: LiveData<Sorting> = _sorting

    private val _viewType: MutableLiveData<SubmissionUiType> = MutableLiveData(COMPACT)
    val viewType: LiveData<SubmissionUiType> = _viewType

    fun toggleUi() {
        _viewType.value = if (_viewType.value == COMPACT) LARGE else COMPACT
    }

    fun changeSort(sorting: Sorting) {
        _sorting.value = sorting
    }
}