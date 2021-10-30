package com.ducktapedapps.updoot.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.NavigationDirections.SubredditScreenNavigation
import com.ducktapedapps.updoot.R


@Composable
fun SearchScreen(
    publishEvent: (Event) -> Unit,
) {
    val viewModel: ExploreVM = hiltViewModel<ExploreVMImpl>()
    val viewState = viewModel.viewState.collectAsState()
    LazyColumn {
        stickyHeader {
            SearchBar(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                setQuery = viewModel::searchSubreddit,
                query = viewState.value.searchQuery
            )
        }
        items(viewState.value.subredditSearchResults) { subredditItem ->
            SubredditItem(
                subreddit = subredditItem,
                openSubreddit = {
                    publishEvent(
                        ScreenNavigationEvent(
                            SubredditScreenNavigation.open(
                                subredditItem.subredditName
                            )
                        )
                    )
                }
            )
        }
    }
}

@Composable
@Preview(backgroundColor = 0xFFFFFF)
fun SearchBarPreview() {
    SearchBar(setQuery = { _ -> }, query = "")
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    setQuery: (String) -> Unit,
    query: String,
) {
    Surface(
        elevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50)
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            value = query,
            onValueChange = setQuery,
            label = {
                Text(text = "Search Subreddits")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
            ),
        )
    }
}

@Composable
private fun SubredditItem(
    subreddit: SearchedSubredditResultsUiModel,
    openSubreddit: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { openSubreddit(subreddit.subredditName) })
            .padding(top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = rememberImagePainter(data = subreddit.icon) {
                error(R.drawable.ic_subreddit_default_24dp)
                transformations(CircleCropTransformation())
            },
            contentDescription = stringResource(R.string.subreddit_icon),
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .size(32.dp)
        )
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = subreddit.subredditName)
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                Text(
                    text = "${subreddit.subscriberCount} ${" • " + subreddit.age}",
                    style = MaterialTheme.typography.caption
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}


