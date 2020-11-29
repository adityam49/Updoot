package com.ducktapedapps.updoot.ui.user

import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.data.local.model.Comment
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.ui.comments.FullComment
import com.ducktapedapps.updoot.ui.subreddit.LargePost
import com.ducktapedapps.updoot.ui.theme.ColorOnScoreBackground
import com.ducktapedapps.updoot.ui.theme.ScoreBackground

@Composable
fun UserInfoScreen(viewModel: UserViewModel) {
    val content = viewModel.content.collectAsState()
    val loading = viewModel.loading.collectAsState()
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
        if (loading.value)
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

        items(content.value.children) { item ->
            when (item) {
                is Comment.CommentData -> FullComment(
                        threadWidth = 2.dp,
                        threadSpacingWidth = 6.dp,
                        singleThreadMode = false,
                        comment = item,
                        onClickComment = {}
                )
                is LinkData -> LargePost(
                        linkData = item,
                        onClickMedia = {},
                        openPost = {},
                        openOptions = {}
                )
            }
            Divider()
        }
        item { Spacer(Modifier.height(200.dp)) }
    }
}

@Composable
fun SectionChip(section: UserSection, isSelected: Boolean, onClick: () -> Unit) {
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
        onClick: (UserSection) -> Unit
) {
    ScrollableRow {
        sections.forEach { userSection ->
            SectionChip(
                    section = userSection,
                    isSelected = userSection == currentSection,
                    onClick = { onClick(userSection) }
            )
        }
    }
}