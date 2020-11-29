package com.ducktapedapps.updoot.ui.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
            Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
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
    ConstraintLayout(modifier) {
        val (backButton, searchField, searchActions) = createRefs()
        BackButton(
                Modifier.constrainAs(backButton) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }, goBack)

        SearchField(
                modifier = Modifier.constrainAs(searchField) {
                    start.linkTo(backButton.end, margin = 8.dp)
                    end.linkTo(searchActions.start, margin = 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                },
                queryString = queryString,
                setQuery = setQuery,
                performSearch = performSearch
        )
        SearchActions(
                modifier = Modifier.constrainAs(searchActions) {
                    start.linkTo(searchField.end)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
                queryString = queryString,
                setQuery = setQuery,
                isLoading = isLoading,
                performSearch = performSearch
        )

    }
}

@Composable
fun SearchField(
        modifier: Modifier,
        queryString: TextFieldValue,
        setQuery: (TextFieldValue) -> Unit,
        performSearch: (query: String) -> Unit,
) {
    Surface(
            modifier = modifier,
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colors.SurfaceOnDrawer,
    ) {
        BasicTextField(
                maxLines = 1,
                cursorColor = MaterialTheme.colors.primary,
                value = queryString,
                modifier = modifier.padding(start = 32.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
                onValueChange = setQuery,
                onImeActionPerformed = { performSearch(queryString.text) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search)
        )
    }

}

@Composable
fun SearchActions(
        modifier: Modifier,
        queryString: TextFieldValue,
        setQuery: (TextFieldValue) -> Unit,
        isLoading: Flow<Boolean>,
        performSearch: (query: String) -> Unit
) {
    Surface(
            color = MaterialTheme.colors.SurfaceOnDrawer,
            modifier = modifier.clip(RoundedCornerShape(50)).animateContentSize()
    ) {
        Row {
            if (queryString.text.isNotBlank()) IconButton(
                    icon = { Icon(asset = Icons.Default.Clear) },
                    onClick = { setQuery(TextFieldValue("")) },
            )

            if (isLoading.collectAsState(initial = true).value)
                CircularProgressIndicator(
                        modifier = Modifier
                                .size(48.dp)
                                .padding(8.dp)
                                .align(Alignment.CenterVertically)
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
private fun BackButton(modifier: Modifier, goBack: () -> Unit) {
    Surface(
            color = MaterialTheme.colors.SurfaceOnDrawer,
            modifier = modifier.clip(CircleShape),
    ) {
        IconButton(onClick = goBack) { Icon(asset = Icons.Default.ArrowBack) }
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
                        .padding(8.dp)
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
