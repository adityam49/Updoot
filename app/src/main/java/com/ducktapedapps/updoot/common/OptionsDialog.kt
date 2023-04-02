package com.ducktapedapps.updoot.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun OptionsDialog(
    dismiss: () -> Unit,
    options: List<MenuItemModel>,
) {
    Dialog(
        onDismissRequest = dismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                options.forEach {
                    DropdownMenuItem(
                        onClick = it.onClick,
                        trailingIcon = {
                            Icon(painterResource(id = it.icon), it.title)
                        },
                        text = {
                            Text(text = it.title)
                        }
                    )
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
            painter = painterResource(id = menuItemModel.icon),
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

