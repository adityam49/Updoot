package com.ducktapedapps.updoot.viewModels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ducktapedapps.updoot.model.CommentData;
import com.ducktapedapps.updoot.model.ListingData;
import com.ducktapedapps.updoot.model.Thing;
import com.ducktapedapps.updoot.repository.CommentsRepo;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CommentsVM extends AndroidViewModel {

    private static final String TAG = "CommentsVM";
    private CommentsRepo repo;
    private MutableLiveData<List<CommentData>> allComments = new MutableLiveData<>();
    private CompositeDisposable disposable;
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(true);

    public CommentsVM(@NonNull Application application) {
        super(application);
        repo = new CommentsRepo(application);
        disposable = new CompositeDisposable();
    }

    public MutableLiveData<List<CommentData>> getAllComments() {
        return allComments;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadComments(String subreddit, String submission_id) {
        repo
                .loadComments(subreddit, submission_id)
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<Thing>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                        isLoading.postValue(true);
                    }

                    @Override
                    public void onSuccess(Thing thing) {
                        thing.getData();
                        List<CommentData> fetchedComments = new ArrayList<>();
                        if (thing.getData() instanceof ListingData
                                && !((ListingData) thing.getData()).getChildren().isEmpty()
                                && ((ListingData) thing.getData()).getChildren().get(0).getKind().equals("t1")) {
                            for (Thing comment : ((ListingData) thing.getData()).getChildren()) {
                                Log.i(TAG, "child " + comment);
                                if (comment != null && comment.getData() instanceof CommentData) {
                                    fetchedComments.add((CommentData) comment.getData());
                                }
                            }
                            allComments.postValue(fetchedComments);
                            isLoading.postValue(false);
                            Log.i(TAG, "onSuccess: ");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                        isLoading.postValue(false);
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
