package com.ducktapedapps.updoot.ui.comments

import android.text.Spanned
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.LinkMetaDataDAO
import com.ducktapedapps.updoot.data.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.data.local.model.Comment.MoreCommentData
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.data.remote.LinkModel
import com.ducktapedapps.updoot.data.remote.fetchMetaData
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.*
import com.ducktapedapps.updoot.utils.Media
import com.ducktapedapps.updoot.utils.toMedia
import io.noties.markwon.Markwon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class CommentsVM(
        private val repo: CommentsRepo,
        submissionsCacheDAO: SubmissionsCacheDAO,
        private val markwon: Markwon,
        private val id: String,
        private val subreddit_name: String,
        private val linkMetaDataDAO: LinkMetaDataDAO
) : ViewModel() {
    val allComments = repo.visibleComments

    val submissionData: Flow<LinkData> = submissionsCacheDAO
            .observeLinkData(id)
            .distinctUntilChanged()
            .flowOn(Dispatchers.IO)

    val content: Flow<SubmissionContent> = submissionData.transformLatest {
        when (val data = it.toMedia()) {
            is Media.SelfText -> emit(SelfText(markwon.toMarkdown(data.text)))
            is Media.Image -> emit(Image(data))
            is Media.Video -> emit(Video(data))
            is Media.Link -> emitAll(getMetaDataFor(data.url))
            is Media.JustTitle -> emit(JustTitle)
        }
    }.distinctUntilChanged()

    val isLoading = repo.commentsAreLoading

    init {
        loadComments()
    }

    private fun loadComments() {
        viewModelScope.launch {
            repo.loadComments(subreddit_name, id)
        }
    }

    fun toggleChildrenVisibility(index: Int) {
        viewModelScope.launch {
            repo.toggleChildrenCommentVisibility(index)
        }
    }

    fun loadMoreComment(moreCommentData: MoreCommentData, index: Int) {
        viewModelScope.launch {
            repo.fetchMoreComments(id, moreCommentData, index)
        }
    }

    fun castVote(direction: Int, index: Int) = Unit

    private fun getMetaDataFor(url: String): Flow<LinkState> = flow {
        emit(LinkState.LoadingLink(url))
        emitAll(url
                .fetchMetaData(linkMetaDataDAO)
                .catch { error -> emit(LinkState.NoMetaDataLink(url, error.message ?: "")) }
                .map { LinkState.LoadedLink(it) })
    }
}

sealed class SubmissionContent {
    data class Image(val data: Media.Image) : SubmissionContent()
    data class Video(val data: Media.Video) : SubmissionContent()
    data class SelfText(val parsedMarkdown: Spanned) : SubmissionContent()
    sealed class LinkState : SubmissionContent() {
        data class LoadedLink(val linkModel: LinkModel) : LinkState()
        data class LoadingLink(val url: String) : LinkState()
        data class NoMetaDataLink(val url: String, val errorReason: String) : LinkState()
    }

    object JustTitle : SubmissionContent()
}

class CommentsVMFactory @Inject constructor(
        private val commentsRepo: CommentsRepo,
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        private val markwon: Markwon,
        private val linkMetaDataDAO: LinkMetaDataDAO
) : ViewModelProvider.Factory {
    private lateinit var subredditName: String
    private lateinit var id: String

    fun setSubredditAndId(name: String, id: String) {
        subredditName = name
        this.id = id
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            CommentsVM(commentsRepo, submissionsCacheDAO, markwon, id, subredditName, linkMetaDataDAO) as T
}