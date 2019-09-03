package com.ducktapedapps.updoot.viewModels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class submissionsVMFactory extends ViewModelProvider.AndroidViewModelFactory {
    private final String subreddit;
    private final Application application;

    public submissionsVMFactory(@NonNull Application application, String subreddit) {
        super(application);
        this.subreddit = subreddit;
        this.application = application;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new submissionsVM(application, subreddit);
    }
}
