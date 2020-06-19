package com.ducktapedapps.updoot.ui.comments

import android.util.Log
import androidx.lifecycle.*
import com.ducktapedapps.updoot.api.local.submissionsCache.SubmissionsCacheDAO
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.utils.linkMetaData.extractMetaData
import com.ducktapedapps.updoot.utils.linkMetaData.fetchMetaDataFrom
import com.ducktapedapps.updoot.utils.linkMetaData.toLinkModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommentsVM(
        private val repo: CommentsRepo,
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        val id: String,
        subreddit_name: String
) : ViewModel() {
    val allComments = repo.allComments

    private val _contentLoading = MutableLiveData(true)
    private val _content = MutableLiveData<CommentScreenContent>()
    val content: LiveData<CommentScreenContent> = _content

    private val _submissionData = MutableLiveData<LinkData>()
    val submissionData: LiveData<LinkData> = _submissionData

    val isLoading = MediatorLiveData<Boolean>().apply {
        var commentsLoading = false
        var contentLoading = false
        addSource(repo.commentsAreLoading) {
            commentsLoading = it
            postValue(commentsLoading || contentLoading)
        }
        addSource(_contentLoading) {
            contentLoading = it
            postValue(commentsLoading || contentLoading)
        }
    }

    init {
        viewModelScope.apply {
            async {
                loadSubmissionData()
                submissionData.value?.let { loadContent(it) }
            }
            async { loadComments(subreddit_name, id) }

        }
    }

    private fun loadComments(subreddit: String, submission_id: String) {
        viewModelScope.launch {
            repo.loadComments(subreddit, submission_id)
        }
    }

    fun toggleChildrenVisibility(index: Int) {
        viewModelScope.launch {
            repo.toggleChildrenCommentVisibility(index, id)
        }
    }

    fun castVote(direction: Int, index: Int) = Unit

    private suspend fun loadContent(linkData: LinkData) {
        if (!linkData.selftext.isNullOrBlank()) {
            _content.postValue(linkData)
            _contentLoading.value = false
        } else loadLinkMetaData()
    }

    private suspend fun loadSubmissionData() {
        withContext(Dispatchers.IO) {
            _submissionData.postValue(submissionsCacheDAO.getLinkData(id).also {
                Log.d("CommentsVM", "loadSubmissionData: $it")
            })
        }
    }

    private suspend fun loadLinkMetaData() {
        try {
            withContext(Dispatchers.IO) {
                _contentLoading.postValue(true)
                loadLinkMetaDataSuccessfully()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private suspend fun loadLinkMetaDataSuccessfully() {
        val url = submissionData.value!!.url
        val htmlResponse = fetchMetaDataFrom(url)
        _content.postValue(htmlResponse.extractMetaData().toLinkModel(url).also {
            Log.d("CommentsVM", "loadLinkMetaDataSuccessfully: $it")
        })
        _contentLoading.postValue(false)
    }
}

