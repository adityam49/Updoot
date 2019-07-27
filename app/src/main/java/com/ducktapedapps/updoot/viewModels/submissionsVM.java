package com.ducktapedapps.updoot.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.model.ListingData;
import com.ducktapedapps.updoot.model.thing;
import com.ducktapedapps.updoot.repository.submissionRepo;
import com.ducktapedapps.updoot.utils.constants;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class submissionsVM extends AndroidViewModel {

    private CompositeDisposable disposable = new CompositeDisposable();
    private static final String TAG = "submissionsVM";
    private submissionRepo frontPageRepo;

    private MutableLiveData<String> after = new MutableLiveData<>();
    private MutableLiveData<String> sorting = new MutableLiveData<>();
    private MutableLiveData<List<LinkData>> allSubmissions = new MutableLiveData<>();
    private MutableLiveData<String> state = new MutableLiveData<>();
    private MutableLiveData<Boolean> hasNextPage = new MutableLiveData<>();

    public submissionsVM(Application application) {
        super(application);
        frontPageRepo = new submissionRepo(application);
        hasNextPage.setValue(null);
        after.setValue(null);
        sorting.setValue(constants.TOP);
        loadNextPage();
    }

    private MutableLiveData<String> getSorting() {
        return sorting;
    }

    public MutableLiveData<Boolean> getHasNextPage() {
        return hasNextPage;
    }

    public MutableLiveData<String> getState() {
        return state;
    }

    public MutableLiveData<List<LinkData>> getAllSubmissions() {
        return allSubmissions;
    }

    private String getAfter() {
        return this.after.getValue();
    }

    public void loadNextPage() {
        disposable.add(
                frontPageRepo
                        .loadNextPage("all", getSorting().getValue(), getAfter())
                        .map(thing -> {
                            if (thing.getData() instanceof ListingData) {
                                after.postValue(((ListingData) thing.getData()).getAfter());
                                if (((ListingData) thing.getData()).getAfter() != null) {
                                    hasNextPage.postValue(true);
                                } else {
                                    hasNextPage.postValue(false);
                                }
                                return ((ListingData) thing.getData()).getChildren();
                            } else {
                                throw new Exception("unsupported response");
                            }
                        })
                        .map(things -> {
                            List<LinkData> linkDataList = new ArrayList<>();
                            for (thing thing : things) {
                                linkDataList.add(((LinkData) thing.getData()));
                            }
                            return linkDataList;
                        })
                        .map(linkDataList -> {
                            List<LinkData> submissions = allSubmissions.getValue();
                            if (submissions == null) {
                                submissions = linkDataList;
                            } else {
                                submissions.addAll(linkDataList);
                            }
                            return submissions;
                        })
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(__ -> state.postValue(constants.LOADING_STATE))
                        .subscribe(submissions -> {
                            allSubmissions.postValue(submissions);
                            state.postValue(constants.SUCCESS_STATE);
                        }, throwable -> {
                            Log.e(TAG, "onError: ", throwable.getCause());
                            state.postValue(throwable.getMessage());
                        }));
    }

    public void reload(String sort) {
        if (sort == null) {
            sorting.setValue(constants.TOP);
        } else {
            sorting.setValue(sort);
        }
        after.setValue(null);
        allSubmissions.setValue(null);
        loadNextPage();

    }

    @Override
    protected void onCleared() {
        if (!disposable.isDisposed()) {
            disposable.clear();
        }
        super.onCleared();
        Log.i(TAG, "onCleared: ");
    }
}
