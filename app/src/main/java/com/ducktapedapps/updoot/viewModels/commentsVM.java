package com.ducktapedapps.updoot.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ducktapedapps.updoot.model.CommentData;
import com.ducktapedapps.updoot.model.ListingData;
import com.ducktapedapps.updoot.model.thing;
import com.ducktapedapps.updoot.repository.commentsRepo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class commentsVM extends AndroidViewModel {

    private static final String TAG = "commentsVM";
    private commentsRepo repo;
    private MutableLiveData<List<CommentData>> allComments = new MutableLiveData<>();
    private CompositeDisposable disposable;


    public commentsVM(@NonNull Application application) {
        super(application);
        repo = new commentsRepo(application);
        disposable = new CompositeDisposable();
    }

    public MutableLiveData<List<CommentData>> getAllComments() {
        return allComments;
    }

    public void loadComments(String subreddit, String submission_id) {
        repo
                .loadComments(subreddit, submission_id)
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<thing>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onSuccess(thing thing) {
                        if (thing.getData() != null && thing.getData() instanceof ListingData &&
                                !((ListingData) thing.getData()).getChildren().isEmpty() &&
                                ((ListingData) thing.getData()).getChildren().get(0).getData() instanceof CommentData) {
                            List<CommentData> fetchedComments = new ArrayList<>();
                            for (thing comment : ((ListingData) thing.getData()).getChildren()) {
                                if (comment.getData() instanceof CommentData) {
                                    fetchedComments.add((CommentData) comment.getData());
                                }
                            }
                            allComments.postValue(fetchedComments);
                        }
                        Log.i(TAG, "onSuccess: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (!disposable.isDisposed())
            disposable.dispose();
    }
}
