package com.ducktapedapps.updoot.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.updoot.subreddit.SubredditSorting
import kotlinx.coroutines.launch

@Composable
fun ModalBottomSheetForActions(
    options: List<MenuItemModel>,
    publishEvent: ((Event) -> Unit)? = null,
    bottomSheetVisible: Boolean,
    hideBottomSheet: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = bottomSheetVisible) {
        if (bottomSheetVisible) sheetState.show() else sheetState.hide()
    }
    if (bottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    hideBottomSheet()
                }
            },
            tonalElevation = 0.dp,
            sheetState = sheetState,
        ) {
            options.forEach {
                MenuItem(menuItemModel = it.copy(onClick = {
                    coroutineScope
                        .launch {
                            sheetState.hide()
                            hideBottomSheet()
                        }
                    val eventData = it.onClick()
                    when (eventData) {
                        is BottomSheetItemType.ScreenNavigation -> {
                            hideBottomSheet()
                            publishEvent?.invoke(eventData.event)
                        }

                        else -> Unit
                    }
                    eventData
                }))
            }
        }
    }

}

@Composable
fun MenuItem(menuItemModel: MenuItemModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { menuItemModel.onClick() }
            .padding(horizontal = 32.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(32.dp),
            painter = painterResource(id = menuItemModel.icon),
            contentDescription = menuItemModel.title,
        )
        Text(
            modifier = Modifier.padding(16.dp),
            text = menuItemModel.title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

data class MenuItemModel(
    val onClick: () -> BottomSheetItemType,
    val title: String,
    @DrawableRes val icon: Int,
)

sealed class BottomSheetItemType {
    object ViewTypeChange : BottomSheetItemType()
    data class PostSorting(val type: SubredditSorting) : BottomSheetItemType()

    data class Account(val userName: String) : BottomSheetItemType()

    object Action : BottomSheetItemType()
    data class ScreenNavigation(val event: Event.ScreenNavigationEvent) : BottomSheetItemType()
}