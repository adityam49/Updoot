package com.ducktapedapps.updoot.imagePreview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.ducktapedapps.navigation.Event

@Composable
fun ImagePreviewScreen(
    modifier: Modifier = Modifier.fillMaxSize(),
    publishEvent: (Event) -> Unit,
    imageUrl: String,
) {
    Scaffold(modifier = modifier) {
        AsyncImage(
            modifier = Modifier.fillMaxSize().padding(it),
            model = imageUrl,
            contentScale = ContentScale.Fit,
            contentDescription = "Image"
        )
    }
}
