package com.ducktapedapps.updoot.video

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.ducktapedapps.navigation.Event
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.ui.StyledPlayerView
import kotlinx.coroutines.delay
import timber.log.Timber

private const val TAG = "VideoScreen"

@Composable
fun VideoScreen(publishEvent: (Event) -> Unit, videoUrl: String) {
    val videoState = remember {
        mutableStateOf(
            VideoState(
                isPaused = true,
                url = videoUrl,
                isMuted = false,
                currentPosition = 0,
                finalPosition = 0,
            )
        )
    }
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                videoState.value = videoState.value.copy(isPaused = true)
            }
            else -> Unit
        }
    }
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            addMediaItem(videoState.value.url.toMediaItem())
            prepare()
            playWhenReady = true
        }
    }
    val eventListener = object : Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                STATE_IDLE -> Timber.d("onPlaybackStateChanged: state idel")
                STATE_BUFFERING -> Timber.d("onPlaybackStateChanged: state buffer")
                STATE_READY -> {
                    videoState.value = videoState.value.copy(
                        finalPosition = exoPlayer.duration,
                        currentPosition = exoPlayer.currentPosition,
                        isPaused = !exoPlayer.isPlaying
                    )
                    Timber.d("onPlaybackStateChanged: state ready")
                }
                STATE_ENDED -> {
                    exoPlayer.seekToDefaultPosition()
                    videoState.value = videoState.value.copy(isPaused = true, currentPosition = 0)
                }
                else -> Log.d(TAG, "onPlaybackStateChanged: else")
            }
        }
    }

    LaunchedEffect(Unit) {
        exoPlayer.addListener(eventListener)
        while (true) {
            delay(30)
            if (exoPlayer.isPlaying) videoState.value =
                videoState.value.copy(currentPosition = exoPlayer.currentPosition)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.removeListener(eventListener)
            exoPlayer.release()
        }
    }

    val togglePlaying = { videoState.value = with(videoState.value) { copy(isPaused = !isPaused) } }

    Box {
        VideoPlayer(
            modifier = Modifier
                .fillMaxSize()
                .clickable { togglePlaying() },
            exoPlayer = exoPlayer,
            videoState = videoState.value
        )
        VideoPlayerControllerOverlay(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.BottomCenter),
            videoState = videoState.value,
            togglePlaying = togglePlaying,
        )
    }
}

@Composable
private fun VideoPlayer(
    modifier: Modifier,
    exoPlayer: ExoPlayer,
    videoState: VideoState
) {
    if (videoState.isPaused) {
        exoPlayer.pause()
    } else {
        exoPlayer.play()
    }
    DisposableEffect(
        AndroidView(
            modifier = modifier,
            factory = {
                StyledPlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                }
            })
    ) {
        onDispose {
            exoPlayer.release()
        }
    }
}

private data class VideoState(
    val url: String,
    val isPaused: Boolean,
    val isMuted: Boolean,
    val currentPosition: Long,
    val finalPosition: Long,
) {
    val progress
        get() = (currentPosition.toFloat() / if (finalPosition > 0L) finalPosition else 1L).times(
            100f
        )
}

@Composable
private fun VideoPlayerControllerOverlay(
    modifier: Modifier,
    videoState: VideoState,
    togglePlaying: () -> Unit
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        SeekBar(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            videoState = videoState,
            setProgress = {}
        )
        PlayPauseButton(
            modifier = Modifier.wrapContentSize(),
            isPaused = videoState.isPaused,
            togglePlaying = togglePlaying
        )
    }
}


@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewSeekBar() {
    VideoPlayerControllerOverlay(
        modifier = Modifier.fillMaxSize(),
        videoState = VideoState(
            url = "",
            isPaused = true,
            isMuted = true,
            currentPosition = 1000L,
            finalPosition = 20000L
        ),
        togglePlaying = {},
    )
}

