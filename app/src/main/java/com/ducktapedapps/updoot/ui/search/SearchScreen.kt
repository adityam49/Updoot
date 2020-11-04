package com.ducktapedapps.updoot.ui.search

import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.CoreTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.User
import com.ducktapedapps.updoot.ui.theme.SurfaceOnDrawer
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Composable
fun ComposeSubredditItem(subreddit: Subreddit, openSubreddit: (String) -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { openSubreddit(subreddit.display_name) })
                    .padding(top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
                asset = vectorResource(id = R.drawable.ic_subreddit_default_24dp),
                modifier = Modifier.padding(start = 16.dp)
        )
        Column(
                modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
        ) {
            Text(text = subreddit.display_name)
            ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.disabled) {
                Text(
                        text = "${
                            subreddit.subscribers?.run { getCompactCountAsString(this) + " Subscribers" }
                        } ${
                            subreddit.accounts_active?.run { "/" + getCompactCountAsString(this) + " Active" } ?: ""
                        }",
                        style = MaterialTheme.typography.caption
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = {}) { Icon(vectorResource(id = R.drawable.ic_round_add_circle_24)) }
    }

}

@Preview
@Composable
private fun previewSearchView() {
    ComposeSearchView(performSearch = {}, modifier = Modifier.fillMaxWidth(), {}, flow { })
}


@Composable
fun ComposeSearchView(
        performSearch: (query: String) -> Unit,
        modifier: Modifier,
        goBack: () -> Unit,
        isLoading: Flow<Boolean>
) {
    val (queryString, setQuery) = remember { mutableStateOf(TextFieldValue("")) }
    Card(
            modifier = modifier.padding(start = 0.dp),
            shape = RoundedCornerShape(50),
            backgroundColor = MaterialTheme.colors.SurfaceOnDrawer,
    ) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = goBack) {
                Icon(asset = Icons.Default.ArrowBack)
            }
            CoreTextField(
                    softWrap = false,
                    maxLines = 1,
                    cursorColor = MaterialTheme.colors.primary,
                    modifier = Modifier.weight(0.8f).padding(start = 8.dp, end = 8.dp),
                    value = queryString,
                    onValueChange = setQuery,
                    imeAction = ImeAction.Search,
            )
            if (queryString.text.isNotBlank()) IconButton(
                    icon = { Icon(asset = Icons.Default.Clear) },
                    onClick = { setQuery(TextFieldValue("")) },
            )

            if (isLoading.collectAsState(initial = true).value)
                CircularProgressIndicator(
                        modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.CenterVertically)
                                .padding(end = 4.dp)
                )
            else
                IconButton(
                        icon = { Icon(asset = Icons.Default.Search) },
                        onClick = { performSearch(queryString.text) },
                )
        }
    }
}


@Composable
fun SearchScreen(
        goBack: () -> Unit,
        openSubreddit: (String) -> Unit,
        subredditDAO: SubredditDAO,
        redditClient: IRedditClient,
        currentUser: Flow<User>,
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember {
        SearchVM(
                redditClient = redditClient,
                subredditDAO = subredditDAO,
                currentUser = currentUser,
                coroutineScope = coroutineScope
        )
    }
    Column(
            modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
    ) {
        ComposeSearchView(
                performSearch = viewModel::searchSubreddit,
                modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                goBack = goBack,
                isLoading = viewModel.searchQueryLoading
        )
        LazyColumnFor(
                items = viewModel.results.collectAsState(initial = emptyList()).value,
                modifier = Modifier.fillMaxWidth()
        ) {
            ComposeSubredditItem(it, openSubreddit)
        }
    }
}
