package com.ducktapedapps.updoot.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.UpdootApplication
import javax.inject.Inject

class ExploreVMFactory(updootApplication: UpdootApplication) : ViewModelProvider.Factory {
    init {
        updootApplication.updootComponent.inject(this)
    }

    @Inject
    lateinit var exploreRepo: ExploreRepo

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreVM::class.java)) {
            return ExploreVM(exploreRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}