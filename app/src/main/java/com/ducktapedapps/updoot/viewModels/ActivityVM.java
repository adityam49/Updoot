package com.ducktapedapps.updoot.viewModels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActivityVM extends ViewModel {


    private static final String TAG = "ActivityVM";
    private MutableLiveData<String> currentAccount = new MutableLiveData<>();

    public ActivityVM() {

    }

    public MutableLiveData<String> getCurrentAccount() {
        Log.i(TAG, "getCurrentAccount: " + currentAccount.getValue());
        return currentAccount;
    }
}
