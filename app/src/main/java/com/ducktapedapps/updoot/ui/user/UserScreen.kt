package com.ducktapedapps.updoot.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.ui.comments.FullComment
import com.ducktapedapps.updoot.ui.common.PageEnd
import com.ducktapedapps.updoot.ui.common.PageLoading
import com.ducktapedapps.updoot.ui.common.PageLoadingFailed
import com.ducktapedapps.updoot.ui.subreddit.LargePost
import com.ducktapedapps.updoot.ui.theme.ColorOnScoreBackground
import com.ducktapedapps.updoot.ui.theme.ScoreBackground
import com.ducktapedapps.updoot.ui.user.UserContent.UserComment
import com.ducktapedapps.updoot.ui.user.UserContent.UserPost
import com.ducktapedapps.updoot.utils.Page.*

@Composable
fun UserInfoScreen(viewModel: UserViewModel) {
    val content = viewModel.content.collectAsState(emptyList())
    val currentSection = viewModel.currentSection.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        //TODO put username somewhere
        stickyHeader {
            UserSections(
                sections = UserSection.values().asList(),
                currentSection = currentSection.value,
                onClick = { viewModel.setSection(it) }
            )
        }

        items(content.value) { page ->
            when (page) {
                is ErrorPage -> PageLoadingFailed(
                    performRetry = viewModel::loadPage,
                    message = page.errorReason
                )

                is LoadedPage -> {
                    LaunchedEffect(Unit) { if (page.hasNextPage()) viewModel.loadPage() }
                    page.content.forEach { userContent ->
                        when (userContent) {
                            is UserComment -> FullComment(
                                threadWidth = 2.dp,
                                threadSpacingWidth = 6.dp,
                                singleThreadMode = false,
                                comment = userContent.data,
                                onClickComment = {}
                            )
                            is UserPost -> LargePost(
                                post = userContent.data,
                                onClickMedia = {},
                                openPost = {},
                                openSubreddit = {},
                                openUser = {},
                            )
                        }
                    }
                }
                LoadingPage -> PageLoading()

                End -> PageEnd()
            }
            Divider()
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