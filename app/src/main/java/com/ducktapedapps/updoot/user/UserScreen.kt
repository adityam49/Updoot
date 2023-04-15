package com.ducktapedapps.updoot.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.NavigationDirections.CommentScreenNavigation
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
fun UserInfoScreen(
    userName: String,
    publishEvent: (Event) -> Unit,
) {
    val viewModel: UserViewModel = hiltViewModel<UserViewModelImpl>().apply {
        setUserName(userName)
    }

    val viewState = viewModel.viewState.collectAsState()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        //TODO put username somewhere
        stickyHeader {
            UserTrophies(trophies = viewState.value.userTrophies)
        }
        stickyHeader {
            UserSections(
                sections = viewState.value.sections,
                currentSection = viewState.value.currentSection,
                onClick = viewModel::setSection
            )
        }

        itemsIndexed(viewState.value.content.content) { index, item ->
            LaunchedEffect(key1 = Unit) {
                with(viewState.value.content) {
                    if (index >= content.size - 10 && footer is UnLoadedPage) viewModel.loadPage()
                }
            }
            when (item) {
                is UserComment -> FullComment(
                    threadWidth = 2.dp,
                    threadSpacingWidth = 6.dp,
                    singleThreadMode = false,
                    comment = item.data,
                    onClickComment = {
                        publishEvent(Event.ToastEvent(item.data.body ?: ""))
                    }
                )
                is UserPost -> LargePost(
                    post = item.data,
                    publishEvent = {
                        publishEvent(
                            ScreenNavigationEvent(
                                CommentScreenNavigation.open(
                                    item.data.subredditName, item.data.id
                                )
                            )
                        )
                    },
                    showPostOptions = {}
                )
            }
        }
        item {
            when (val footer = viewState.value.content.footer) {
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
            MaterialTheme.colorScheme.ScoreBackground
        else
            MaterialTheme.colorScheme.surface,
        contentColor =
        if (isSelected)
            MaterialTheme.colorScheme.ColorOnScoreBackground
        else
            MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize()
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)

    ) {
        Text(
            text = section.name,
            style = MaterialTheme.typography.labelMedium,
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
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.background) {
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
                AsyncImage(
                    model = trophy.icon,
                    error = painterResource(id = R.drawable.ic_image_error_24dp),
                    contentDescription = trophy.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(shape = CircleShape)
                )
                Text(
                    text = trophy.name,
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }

        }
    }
}