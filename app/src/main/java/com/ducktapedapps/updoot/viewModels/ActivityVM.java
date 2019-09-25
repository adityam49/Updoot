package com.ducktapedapps.updoot.viewModels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ducktapedapps.updoot.utils.SingleLiveEvent;


public class ActivityVM extends ViewModel {
    private static final String TAG = "ActivityVM";
    private MutableLiveData<SingleLiveEvent<String>> currentAccount;

    public ActivityVM() {
        currentAccount = new MutableLiveData<>(new SingleLiveEvent<>(null));
    }

    public MutableLiveData<SingleLiveEvent<String>> getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(String newAccount) {
        this.currentAccount.setValue(new SingleLiveEvent<>(newAccount));
    }
}
