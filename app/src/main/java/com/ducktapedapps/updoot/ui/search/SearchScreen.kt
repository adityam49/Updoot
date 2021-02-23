package com.ducktapedapps.updoot.ui.search

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.transform.CircleCropTransformation
import com.ducktapedapps.updoot.R
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.imageloading.ImageLoadState
import kotlinx.coroutines.flow.Flow


@Composable
fun SearchScreen(
    goBack: () -> Unit,
    openSubreddit: (String) -> Unit,
    viewModel: SearchVM,
) {
    val searchResults = viewModel.results.collectAsState(initial = emptyList()).value
    val (queryString, setFieldQueryValue) = remember { mutableStateOf(TextFieldValue("")) }
    val setQuery: (TextFieldValue) -> Unit = { value ->
        viewModel.searchSubreddit(value.text)
        setFieldQueryValue(value)
    }
    val showNsfw = viewModel.includeNsfw.collectAsState(true)
    Column(modifier = Modifier.fillMaxSize()) {
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
            toggleNsfwPref = { viewModel.toggleIncludeNsfw() }
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                searchResults.forEach { SubredditItem(it, openSubreddit) }
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
    Card(
        modifier = modifier,
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
        CoilImage(
            data = subreddit.icon,
            requestBuilder = {
                transformations(CircleCropTransformation())
            },
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .size(32.dp)
        ) { state ->
            when (state) {
                is ImageLoadState.Success -> Image(
                    painter = state.painter,
                    contentDescription = stringResource(R.string.subreddit_icon)
                )
                else -> Icon(
                    painter = painterResource(id = R.drawable.ic_subreddit_default_24dp),
                    contentDescription = stringResource(id = R.string.subreddit_icon)
                )
            }
        }
        Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(start = 16.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = subreddit.subredditName)
            Providers(LocalContentAlpha provides ContentAlpha.disabled) {
                Text(
                    text = "${subreddit.subscriberCount} ${" • " + subreddit.age}",
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
            }, goBack
        )

        SearchField(
            modifier = Modifier.constrainAs(searchField) {
                start.linkTo(backButton.end, margin = 8.dp)
                end.linkTo(searchActions.start, margin = 8.dp)
                top.linkTo(parent.top)
                bottom.linkTo(parent.bottom)
                width = Dimension.fillToConstraints
                height = Dimension.fillToConstraints
            }.focusable(true),
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(50),
    ) {
        BasicTextField(
            value = queryString,
            onValueChange = setQuery,
            singleLine = true,
            cursorColor = MaterialTheme.colors.primary,
            modifier = modifier.padding(start = 32.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search,
            ),
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
    Card(
        shape = RoundedCornerShape(50),
        modifier = modifier
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
    Card(
        modifier = modifier,
        shape = CircleShape,
    ) {
        IconButton(
            onClick = goBack,
            content = {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = Icons.Default.ArrowBack.name
                )
            }
        )
    }
}