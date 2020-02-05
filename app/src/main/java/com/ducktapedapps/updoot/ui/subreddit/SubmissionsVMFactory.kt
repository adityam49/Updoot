package com.ducktapedapps.updoot.ui.subreddit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.UpdootApplication
import javax.inject.Inject

class SubmissionsVMFactory(private val subreddit: String, updootApplication: UpdootApplication) : ViewModelProvider.Factory {
    init {
        updootApplication.updootComponent.inject(this)
    }

    @Inject
    lateinit var submissionRepo: SubmissionRepo

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubmissionsVM::class.java)) {
            return SubmissionsVM(subreddit, submissionRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
