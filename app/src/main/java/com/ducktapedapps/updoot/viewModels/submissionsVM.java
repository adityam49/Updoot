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

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class submissionsVM extends AndroidViewModel implements InfiniteScrollVM {

    private CompositeDisposable disposable = new CompositeDisposable();
    private static final String TAG = "submissionsVM";
    private submissionRepo frontPageRepo;

    private final String subreddit;
    private String after;
    private String sorting;
    private String currentAccount;
    private String time;
    private MutableLiveData<List<LinkData>> allSubmissions = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<String> state = new MutableLiveData<>();
    private MutableLiveData<String> toastMessage = new MutableLiveData<>();

    public submissionsVM(Application application, String subreddit) {
        super(application);
        frontPageRepo = new submissionRepo(application);
        after = null;
        time = null;
        currentAccount = null;
        this.subreddit = subreddit;
        sorting = constants.HOT;
        loadNextPage();
    }

    public MutableLiveData<String> getToastMessage() {
        return toastMessage;
    }

    public MutableLiveData<String> getState() {
        return state;
    }

    public MutableLiveData<List<LinkData>> getAllSubmissions() {
        return allSubmissions;
    }


    public String getCurrentAccount() {
        return currentAccount;
    }

    public void setCurrentAccount(String currentAccount) {
        this.currentAccount = currentAccount;
    }

    public String getAfter() {
        return this.after;
    }

    public void loadNextPage() {
        disposable.add(
                frontPageRepo
                        .loadNextPage(subreddit, sorting, time, after)
                        .map(thing -> {
                            if (thing.getData() instanceof ListingData) {
                                after = ((ListingData) thing.getData()).getAfter();
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


    public void castVote(int index, int direction) {
        if (allSubmissions.getValue() != null) {
            LinkData data = allSubmissions.getValue().get(index);
            if (data != null) {
                final List<LinkData> currentSubmissions = allSubmissions.getValue();
                frontPageRepo
                        .castVote(data, direction)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                LinkData updateData = currentSubmissions.get(index);
                                updateData = updateData.vote(direction);
                                currentSubmissions.set(index, updateData);
                                allSubmissions.postValue(currentSubmissions);
                                disposable.add(d);
                            }

                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onError(Throwable throwable) {
                                LinkData originalData = currentSubmissions.get(index);
                                originalData = originalData.vote(direction);
                                currentSubmissions.set(index, originalData);
                                allSubmissions.postValue(currentSubmissions);
                                Log.e(TAG, "castVote: ", throwable);
                            }
                        });
            }
        }
    }

    public void save(int index) {
        if (allSubmissions.getValue() != null) {
            LinkData data = allSubmissions.getValue().get(index);
            if (data != null) {
                final List<LinkData> currentSubmissions = getAllSubmissions().getValue();
                frontPageRepo
                        .save(data)
                        .subscribeOn(Schedulers.io())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                disposable.add(d);
                            }

                            @Override
                            public void onComplete() {
                                LinkData updatedData = allSubmissions.getValue().get(index);
                                updatedData = updatedData.save();
                                currentSubmissions.set(index, updatedData);
                                allSubmissions.postValue(currentSubmissions);
                                if (updatedData.getSaved()) {
                                    toastMessage.postValue("Submission saved!");
                                } else {
                                    toastMessage.postValue("Submission unsaved!");
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                LinkData updatedData = allSubmissions.getValue().get(index);
                                updatedData = updatedData.save();
                                currentSubmissions.set(index, updatedData);
                                allSubmissions.postValue(currentSubmissions);
                                toastMessage.postValue("Error saving submission");
                            }
                        });
            }
        }
    }

    public void reload(String sort, String time) {
        if (sort == null) {
            sorting = constants.HOT;
        } else {
            sorting = sort;
            if (time != null) {
                this.time = time;
            }
        }
        after = null;
//        after.setValue(null);
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
