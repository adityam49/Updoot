package com.ducktapedapps.updoot.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.ui.comments.FullComment
import com.ducktapedapps.updoot.ui.subreddit.LargePost
import com.ducktapedapps.updoot.ui.theme.ColorOnScoreBackground
import com.ducktapedapps.updoot.ui.theme.ScoreBackground
import com.ducktapedapps.updoot.ui.user.UserContent.UserComment
import com.ducktapedapps.updoot.ui.user.UserContent.UserPost

@Composable
fun UserInfoScreen(viewModel: UserViewModel) {
    val content = viewModel.content.collectAsState()
    val loading = viewModel.isLoading.collectAsState()
    val currentSection = viewModel.currentSection.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        //TODO put username somewhere
        item {
            UserSections(
                    sections = UserSection.values().asList(),
                    currentSection = currentSection.value,
                    onClick = { viewModel.setSection(it) }
            )
        }

        itemsIndexed(content.value) { index, item ->
            if (content.value.size - 5 <= index) viewModel.loadPage()
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
            Divider()
        }

        if (loading.value) item {
            Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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