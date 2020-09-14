package com.ducktapedapps.updoot.ui.subreddit.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.SubmissionsCacheDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OptionsSheetViewModel(submissionsCacheDAO: SubmissionsCacheDAO, submissionId: String) : ViewModel() {

    private val _optionsList = MutableLiveData<List<SubmissionOptionUiModel>>()
    val optionsList: LiveData<List<SubmissionOptionUiModel>> = _optionsList

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val linkData = submissionsCacheDAO.getLinkData(submissionId)
            _optionsList.postValue(listOf(
                    SubmissionOptionUiModel(name = linkData.subredditName, icon = R.drawable.ic_subreddit_default_24dp),
                    SubmissionOptionUiModel(name = linkData.author, icon = R.drawable.ic_account_circle_24dp),
                    SubmissionOptionUiModel(name = "Copy link", additionalData = linkData.permalink, icon = R.drawable.ic_link_24dp)
            ))
        }
    }
}