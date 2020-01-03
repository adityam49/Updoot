package com.ducktapedapps.updoot.ui.explore

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExploreVMFactory(private val appContext: Application) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreVM::class.java)) {
            return ExploreVM(appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}