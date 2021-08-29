package com.ducktapedapps.updoot.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.comments.FullComment
import com.ducktapedapps.updoot.common.PageEnd
import com.ducktapedapps.updoot.common.PageLoading
import com.ducktapedapps.updoot.common.PageLoadingFailed
import com.ducktapedapps.updoot.data.remote.model.Trophy
import com.ducktapedapps.updoot.subreddit.LargePost
import com.ducktapedapps.updoot.theme.ColorOnScoreBackground
import com.ducktapedapps.updoot.theme.ScoreBackground
import com.ducktapedapps.updoot.user.UserContent.UserComment
import com.ducktapedapps.updoot.user.UserContent.UserPost
import com.ducktapedapps.updoot.utils.PagingModel.Footer.*

@Composable
fun UserInfoScreen(viewModel: UserViewModel) {
    val pagedData = viewModel.content.collectAsState()
    val currentSection = viewModel.currentSection.collectAsState()
    val userContentSections = viewModel.sections.collectAsState()
    val trophies = viewModel.userTrophies.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        //TODO put username somewhere
        stickyHeader {
            UserTrophies(trophies = trophies.value)
        }
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

@Composable
fun UserTrophies(trophies: List<Trophy>) {

    LazyRow {
        items(trophies) { trophy ->
            Column(Modifier.padding(4.dp)) {
                Image(
                    painter = rememberImagePainter(data=trophy.icon){
                        error(R.drawable.ic_image_error_24dp)
                    },
                    contentDescription = trophy.name,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = trophy.name,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.overline
                )
            }

        }
    }
}