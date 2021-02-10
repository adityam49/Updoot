package com.ducktapedapps.updoot.ui.search

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.SubredditDAO
import com.ducktapedapps.updoot.data.remote.model.RemoteSubreddit
import com.ducktapedapps.updoot.ui.theme.SurfaceOnDrawer
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.getCompactAge
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import dev.chrisbanes.accompanist.glide.GlideImage
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import kotlinx.coroutines.flow.Flow

@Composable
fun SearchScreen(
        goBack: () -> Unit,
        openSubreddit: (String) -> Unit,
        subredditDAO: SubredditDAO,
        redditClient: IRedditClient,
) {
    val viewModel = remember {
        SearchVM(
                redditClient = redditClient,
                subredditDAO = subredditDAO,
        )
    }
    val searchResults = viewModel.results.collectAsState(initial = emptyList()).value
    val (queryString, setFieldQueryValue) = remember { mutableStateOf(TextFieldValue("")) }
    val setQuery: (TextFieldValue) -> Unit = { value ->
        viewModel.searchSubreddit(value.text)
        setFieldQueryValue(value)
    }
    val showNsfw = viewModel.includeOver18.collectAsState(true)
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
                isLoading = viewModel.searchQueryLoading,
                queryString = queryString,
                setQuery = setQuery,
        )
        NsfwCheckRow(
                modifier = Modifier
                        .padding(8.dp)
                        .height(48.dp)
                        .fillMaxWidth(),
                showNsfw = showNsfw.value,
                toggleNsfwPref = { viewModel.toggleIncludeOver18() }
        )
        LazyColumn(modifier = Modifier.fillMaxWidth()
        ) {
            item {
                searchResults.forEach {
                    ComposeSubredditItem(it, openSubreddit)
                }
            }
        }
        Spacer(
                Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f)
        )
    }
}

@Composable
fun NsfwCheckRow(
        modifier: Modifier = Modifier,
        showNsfw: Boolean,
        toggleNsfwPref: (Boolean) -> Unit
) {
    Surface(
            modifier = modifier,
            color = MaterialTheme.colors.SurfaceOnDrawer,
            shape = RoundedCornerShape(8.dp),
    ) {
        Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "Show NSFW results")
            Checkbox(checked = showNsfw, onCheckedChange = {
                toggleNsfwPref(
                        true /* won't matter as viewModel is changing this value */
                )
            })
        }
    }
}

@Composable
fun ComposeSubredditItem(remoteSubreddit: RemoteSubreddit, openSubreddit: (String) -> Unit) {
    Row(
            modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { openSubreddit(remoteSubreddit.display_name) })
                    .padding(top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Log.i("SearchScreen", "url : ${remoteSubreddit.community_icon}")
        GlideImage(
                data = remoteSubreddit.community_icon,
                requestBuilder = {
                    centerInside().circleCrop()
                },
                modifier = Modifier
                        .padding(start = 16.dp)
                        .preferredSize(32.dp)
        ) { imageLoadState ->
            if (imageLoadState is ImageLoadState.Error)
                loadVectorResource(id = R.drawable.ic_subreddit_default_24dp).resource.resource?.let {
                    Image(imageVector = it, contentDescription = "Subreddit Icon")
                }
        }
        Column(
                modifier = Modifier
                        .wrapContentWidth()
                        .padding(start = 16.dp)
                        .align(Alignment.CenterVertically)
        ) {
            Text(text = remoteSubreddit.display_name)
            Providers(AmbientContentAlpha provides ContentAlpha.disabled) {
                Text(
                        text = "${
                            remoteSubreddit.subscribers?.run { getCompactCountAsString(this) + " Subscribers" }
                        } ${
                            " • " + getCompactAge(remoteSubreddit.created)
                        }",
                        style = MaterialTheme.typography.caption
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }

}

@Composable
fun ComposeSearchView(
        performSearch: (query: String) -> Unit,
        queryString: TextFieldValue,
        setQuery: (TextFieldValue) -> Unit,
        modifier: Modifier,
        goBack: () -> Unit,
        isLoading: Flow<Boolean>
) {
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
            contentColor = MaterialTheme.colors.onSecondary
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
            modifier = modifier
                    .clip(RoundedCornerShape(50))
                    .animateContentSize()
    ) {
        Row {
            if (queryString.text.isNotBlank()) IconButton(
                    onClick = { setQuery(TextFieldValue("")) },
                    content = { Icon(Icons.Default.Clear, contentDescription = "Clear Icon") }
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
                        onClick = { performSearch(queryString.text) },
                        content = { Icon(Icons.Default.Search, contentDescription = "Search Icon") }
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
        IconButton(
                onClick = goBack,
                content = { Icon(Icons.Default.ArrowBack, contentDescription = Icons.Default.ArrowBack.name) }
        )
    }
}