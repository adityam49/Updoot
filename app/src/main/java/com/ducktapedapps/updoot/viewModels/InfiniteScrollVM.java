package com.ducktapedapps.updoot.viewModels;

import androidx.lifecycle.MutableLiveData;

public interface InfiniteScrollVM {
    void loadNextPage();

    String getAfter();

    MutableLiveData<Boolean> getIsLoading();
}
