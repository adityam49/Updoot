package com.ducktapedapps.updoot.subreddit.options

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ducktapedapps.updoot.R.drawable
import com.ducktapedapps.updoot.utils.PostViewType
import com.ducktapedapps.updoot.utils.PostViewType.COMPACT
import com.ducktapedapps.updoot.utils.PostViewType.LARGE

@Composable
fun SubredditOptions(
    modifier: Modifier = Modifier,
    subredditName: String,
) {
    val viewModel: SubredditOptionsVM = hiltViewModel<SubredditOptionsVMImpl>().apply {
        setSubredditName(subredditName)
    }
    val viewState = viewModel.viewState.collectAsState()
    Column(modifier = modifier) {
        SubredditViewType(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.togglePostViewType() }
                .padding(32.dp),
            currentViewType = viewState.value.postType
        )
    }
}

@Composable
private fun SubredditViewType(
    modifier: Modifier = Modifier,
    currentViewType: PostViewType,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            modifier = Modifier.padding(end = 16.dp),
            painter = painterResource(
                id = when (currentViewType) {
                    COMPACT -> drawable.ic_list_view_24dp
                    LARGE -> drawable.ic_card_view_24dp
                }
            ),
            contentDescription = currentViewType.name
        )
        Text(text = currentViewType.name)
    }
}