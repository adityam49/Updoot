package com.ducktapedapps.updoot.ui.comments

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.model.CommentData
import io.reactivex.SingleObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

const val TAG = "CommentsVM"

class CommentsVM(application: Application, id: String, subreddit_name: String) : AndroidViewModel(application) {
    private val repo = CommentsRepo(application)
    private val disposable = CompositeDisposable()
    private val _allComments = MutableLiveData<List<CommentData>>()
    private val _isLoading = MutableLiveData(true)

    val allComments: LiveData<List<CommentData>> = _allComments
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadComments(subreddit_name, id)
    }

    fun loadComments(subreddit: String, submission_id: String) {
        repo
                .loadComments(subreddit, submission_id)
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<List<CommentData>> {
                    override fun onSubscribe(d: Disposable) {
                        disposable.add(d)
                        _isLoading.postValue(true)
                    }

                    override fun onSuccess(commentDataList: List<CommentData>) {
                        _allComments.postValue(commentDataList)
                        _isLoading.postValue(false)
                    }

                    override fun onError(e: Throwable) {
                        Log.e(TAG, "onError: ", e)
                        _isLoading.postValue(false)
                    }
                })
    }

    fun toggleChildrenVisibility(index: Int) {
        val list = allComments.value?.toMutableList()
        if (list != null) {
            val parentComment = list[index]
            if (parentComment.replies.isNotEmpty()) {
                list[index] = parentComment.copy(repliesExpanded = !parentComment.repliesExpanded)
                if (!parentComment.repliesExpanded) {
                    list.addAll(index + 1, recursiveChildrenExpansion(parentComment.replies))
                } else {
                    if (parentComment.replies.isNotEmpty()) {
                        val commentsToBeRemoved = mutableListOf<CommentData>()
                        for (i in index + 1..list.size - 1) {
                            if (list[i].depth > parentComment.depth) commentsToBeRemoved.add(list[i])
                            else break
                        }
                        list.removeAll(commentsToBeRemoved)
                    }
                }
                _allComments.value = list
            }
        }

    }

    private fun recursiveChildrenExpansion(list: List<CommentData>): List<CommentData> {
        val updateList = mutableListOf<CommentData>()
        for (comment in list) {
            updateList.add(comment.copy(repliesExpanded = !comment.repliesExpanded))
            if (comment.replies.isNotEmpty()) updateList.addAll(recursiveChildrenExpansion(comment.replies))
        }
        return updateList
    }

    override fun onCleared() {
        super.onCleared()
        if (!disposable.isDisposed)
            disposable.dispose()
    }
}

