package com.ducktapedapps.updoot.ui.subreddit.options

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.data.local.SubmissionsCacheDAO
import javax.inject.Inject

class OptionsSheetVMFactory @Inject constructor(
        val submissionsCacheDAO: SubmissionsCacheDAO
) : ViewModelProvider.Factory {
    private lateinit var submissionId: String

    fun setSubmissionId(id: String) {
        submissionId = id
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            if (modelClass.isAssignableFrom(OptionsSheetViewModel::class.java))
                OptionsSheetViewModel(submissionsCacheDAO, submissionId) as T
            else throw IllegalArgumentException("Unknown ViewModel class")
}