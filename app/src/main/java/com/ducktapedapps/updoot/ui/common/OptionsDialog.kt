package com.ducktapedapps.updoot.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.AndroidDialogProperties
import androidx.compose.ui.window.Dialog

@Composable
fun OptionsDialog(
        dismiss: () -> Unit,
        options: List<MenuItemModel>,
) {
    Dialog(
            onDismissRequest = dismiss,
            properties = AndroidDialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
            )
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                options.forEach {
                    DropdownMenuItem(onClick = it.onClick) {
                        MenuItem(menuItemModel = it)
                    }
                }
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

