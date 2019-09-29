package com.ducktapedapps.updoot.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.model.ListingData;
import com.ducktapedapps.updoot.model.Thing;
import com.ducktapedapps.updoot.repository.SubmissionRepo;
import com.ducktapedapps.updoot.utils.Constants;
import com.ducktapedapps.updoot.utils.SingleLiveEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SubmissionsVM extends AndroidViewModel implements InfiniteScrollVM {

    private static final String TAG = "SubmissionsVM";
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private SubmissionRepo frontPageRepo;

    private final String subreddit;
    private String after;
    private String sorting;
    private String time;
    private MutableLiveData<List<LinkData>> allSubmissions = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);
    private MutableLiveData<SingleLiveEvent<String>> toastMessage = new MutableLiveData<>(new SingleLiveEvent<>(null));
    private int expandedSubmissionIndex = -1;

    SubmissionsVM(Application application, String subreddit) {
        super(application);
        frontPageRepo = new SubmissionRepo(application);
        after = null;
        time = null;
        this.subreddit = subreddit;
        sorting = Constants.HOT;
        loadNextPage();
    }

    public MutableLiveData<SingleLiveEvent<String>> getToastMessage() {
        return toastMessage;
    }

    @Override
    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<List<LinkData>> getAllSubmissions() {
        return allSubmissions;
    }

    public String getAfter() {
        return this.after;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void loadNextPage() {
        compositeDisposable.add(frontPageRepo
                .loadNextPage(subreddit, sorting, time, after)
                .map(thing -> {
                    if (thing.getData() instanceof ListingData) {
                        after = ((ListingData) thing.getData()).getAfter();
                        List<LinkData> linkDataList = new ArrayList<>();
                        for (Thing linkThing : ((ListingData) thing.getData()).getChildren()) {
                            linkDataList.add(((LinkData) linkThing.getData()));
                        }
                        return linkDataList;
                    } else {
                        throw new Exception("unsupported response");
                    }
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
                .doOnSubscribe(__ -> isLoading.postValue(true))
                .subscribe(submissions -> {
                    allSubmissions.postValue(submissions);
                    isLoading.postValue(false);
                }, throwable -> {
                    Log.e(TAG, "onError: ", throwable);
                    isLoading.postValue(false);
                    toastMessage.postValue(new SingleLiveEvent<>(throwable.getMessage()));
                }));
    }


    public void castVote(int index, int direction) {
        if (allSubmissions.getValue() != null) {
            LinkData data = allSubmissions.getValue().get(index);
            if (data != null) {
                if (data.isArchived()) {
                    toastMessage.setValue(new SingleLiveEvent<>("Submission is archived!"));
                    return;
                }
                if (data.isLocked()) {
                    toastMessage.setValue(new SingleLiveEvent<>("Submission is locked!"));
                    return;
                }

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
                                compositeDisposable.add(d);
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
                                compositeDisposable.add(d);
                            }

                            @Override
                            public void onComplete() {
                                LinkData updatedData = allSubmissions.getValue().get(index);
                                updatedData = updatedData.save();
                                currentSubmissions.set(index, updatedData);
                                allSubmissions.postValue(currentSubmissions);
                                if (updatedData.getSaved()) {
                                    toastMessage.postValue(new SingleLiveEvent<>("Submission saved!"));
                                } else {
                                    toastMessage.postValue(new SingleLiveEvent<>("Submission unsaved!"));
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                LinkData updatedData = allSubmissions.getValue().get(index);
                                updatedData = updatedData.save();
                                currentSubmissions.set(index, updatedData);
                                allSubmissions.postValue(currentSubmissions);
                                toastMessage.postValue(new SingleLiveEvent<>("Error saving submission"));
                            }
                        });
            }
        }
    }

    public void reload(String sort, String time) {
        if (sort == null) {
            sorting = Constants.HOT;
        } else {
            sorting = sort;
            if (time != null) {
                this.time = time;
            }
        }
        after = null;
        allSubmissions.setValue(null);
        loadNextPage();
    }


    public void expandSelfText(int index) {
        List<LinkData> updatedList = allSubmissions.getValue();
        LinkData data = updatedList.get(index);
        if (index == expandedSubmissionIndex) {
            if (data.getSelftext() != null) {
                data = data.toggleSelfTextExpansion();
                updatedList.set(index, data);
                if (!data.isSelfTextExpanded()) expandedSubmissionIndex = -1;
            }
        } else {
            data = data.toggleSelfTextExpansion();
            updatedList.set(index, data);
            if (expandedSubmissionIndex != -1) {
                data = updatedList.get(expandedSubmissionIndex);
                data = data.toggleSelfTextExpansion();
                updatedList.set(expandedSubmissionIndex, data);
            }
            expandedSubmissionIndex = index;
        }
        allSubmissions.setValue(updatedList);
    }

    @Override
    protected void onCleared() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
        super.onCleared();
        Log.i(TAG, "onCleared: ");
    }
}
