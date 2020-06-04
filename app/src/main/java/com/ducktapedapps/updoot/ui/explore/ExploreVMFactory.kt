package com.ducktapedapps.updoot.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class ExploreVMFactory @Inject constructor(private val exploreRepo: ExploreRepo) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExploreVM::class.java)) {
            return ExploreVM(exploreRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}