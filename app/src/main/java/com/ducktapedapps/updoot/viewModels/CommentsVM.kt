package com.ducktapedapps.updoot.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.repository.CommentsRepo
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

const val TAG = "CommentsVM"

class CommentsVM(application: Application) : AndroidViewModel(application) {
    private val repo = CommentsRepo(application)
    private val disposable = CompositeDisposable()
    val allComments = MutableLiveData<List<CommentData>>()
    val isLoading = MutableLiveData(true)

    fun loadComments(subreddit: String, submission_id: String) {
        repo
                .loadComments(subreddit, submission_id)
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<List<CommentData>> {
                    override fun onSubscribe(d: Disposable) {
                        disposable.add(d)
                        isLoading.postValue(true)
                    }

                    override fun onSuccess(commentDataList: List<CommentData>) {
                        allComments.postValue(commentDataList)
                        isLoading.postValue(false)
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: ", e)
                        isLoading.postValue(false)
                    }
                })
    }

    fun toggleChildrenVisibility(index: Int) {
        //TODO comment children visibility`
    }

    override fun onCleared() {
        super.onCleared()
        if (!disposable.isDisposed)
            disposable.dispose()
    }
}

