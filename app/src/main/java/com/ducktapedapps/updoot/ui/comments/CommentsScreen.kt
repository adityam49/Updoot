package com.ducktapedapps.updoot.ui.comments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.ducktapedapps.updoot.data.local.model.Comment
import com.ducktapedapps.updoot.data.local.model.Comment.CommentData
import com.ducktapedapps.updoot.data.local.model.Comment.MoreCommentData
import kotlinx.coroutines.flow.Flow

@Composable
fun CommentsScreen(viewModel: CommentsVM) {
    Box {
        if (viewModel.isLoading.asFlow().collectAsState(initial = true).value)
            CircularProgressIndicator(
                    modifier = Modifier.size(48.dp).align(Alignment.Center)
            )
        AllComments(
                allComments = viewModel.allComments,
                toggleChildrenVisibility = viewModel::toggleChildrenVisibility,
                loadMoreComments = viewModel::loadMoreComment
        )
    }
}


@Composable
fun AllComments(
        allComments: Flow<List<Comment>>,
        toggleChildrenVisibility: (Int) -> Unit,
        loadMoreComments: (MoreCommentData, Int) -> Unit
) {
    LazyColumnForIndexed(
            items = allComments.collectAsState(initial = emptyList()).value
    ) { index: Int, comment: Comment ->
        when (comment) {
            is CommentData -> FullComment(
                    comment = comment,
                    onClickComment = { toggleChildrenVisibility(index) },
                    singleThreadMode = false, // TODO : get value from preference storage
                    threadSpacingWidth = 6.dp,
                    threadWidth = 2.dp,
            )
            is MoreCommentData -> MoreComment(
                    data = comment,
                    loadMoreComments = { loadMoreComments(comment, index) },
                    singleThreadMode = false, // TODO : get value from preference storage
                    threadSpacingWidth = 6.dp,
                    threadWidth = 2.dp,
            )
        }
    }
}

