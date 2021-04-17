package com.ducktapedapps.updoot.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SubredditBottomBar(
    modifier: Modifier = Modifier,
    navigateUp: () -> Unit,
    openMenu: () -> Unit,
    subredditName: String,
    showActionIcons: Boolean
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = navigateUp, enabled = showActionIcons) {
            Icon(Icons.Default.ArrowBack, Icons.Default.ArrowBack.name)
        }

        Text(text = subredditName, modifier = Modifier.padding(8.dp))

        IconButton(onClick = openMenu, enabled = showActionIcons) {
            Icon(Icons.Default.Menu, Icons.Default.Menu.name)
        }
    }
}