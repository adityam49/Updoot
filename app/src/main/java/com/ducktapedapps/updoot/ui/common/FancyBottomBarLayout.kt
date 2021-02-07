package com.ducktapedapps.updoot.ui.common

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import com.ducktapedapps.updoot.ui.theme.UpdootDarkColors

/**
 *  Fancy bottom bar to be used with BottomSheetScaffold
 */

@ExperimentalMaterialApi
@Composable
fun FancyBottomBar(
        navigateUp: () -> Unit,
        modifier: Modifier = Modifier,
        title: String,
        options: List<BottomBarActions> = emptyList(),
        sheetProgress: SwipeProgress<BottomSheetValue>,
) {
    AnimatedBottomBarLayout(
            modifier = modifier
                    .wrapContentHeight(),
            sheetProgress = sheetProgress
    ) {
        //TODO : make back button optional
        IconButton(onClick = navigateUp) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back Arrow", tint = UpdootDarkColors.onSurface)
        }
        Text(
                modifier = Modifier.wrapContentWidth(),
                text = title,
                color = UpdootDarkColors.onSurface
        )
        options.forEach {
            IconButton(onClick = it.open) {
                Icon(it.icon, it.title, tint = UpdootDarkColors.onSurface)
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun AnimatedBottomBarLayout(
        modifier: Modifier = Modifier,
        sheetProgress: SwipeProgress<BottomSheetValue>,
        content: @Composable () -> Unit,
) {
    val progress = with(sheetProgress) {
        when {
            from == BottomSheetValue.Collapsed && to == BottomSheetValue.Collapsed -> 0f
            from == BottomSheetValue.Expanded && to == BottomSheetValue.Expanded -> 1f
            from == BottomSheetValue.Expanded && to == BottomSheetValue.Collapsed -> 1f - fraction
            else -> fraction
        }
    }
    Layout(
            modifier = modifier,
            content = content
    ) { measurables, constraints ->
        val placeables = measurables.map {
            it.measure(constraints)
        }

        check(placeables.size >= 2)

        val width = constraints.maxWidth
        val height =
                placeables.maxOf { it.height }
                        .coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(
                width = width,
                height = height,
        ) {
            placeables.forEachIndexed { index, placeable ->
                when (index) {
                    //back button
                    0 -> placeable.place(x = -(placeable.width * progress).toInt(), y = 0)

                    //title
                    1 -> placeable.place(
                            y = (height - placeable.height) / 2,
                            x = (placeables[0].width * (1 - progress) + (constraints.maxWidth - placeable.width) * progress / 2).toInt()
                    )

                    //actions
                    else -> placeable.apply {
                        place(
                                x = (width - ((placeables.size - index) * placeable.width * (1 - progress))).toInt(),
                                y = 0
                        )
                    }
                }
            }
        }
    }
}

data class BottomBarActions(
        val icon: ImageVector,
        val title: String,
        val open: () -> Unit,
)