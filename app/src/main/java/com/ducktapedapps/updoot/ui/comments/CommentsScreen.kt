package com.ducktapedapps.updoot.ui.comments

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumnForIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.asFlow
import com.ducktapedapps.updoot.data.local.model.Comment
import kotlinx.coroutines.flow.Flow

@Composable
fun CommentsScreen(
        viewModel: CommentsVM
) {
    if (viewModel.isLoading.asFlow().collectAsState(initial = true).value)
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
    else
        AllComments(allComments = viewModel.allComments, loadMoreComments = viewModel::toggleChildrenVisibility)
}


@Composable
fun AllComments(allComments: Flow<List<Comment>>, loadMoreComments: (Int) -> Unit) {
    LazyColumnForIndexed(items = allComments.collectAsState(initial = emptyList()).value) { index: Int, comment: Comment ->
        if (comment is Comment.CommentData)
            FullComment(
                    comment = comment,
                    onClickComment = { loadMoreComments(index) },
                    singleThreadMode = false,
                    threadSpacingWidth = 6.dp,
                    threadWidth = 2.dp,
            )
    }
}

