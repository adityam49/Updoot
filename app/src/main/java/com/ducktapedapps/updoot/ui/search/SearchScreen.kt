package com.ducktapedapps.updoot.ui.search

import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.CoreTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.ui.User
import com.ducktapedapps.updoot.ui.theme.surfaceOnDrawer
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@Composable
fun ComposeSubredditItem(subreddit: Subreddit, openSubreddit: (String) -> Unit) {
    Row(modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { openSubreddit(subreddit.display_name) })
            .padding(8.dp)
    ) {
        Image(asset = vectorResource(id = R.drawable.ic_subreddit_default_24dp), modifier = Modifier.align(Alignment.CenterVertically))
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
        IconButton(onClick = {}) { R.drawable.ic_round_add_circle_24 }
    }

}

@Preview
@Composable
fun previewSearchView() {
    ComposeSearchView(performSearch = {}, modifier = Modifier.fillMaxWidth())
}


@Composable
fun ComposeSearchView(performSearch: (query: String) -> Unit, modifier: Modifier) {
    val (queryString, setQuery) = remember { mutableStateOf(TextFieldValue("")) }

    Surface(
            modifier = modifier,
            color = surfaceOnDrawer,
            contentColor = MaterialTheme.colors.onSurface,
            elevation = 1.dp,
            shape = RoundedCornerShape(24.dp),
    ) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            CoreTextField(
                    maxLines = 1,
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp).weight(0.8f),
                    value = queryString,
                    cursorColor = MaterialTheme.colors.onSecondary,
                    onValueChange = setQuery,
                    imeAction = ImeAction.Search,
                    onTextInputStarted = { controller: SoftwareKeyboardController ->
                        controller.showSoftwareKeyboard()
                    }
            )
            IconButton(
                    onClick = { performSearch(queryString.text) },
                    modifier = Modifier.padding(end = 8.dp).weight(0.2f)
            ) {
                Icon(asset = vectorResource(id = R.drawable.ic_search_24dp))
            }
        }
    }
}


@ExperimentalCoroutinesApi
@FlowPreview
@Composable
fun SearchScreen(
        openSubreddit: (String) -> Unit,
        subredditDAO: SubredditDAO,
        redditClient: IRedditClient,
        currentUser: Flow<User>
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
        ComposeSearchView(viewModel::searchSubreddit, Modifier.fillMaxWidth().wrapContentHeight().padding(8.dp))
        LazyColumnFor(
                items = viewModel.results.collectAsState(initial = emptyList()).value,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            ComposeSubredditItem(it, openSubreddit)
        }
    }
}

