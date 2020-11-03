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

@Composable
fun ComposeSubredditItem(subreddit: Subreddit, openSubreddit: (String) -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { openSubreddit(subreddit.display_name) })
                    .padding(top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(asset = vectorResource(id = R.drawable.ic_subreddit_default_24dp))
        Column(
                modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.CenterVertically)
        ) {
            Text(text = subreddit.display_name, style = MaterialTheme.typography.caption)

            ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.disabled) {
                Text(
                        text = "${
                            subreddit.subscribers?.run { getCompactCountAsString(this) + " Subscribers" }
                        } ${
                            subreddit.accounts_active?.run { "/" + getCompactCountAsString(this) + " Active" } ?: ""
                        }",
                        style = MaterialTheme.typography.subtitle2
                )

            }
        }
        IconButton(onClick = {}) { vectorResource(id = R.drawable.ic_round_add_circle_24) }
    }

}

@Preview
@Composable
fun previewSearchView() {
    ComposeSearchView(performSearch = {}, modifier = Modifier.fillMaxWidth(), {})
}


@Composable
fun ComposeSearchView(
        performSearch: (query: String) -> Unit,
        modifier: Modifier,
        goBack: () -> Unit
) {
    val (queryString, setQuery) = remember { mutableStateOf(TextFieldValue("")) }
    Card(
            modifier = modifier,
            shape = RoundedCornerShape(50),
            backgroundColor = MaterialTheme.colors.SurfaceOnDrawer,
    ) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = goBack) {
                Icon(asset = Icons.Default.ArrowBack)
            }
            CoreTextField(
                    maxLines = 1,
                    modifier = Modifier.weight(0.8f).padding(start = 16.dp, end = 8.dp),
                    value = queryString,
                    onValueChange = setQuery,
                    imeAction = ImeAction.Search,
            )
            IconButton(
                    icon = { Icon(asset = Icons.Default.Search) },
                    onClick = { performSearch(queryString.text) },
            )
            if (queryString.text.isNotBlank()) IconButton(
                    icon = { Icon(asset = Icons.Default.Clear) },
                    onClick = { setQuery(TextFieldValue("")) },
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
                    .padding(8.dp)
    ) {
        ComposeSearchView(
                performSearch = viewModel::searchSubreddit,
                modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                goBack = goBack
        )
        LazyColumnFor(
                items = viewModel.results.collectAsState(initial = emptyList()).value,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            ComposeSubredditItem(it, openSubreddit)
        }
    }
}
