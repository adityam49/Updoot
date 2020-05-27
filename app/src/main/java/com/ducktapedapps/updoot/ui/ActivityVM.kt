package com.ducktapedapps.updoot.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.navDrawer.NavDrawerItemModel
import com.ducktapedapps.updoot.utils.SingleLiveEvent

class ActivityVM : ViewModel() {
    val currentAccount: MutableLiveData<SingleLiveEvent<String?>> = MutableLiveData(SingleLiveEvent<String?>(null))
    val navItemList: LiveData<List<NavDrawerItemModel>> = MutableLiveData(
            listOf(
                    NavDrawerItemModel("Item1", R.drawable.ic_explore_black_24dp),
                    NavDrawerItemModel("Item2", R.drawable.ic_explore_black_24dp),
                    NavDrawerItemModel("Item3", R.drawable.ic_explore_black_24dp),
                    NavDrawerItemModel("Item4", R.drawable.ic_explore_black_24dp)
            )
    )

    fun setCurrentAccount(newAccount: String?) {
        currentAccount.value = SingleLiveEvent(newAccount)
    }

}