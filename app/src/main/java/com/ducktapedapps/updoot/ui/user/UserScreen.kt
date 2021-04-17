package com.ducktapedapps.updoot.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.comments.FullComment
import com.ducktapedapps.updoot.ui.common.PageEnd
import com.ducktapedapps.updoot.ui.common.PageLoading
import com.ducktapedapps.updoot.ui.common.PageLoadingFailed
import com.ducktapedapps.updoot.ui.subreddit.LargePost
import com.ducktapedapps.updoot.ui.theme.ColorOnScoreBackground
import com.ducktapedapps.updoot.ui.theme.ScoreBackground
import com.ducktapedapps.updoot.ui.user.UserContent.UserComment
import com.ducktapedapps.updoot.ui.user.UserContent.UserPost
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*

@Composable
fun UserInfoScreen(viewModel: UserViewModel) {
    val pagedData = viewModel.content.collectAsState()
    val currentSection = viewModel.currentSection.collectAsState()
    val userContentSections = viewModel.sections.collectAsState()
    LaunchedEffect(key1 = Unit) { viewModel.loadPage() }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        //TODO put username somewhere
        stickyHeader {
            UserSections(
                sections = userContentSections.value,
                currentSection = currentSection.value,
                onClick = { viewModel.setSection(it) }
            )
        }

        itemsIndexed(pagedData.value.content) { index, item ->
            LaunchedEffect(key1 = Unit) {
                with(pagedData.value) {
                    if (index >= content.size - 10 && footer is UnLoadedPage) viewModel.loadPage()
                }
            }
            when (item) {
                is UserComment -> FullComment(
                    threadWidth = 2.dp,
                    threadSpacingWidth = 6.dp,
                    singleThreadMode = false,
                    comment = item.data,
                    onClickComment = {}
                )
                is UserPost -> LargePost(
                    post = item.data,
                    onClickMedia = {},
                    openPost = {},
                    openSubreddit = {},
                    openUser = {},
                )
            }
        }
        item {
            when (val footer = pagedData.value.footer) {
                End -> PageEnd()
                is Error -> PageLoadingFailed(
                    performRetry = viewModel::loadPage,
                    message = footer.exception.message
                        ?: stringResource(id = R.string.something_went_wrong)
                )
                Loading -> PageLoading()
                is UnLoadedPage -> Unit
            }
        }
        item { Spacer(Modifier.height(200.dp)) }
    }
}


@Composable
fun SectionChip(
    section: UserSection,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        color =
        if (isSelected)
            MaterialTheme.colors.ScoreBackground
        else
            MaterialTheme.colors.surface,
        contentColor =
        if (isSelected)
            MaterialTheme.colors.ColorOnScoreBackground
        else
            MaterialTheme.colors.onSurface,
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)

    ) {
        Text(
            text = section.name,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun UserSections(
    sections: List<UserSection>,
    currentSection: UserSection,
    onClick: (UserSection) -> Unit,
) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colors.background) {
        LazyRow {
            items(sections) {
                SectionChip(
                    section = it,
                    isSelected = it == currentSection,
                    onClick = { onClick(it) }
                )
            }
        }
    }
}