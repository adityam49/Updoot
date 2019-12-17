package com.ducktapedapps.updoot.ui.subreddit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.InfiniteScrollVM
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import io.reactivex.CompletableObserver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class SubmissionsVM internal constructor(application: Application, subreddit: String) : AndroidViewModel(application), InfiniteScrollVM {
    private val compositeDisposable = CompositeDisposable()
    private val frontPageRepo: SubmissionRepo = SubmissionRepo(application)
    override var after: String? = null
    private var expandedSubmissionIndex = -1
    private var sorting: String
    private var time: String?
    val subreddit: String
    override val isLoading = MutableLiveData(true)

    private val _allSubmissions = MutableLiveData<MutableList<LinkData>>(ArrayList())
    private val _toastMessage = MutableLiveData(SingleLiveEvent<String?>(null))
    val allSubmissions: LiveData<MutableList<LinkData>> = _allSubmissions
    val toastMessage: LiveData<SingleLiveEvent<String?>> = _toastMessage

    override fun loadNextPage() {
        compositeDisposable.add(frontPageRepo
                .loadNextPage(subreddit, sorting, time, after)
                .map<MutableList<LinkData>> { response: Pair<List<LinkData>?, String?> ->
                    after = response.second
                    val submissions: MutableList<LinkData> = _allSubmissions.value
                            ?: mutableListOf()
                    submissions.addAll(response.first ?: listOf())
                    submissions
                }
                .subscribeOn(Schedulers.io())
                .doOnSubscribe { isLoading.postValue(true) }
                .subscribe({ submissions: MutableList<LinkData>? ->
                    _allSubmissions.postValue(submissions)
                    isLoading.postValue(false)
                }) { throwable: Throwable ->
                    isLoading.postValue(false)
                    _toastMessage.postValue(SingleLiveEvent(throwable.message))
                })
    }

    fun castVote(index: Int, direction: Int) {
        if (_allSubmissions.value != null) {
            val data = _allSubmissions.value!![index]
            if (data.archived) {
                _toastMessage.value = SingleLiveEvent("Submission is archived!")
                return
            }
            if (data.locked) {
                _toastMessage.value = SingleLiveEvent("Submission is locked!")
                return
            }
            val currentSubmissions = _allSubmissions.value
            frontPageRepo
                    .castVote(data, direction)
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {
                            var updateData = currentSubmissions!![index]
                            updateData = updateData.vote(direction)
                            currentSubmissions[index] = updateData
                            _allSubmissions.postValue(currentSubmissions)
                            compositeDisposable.add(d)
                        }

                        override fun onComplete() {}
                        override fun onError(throwable: Throwable) {
                            var originalData = currentSubmissions!![index]
                            originalData = originalData.vote(direction)
                            currentSubmissions[index] = originalData
                            _allSubmissions.postValue(currentSubmissions)
                        }
                    })

        }
    }

    fun toggleSave(index: Int) {
        if (_allSubmissions.value != null) {
            val data = _allSubmissions.value!![index]
            val currentSubmissions = _allSubmissions.value
            frontPageRepo
                    .save(data)
                    .subscribeOn(Schedulers.io())
                    .subscribe(object : CompletableObserver {
                        override fun onSubscribe(d: Disposable) {
                            compositeDisposable.add(d)
                        }

                        override fun onComplete() {
                            var updatedData = _allSubmissions.value!![index]
                            updatedData = updatedData.save()
                            currentSubmissions!![index] = updatedData
                            _allSubmissions.postValue(currentSubmissions)
                            if (updatedData.saved) {
                                _toastMessage.postValue(SingleLiveEvent("Submission saved!"))
                            } else {
                                _toastMessage.postValue(SingleLiveEvent("Submission unsaved!"))
                            }
                        }

                        override fun onError(e: Throwable) {
                            var updatedData = _allSubmissions.value!![index]
                            updatedData = updatedData.save()
                            currentSubmissions!![index] = updatedData
                            _allSubmissions.postValue(currentSubmissions)
                            _toastMessage.postValue(SingleLiveEvent("Error saving submission"))
                        }
                    })
        }
    }

    fun reload(sort: String?, time: String?) {
        sorting = sort ?: ""
        this.time = time
        after = null
        _allSubmissions.value = null
        loadNextPage()
    }

    fun expandSelfText(index: Int) {
        val updatedList = _allSubmissions.value
        if (updatedList?.get(index) != null) {
            var data = updatedList[index]
            if (index == expandedSubmissionIndex) {
                if (data.selftext != null) {
                    data = data.toggleSelfTextExpansion()
                    updatedList[index] = data
                    if (!data.isSelfTextExpanded) expandedSubmissionIndex = -1
                }
            } else {
                data = data.toggleSelfTextExpansion()
                updatedList[index] = data
                if (expandedSubmissionIndex != -1) {
                    data = updatedList[expandedSubmissionIndex]
                    data = data.toggleSelfTextExpansion()
                    updatedList[expandedSubmissionIndex] = data
                }
                expandedSubmissionIndex = index
            }
        }
        _allSubmissions.value = updatedList
    }

    override fun onCleared() {
        super.onCleared()
        if (compositeDisposable.isDisposed.not()) {
            compositeDisposable.clear()
        }
    }

    init {
        after = null
        time = null
        this.subreddit = subreddit
        sorting = ""
        loadNextPage()
    }
}