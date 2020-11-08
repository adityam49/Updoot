package com.ducktapedapps.updoot.ui.subreddit.options

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.onActive
import androidx.compose.ui.Modifier
import com.ducktapedapps.updoot.ui.subreddit.CompactPost
import com.ducktapedapps.updoot.ui.subreddit.LargePost
import com.ducktapedapps.updoot.ui.subreddit.SubmissionsVM
import com.ducktapedapps.updoot.utils.Media
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT
import com.ducktapedapps.updoot.utils.SubmissionUiType.LARGE
import com.ducktapedapps.updoot.utils.toMedia

@ExperimentalLazyDsl
@Composable
fun SubredditScreen(
        viewModel: SubmissionsVM,
        openMedia: (Media) -> Unit,
        openComments: (subreddit: String, id: String) -> Unit,
        openOptions: (id: String) -> Unit
) {
    val allSubmissions = viewModel.allSubmissions.collectAsState(initial = emptyList())
    val loading = viewModel.isLoading
    val postType = viewModel.postViewType.collectAsState(initial = null)
    LazyColumn {
        if (loading.value) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        itemsIndexed(items = allSubmissions.value) { index, item ->
            onActive {
                viewModel.lastScrollPosition = index
                //TODO move paging stuff to viewModel
                if (index >= allSubmissions.value.size - 10 && viewModel.hasNextPage() && !viewModel.isLoading.value) viewModel.loadPage()
            }
            when (postType.value) {
                COMPACT -> CompactPost(
                        linkData = item,
                        onClickMedia = { openMedia(item.toMedia()) },
                        onClickPost = { openComments(item.subredditName, item.name) },
                        openOptions = { openOptions(item.name) }
                )
                LARGE -> LargePost(
                        linkData = item,
                        onClickMedia = { openMedia(item.toMedia()) },
                        openPost = { openComments(item.subredditName, item.name) },
                        openOptions = { openOptions(item.name) }
                )
                null -> Unit
            }
        }
    }
}