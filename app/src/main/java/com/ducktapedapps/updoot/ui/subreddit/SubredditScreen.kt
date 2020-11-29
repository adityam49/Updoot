package com.ducktapedapps.updoot.ui.subreddit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.onActive
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.utils.Media
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT
import com.ducktapedapps.updoot.utils.SubmissionUiType.LARGE
import com.ducktapedapps.updoot.utils.toMedia

@Composable
fun SubredditScreen(
        viewModel: SubmissionsVM,
        openMedia: (Media) -> Unit,
        openComments: (subreddit: String, id: String) -> Unit,
        openOptions: (id: String) -> Unit
) {
    val allSubmissions = viewModel.allSubmissions.collectAsState(initial = emptyList())
    val loading = viewModel.isLoading.collectAsState()
    val postType = viewModel.postViewType.collectAsState(initial = null)
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        LazyColumn {
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
                Divider()
            }
            if (loading.value) item {
                Box(alignment = Alignment.Center, modifier = Modifier.fillParentMaxWidth()) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp).padding(16.dp))
                }
            }
            item { Spacer(Modifier.height(200.dp)) }
        }
    }
}