@Composable
private fun PlayPauseButton(
    modifier: Modifier,
    isPaused: Boolean,
    togglePlaying: () -> Unit,
) {
    IconToggleButton(
        modifier = modifier,
        checked = isPaused,
        onCheckedChange = { togglePlaying() }
    ) {
        Icon(
            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Close,
            contentDescription = if (isPaused) Icons.Default.PlayArrow.name else Icons.Default.Close.name,
        )
    }

}

private fun String.toMediaItem(): MediaItem {
    return MediaItem.fromUri(Uri.parse(this))
}

@Composable
private fun SeekBar(
    modifier: Modifier,
    videoState: VideoState,
    setProgress: (Float) -> Unit,
) {
    ConstraintLayout(
        constraintSet = seekbarConstraints(),
        modifier = modifier,
    ) {
        with(videoState) {
            TimeStamp(
                modifier = Modifier
                    .padding(4.dp)
                    .layoutId("timeElapsed"),
                text = millisToString(currentPosition)
            )
            SeekBar(
                modifier = Modifier
                    .padding(8.dp)
                    .layoutId("seekBar"),
                progress = progress,
            )
            TimeStamp(
                modifier = Modifier
                    .padding(4.dp)
                    .layoutId("timeRemaining"),
                text = millisToString(finalPosition)
            )
        }
    }

}

private fun seekbarConstraints(): ConstraintSet = ConstraintSet {
    val timeElapsed = createRefFor("timeElapsed")
    val timeRemaining = createRefFor("timeRemaining")
    val seekBar = createRefFor("seekBar")

    constrain(timeElapsed) {
        start.linkTo(parent.start)
        top.linkTo(seekBar.top)
        end.linkTo(seekBar.start)
        bottom.linkTo(seekBar.bottom)
        width = Dimension.wrapContent
    }
    constrain(seekBar) {
        width = Dimension.fillToConstraints
        start.linkTo(timeElapsed.end)
        top.linkTo(parent.top)
        bottom.linkTo(parent.bottom)
        end.linkTo(timeRemaining.start)
    }
    constrain(timeRemaining) {
        top.linkTo(seekBar.top)
        start.linkTo(seekBar.end)
        end.linkTo(parent.end)
        bottom.linkTo(seekBar.bottom)
        width = Dimension.wrapContent
    }
}


@Composable
private fun TimeStamp(
    modifier: Modifier = Modifier,
    text: String,
) {
    BasicText(
        modifier = modifier,
        text = text,
        style = TextStyle(color = MaterialTheme.colorScheme.onSurface)
    )
}

@Composable
private fun SeekBar(
    modifier: Modifier = Modifier,
    progress: Float,
    finishedStrokeWidth: Dp = 5.dp,
    unfinishedStrokeWidth: Dp = 3.dp,
    finishedColor: Color = MaterialTheme.colorScheme.onSurface,
    unfinishedColor: Color = MaterialTheme.colorScheme.primary,
) {
    val seekBarStrokeWidth: Float
    val thumbRadius: Float
    val unfinishedSeekBarStrokeWidth: Float
    with(LocalDensity.current) {
        seekBarStrokeWidth = finishedStrokeWidth.toPx()
        unfinishedSeekBarStrokeWidth = unfinishedStrokeWidth.toPx()
        thumbRadius = finishedStrokeWidth.times(2).toPx()
    }

    Canvas(modifier = modifier) {
        val start = Offset(0f, size.height.div(2))
        val end = Offset(size.width, size.height.div(2))
        val thumbPosition = lerp(start, end, progress / 100)
        drawLine(
            cap = StrokeCap.Round,
            color = finishedColor,
            start = start,
            end = thumbPosition,
            strokeWidth = seekBarStrokeWidth
        )
        drawLine(
            color = finishedColor.copy(alpha = 0.2f),
            start = thumbPosition,
            end = end,
            strokeWidth = unfinishedSeekBarStrokeWidth,
        )
        drawCircle(
            color = unfinishedColor,
            radius = thumbRadius,
            center = thumbPosition
        )
    }
}

@Composable
private fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}