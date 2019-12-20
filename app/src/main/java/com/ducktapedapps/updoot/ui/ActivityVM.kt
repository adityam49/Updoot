package com.ducktapedapps.updoot.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ducktapedapps.updoot.utils.SingleLiveEvent

class ActivityVM : ViewModel() {
    val currentAccount: MutableLiveData<SingleLiveEvent<String?>> = MutableLiveData(SingleLiveEvent<String?>(null))

    fun setCurrentAccount(newAccount: String?) {
        currentAccount.value = SingleLiveEvent(newAccount)
    }

}