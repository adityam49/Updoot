package com.ducktapedapps.updoot.ui.comments

import android.text.Spanned
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ducktapedapps.updoot.data.local.SubmissionsCacheDAO
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.data.remote.LinkModel
import com.ducktapedapps.updoot.data.remote.fetchMetaDataFrom
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.*
import com.ducktapedapps.updoot.utils.Media
import com.ducktapedapps.updoot.utils.toMedia
import io.noties.markwon.Markwon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class CommentsVM(
        private val repo: CommentsRepo,
        submissionsCacheDAO: SubmissionsCacheDAO,
        private val markwon: Markwon,
        private val id: String,
        private val subreddit_name: String
) : ViewModel() {
    val allComments = repo.allComments

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
            repo.toggleChildrenCommentVisibility(index, id)
        }
    }

    fun castVote(direction: Int, index: Int) = Unit

    private fun getMetaDataFor(url: String): Flow<LinkState> = flow {
        emit(LinkState.LoadingLink(url))
        emitAll(fetchMetaDataFrom(url)
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

@ExperimentalCoroutinesApi
class CommentsVMFactory @Inject constructor(
        private val commentsRepo: CommentsRepo,
        private val submissionsCacheDAO: SubmissionsCacheDAO,
        private val markwon: Markwon
) : ViewModelProvider.Factory {
    private lateinit var subredditName: String
    private lateinit var id: String

    fun setSubredditAndId(name: String, id: String) {
        subredditName = name
        this.id = id
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            CommentsVM(commentsRepo, submissionsCacheDAO, markwon, id, subredditName) as T
}