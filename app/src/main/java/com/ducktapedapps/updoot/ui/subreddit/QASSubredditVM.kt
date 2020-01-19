package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ducktapedapps.updoot.utils.Sorting

class QASSubredditVM : ViewModel() {

    private val _sorting: MutableLiveData<Sorting> = MutableLiveData(Sorting.HOT)
    val sorting: LiveData<Sorting> = _sorting

    fun changeSort(sorting: Sorting) {
        _sorting.value = sorting
    }
}