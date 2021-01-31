package com.ducktapedapps.updoot.ui.subreddit

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun PopUp(
        dismiss: () -> Unit,
        expanded: Boolean,
        options: List<MenuItemModel>,
        toggle: @Composable () -> Unit,
) {
    DropdownMenu(
            toggle = toggle,
            expanded = expanded,
            onDismissRequest = dismiss,
            dropdownOffset = DpOffset(32.dp, 0.dp)
    ) {
        options.forEach {
            DropdownMenuItem(onClick = it.onClick) {
                MenuItem(menuItemModel = it)
            }
        }
    }
}


@Composable
fun MenuItem(menuItemModel: MenuItemModel) {
    Row(
            modifier = Modifier
                    .wrapContentHeight()
                    .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
                modifier = Modifier.size(24.dp),
                imageVector = vectorResource(id = menuItemModel.icon),
                contentDescription = menuItemModel.title,
        )
        Text(modifier = Modifier.padding(8.dp), text = menuItemModel.title)
    }
}

data class MenuItemModel(
        val onClick: () -> Unit,
        val title: String,
        @DrawableRes val icon: Int,
)

