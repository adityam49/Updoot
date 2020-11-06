package com.ducktapedapps.updoot.ui.user

import androidx.compose.foundation.ScrollableRow
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.data.local.model.Comment
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.ui.theme.ColorOnScoreBackground
import com.ducktapedapps.updoot.ui.theme.ScoreBackground

@ExperimentalLazyDsl
@Composable
fun UserInfoScreen(viewModel: UserViewModel) {
    val content = viewModel.content.collectAsState()
    val loading = viewModel.loading.collectAsState()
    val currentSection = viewModel.currentSection.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
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

        item {
            Divider()
            content.value.children.forEach {
                when (it) {
                    is Comment.CommentData -> Text(text = it.body ?: "?")
                    is LinkData -> Text(text = it.title)
                }
            }
        }
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