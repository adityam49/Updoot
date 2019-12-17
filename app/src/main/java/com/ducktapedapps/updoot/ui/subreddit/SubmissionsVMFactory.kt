package com.ducktapedapps.updoot.ui.subreddit

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SubmissionsVMFactory(private val application: Application,
                           private val subreddit: String) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubmissionsVM::class.java)) {
            return SubmissionsVM(application, subreddit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}
