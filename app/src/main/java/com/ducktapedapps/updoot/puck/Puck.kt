package com.ducktapedapps.updoot.puck

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toSize
import com.ducktapedapps.updoot.puck.Utils.Behaviour
import com.ducktapedapps.updoot.puck.Utils.Behaviour.Sticky
import com.ducktapedapps.updoot.puck.Utils.Configuration.*
import com.ducktapedapps.updoot.puck.Utils.Edge
import com.ducktapedapps.updoot.puck.Utils.Edge.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Modifier.puck(
        parentSize: MutableState<Size>,
        behaviour: Behaviour = Sticky(config = Corners),
        isPointsTowardsCenter: Boolean = false,
        focusedSizeMultiplier: Float = 1.05f,
        animationDuration: Int = 500,
        offset: Offset = Offset(0f, 0f),
        previousPointState: MutableState<Offset>,
        latestPointState: MutableState<Offset>,
) = composed {

    val focused = remember { mutableStateOf(false) }
    val isGravityActive = remember { mutableStateOf(false) }
    val composableSize = remember { mutableStateOf(Size.Zero) }
    val dragAngle = remember { mutableStateOf(0.0) }
    val dragSlope = remember { mutableStateOf(0f) }
    val dragOffset = remember { mutableStateOf(Offset(0f, 0f)) }

    val offsetYAnimatable = remember { Animatable(offset.x) }
    val offsetXAnimatable = remember { Animatable(offset.y) }
    val rotationAnimatable = remember { Animatable(0f) }

    val scale = animateFloatAsState(if (focused.value) focusedSizeMultiplier else 1f)
    val coroutineScope = rememberCoroutineScope()

    this
            //Increase scale when dragging
            .scale(scale.value)
            .offset { IntOffset(offsetXAnimatable.value.roundToInt(), offsetYAnimatable.value.roundToInt()) }
            .onGloballyPositioned { coordinates ->
                composableSize.value = coordinates.size.toSize()
            }
            .rotate(rotationAnimatable.value)
            .pointerInput(Unit) {
                detectDragGestures(
                        onDragStart = { offset ->
                            previousPointState.value = Offset(offsetXAnimatable.value, offsetYAnimatable.value)
                            focused.value = true
                            isGravityActive.value = false
                            //Animate to 0 rotation
                            if (isPointsTowardsCenter) rotateComposable(coroutineScope, rotationAnimatable, 0f)
                        },
                        onDragEnd = {
                            focused.value = false
                            val centerInclination = atan2(parentSize.value.height / 2, parentSize.value.width / 2) * (180 / Math.PI)
                            val slope = (latestPointState.value.y - previousPointState.value.y) / (latestPointState.value.x - previousPointState.value.x)
                            val width = parentSize.value.width / 2
                            val height = parentSize.value.height / 2

                            if (behaviour is Sticky) {

                                when (behaviour.config) {
                                    is Edges -> {

                                        val exp = slope * width
                                        val topExp = height / slope
                                        if (exp >= -height && exp <= height) {
                                            //Right or Left Edge
                                            stickToVerticalEdges(
                                                    latestPointState.value,
                                                    previousPointState.value,
                                                    isPointsTowardsCenter,
                                                    coroutineScope,
                                                    rotationAnimatable,
                                                    parentSize,
                                                    composableSize,
                                                    offsetXAnimatable,
                                                    animationDuration,
                                                    slope,
                                                    offsetYAnimatable,
                                            )
                                        } else if (topExp >= -width && topExp <= width) {
                                            //top
                                            if (isPointsTowardsCenter) rotateComposable(Top, coroutineScope, rotationAnimatable)


                                            if (latestPointState.value.y < previousPointState.value.y) {
                                                val x = getXIntercept(0f, previousPointState.value, slope, parentSize.value, composableSize.value)

                                                animateTranslation(coroutineScope, offsetXAnimatable, x, animationDuration)
                                                animateTranslation(coroutineScope, offsetYAnimatable, 0f, animationDuration)

                                            } else {
                                                val x = getXIntercept(parentSize.value.height - composableSize.value.height, previousPointState.value, slope, parentSize.value, composableSize.value)
                                                if (isPointsTowardsCenter) rotateComposable(Bottom, coroutineScope, rotationAnimatable)
                                                animateTranslation(coroutineScope, offsetXAnimatable, x, animationDuration)
                                                animateTranslation(coroutineScope, offsetYAnimatable, parentSize.value.height - composableSize.value.height, animationDuration)
                                            }
                                        }

                                    }

                                    is Corners -> {
                                        val TOP_RIGHT_CORNER = -centerInclination
                                        val BOTTOM_RIGHT_CORNER = centerInclination
                                        val BOTTOM_LEFT_CORNER = 180 - centerInclination
                                        val TOP_LEFT_CORNER = -180 + centerInclination
                                        if (dragAngle.value > 0) {
                                            val bottomLeftDifference = Math.abs(BOTTOM_LEFT_CORNER - dragAngle.value)
                                            val bottomRightDifference = Math.abs(BOTTOM_RIGHT_CORNER - dragAngle.value)
                                            if (bottomLeftDifference < bottomRightDifference) {
                                                //bottum left
                                                animateTranslation(coroutineScope, offsetXAnimatable, 0f, animationDuration)
                                                animateTranslation(coroutineScope, offsetYAnimatable, parentSize.value.height - composableSize.value.height, animationDuration)
                                            } else {
                                                //bottum right
                                                animateTranslation(coroutineScope, offsetXAnimatable, parentSize.value.width - composableSize.value.width, animationDuration)
                                                animateTranslation(coroutineScope, offsetYAnimatable, parentSize.value.height - composableSize.value.height, animationDuration)
                                            }

                                        } else {
                                            val topLeftDifference = abs(abs(TOP_LEFT_CORNER) - abs(dragAngle.value))
                                            val topRightDifference = abs(abs(TOP_RIGHT_CORNER) - abs(dragAngle.value))
                                            if (topLeftDifference < topRightDifference) {
                                                //Top left
                                                animateTranslation(coroutineScope, offsetXAnimatable, 0f, animationDuration)
                                                animateTranslation(coroutineScope, offsetYAnimatable, 0f, animationDuration)

                                            } else {
                                                //top right
                                                animateTranslation(coroutineScope, offsetXAnimatable, parentSize.value.width - composableSize.value.width, animationDuration)
                                                animateTranslation(coroutineScope, offsetYAnimatable, 0f, animationDuration)
                                            }
                                        }
                                    }

                                    is HorizontalEdges -> {
                                        stickToHorizontalEdges(latestPointState.value, previousPointState.value, slope, parentSize, composableSize, coroutineScope, offsetXAnimatable, animationDuration, offsetYAnimatable)
                                    }
                                    is VerticalEdges -> {
                                        stickToVerticalEdges(
                                                latestPointState.value,
                                                previousPointState.value,
                                                isPointsTowardsCenter,
                                                coroutineScope,
                                                rotationAnimatable,
                                                parentSize,
                                                composableSize,
                                                offsetXAnimatable,
                                                animationDuration,
                                                slope,
                                                offsetYAnimatable,
                                        )
                                    }
                                }
                            }
                            if (behaviour is Behaviour.Gravity) {
                                //TODO: add support for more positions
                                val position = behaviour.circle
                                //move offset to center of composable
                                val xCenter = offsetXAnimatable.value + composableSize.value.width / 2
                                val yCenter = offsetYAnimatable.value + composableSize.value.height / 2

                                val isInside = isInside(position.xCenter, position.yCenter, position.radius, xCenter, yCenter)
                                if (isInside) {
                                    //make center of composable align with center of circle
                                    animateTranslation(coroutineScope, offsetXAnimatable, position.xCenter - composableSize.value.width / 2, animationDuration)
                                    animateTranslation(coroutineScope, offsetYAnimatable, position.yCenter - composableSize.value.height / 2, animationDuration)
                                }
                            }
                        },
                        onDragCancel = {
                            focused.value = false
                        },
                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                            change.consumeAllChanges()
                            dragAngle.value = ((atan2(dragAmount.y, dragAmount.x)) * (180 / Math.PI))
                            dragSlope.value = (dragAmount.y / dragAmount.x)
                            dragOffset.value = dragAmount
                            //Using snapTo for showing no animation for real time draging
                            snapToNewPosition(coroutineScope, offsetXAnimatable, dragAmount, parentSize, composableSize, offsetYAnimatable)
                            latestPointState.value = Offset(x = offsetXAnimatable.value, y = offsetYAnimatable.value)
                        }
                )
            }
}

private fun snapToNewPosition(
        coroutineScope: CoroutineScope,
        offsetXAnimatable: Animatable<Float, AnimationVector1D>,
        dragAmount: Offset,
        parentSize: MutableState<Size>,
        composableSize: MutableState<Size>,
        offsetYAnimatable: Animatable<Float, AnimationVector1D>,
) {
    coroutineScope.launch {
        offsetXAnimatable.snapTo(
                (offsetXAnimatable.value + dragAmount.x)
                        .coerceIn(0f, parentSize.value.width - composableSize.value.width)
        )
        offsetYAnimatable.snapTo(
                (offsetYAnimatable.value + dragAmount.y)
                        .coerceIn(0f, parentSize.value.height - composableSize.value.height)
        )
    }
}

private fun stickToHorizontalEdges(
        latestPoint: Offset,
        previousPoint: Offset,
        slope: Float,
        parentSize: MutableState<Size>,
        composableSize: MutableState<Size>,
        coroutineScope: CoroutineScope,
        offsetXAnimatable: Animatable<Float, AnimationVector1D>,
        animationDuration: Int,
        offsetYAnimatable: Animatable<Float, AnimationVector1D>
) {
    if (latestPoint.y < previousPoint.y) {
        val x = getXIntercept(0f, previousPoint, slope, parentSize.value, composableSize.value)
        animateTranslation(coroutineScope, offsetXAnimatable, x, animationDuration)
        animateTranslation(coroutineScope, offsetYAnimatable, 0f, animationDuration)

    } else {
        val x = getXIntercept(parentSize.value.height - composableSize.value.height, previousPoint, slope, parentSize.value, composableSize.value)
        animateTranslation(coroutineScope, offsetXAnimatable, x, animationDuration)
        animateTranslation(coroutineScope, offsetYAnimatable, parentSize.value.height - composableSize.value.height, animationDuration)
    }
}

fun isInside(circle_x: Float, circle_y: Float, rad: Float, x: Float, y: Float): Boolean {
    val distance = sqrt((x - circle_x) * (x - circle_x) + (y - circle_y) * (y - circle_y).toDouble())
    return (distance - rad) < 0
}

private fun stickToVerticalEdges(
        latestPoint: Offset,
        previousPoint: Offset,
        isPointsTowardsCenter: Boolean,
        coroutineScope: CoroutineScope,
        rotationAnimatable: Animatable<Float, AnimationVector1D>,
        parentSize: MutableState<Size>,
        composableSize: MutableState<Size>,
        offsetXAnimatable: Animatable<Float, AnimationVector1D>,
        animationDuration: Int,
        slope: Float,
        offsetYAnimatable: Animatable<Float, AnimationVector1D>,
) {
    if (latestPoint.x > previousPoint.x) {
        //right edge
        var xCord = parentSize.value.width - composableSize.value.width
        var yCord = getYIntercept(parentSize.value.width - composableSize.value.width, previousPoint, slope, 0f, parentSize.value.height - composableSize.value.height)

        if (isPointsTowardsCenter) {
            rotateComposable(Right, coroutineScope, rotationAnimatable)
            xCord = parentSize.value.width - composableSize.value.width / 2 - composableSize.value.height / 2
            //don't know how but this works ?
            val minimumY = composableSize.value.width / 2 - composableSize.value.height / 2
            val maximumY = parentSize.value.height - composableSize.value.width / 2f - composableSize.value.height / 2f
            yCord = getYIntercept(parentSize.value.width - composableSize.value.width, previousPoint, slope, minimumY, maximumY)
        }
        animateTranslation(coroutineScope, offsetXAnimatable, xCord, animationDuration)
        animateTranslation(coroutineScope, offsetYAnimatable, yCord, animationDuration)

    } else {
        //leftEdge
        var xCord = 0f
        var yCord = getYIntercept(0f, previousPoint, slope, 0f, parentSize.value.height - composableSize.value.height)
        if (isPointsTowardsCenter) {
            rotateComposable(Left, coroutineScope, rotationAnimatable)
            xCord = -composableSize.value.width / 2 + composableSize.value.height / 2
            val minimumY = +composableSize.value.width / 2f - composableSize.value.height / 2f
            val maximumY = parentSize.value.height - composableSize.value.width / 2f - composableSize.value.height / 2f
            yCord = getYIntercept(0f, previousPoint, slope, minimumY, maximumY)
        }
        animateTranslation(coroutineScope, offsetXAnimatable, xCord, animationDuration)
        animateTranslation(coroutineScope, offsetYAnimatable, yCord, animationDuration)
    }
}

private fun animateTranslation(
        coroutineScope: CoroutineScope,
        offsetAnimatable: Animatable<Float, AnimationVector1D>,
        targetValue: Float,
        animationDuration: Int
) {
    coroutineScope.launch {
        offsetAnimatable.animateTo(
                targetValue = targetValue,
                animationSpec = tween(
                        durationMillis = animationDuration,
                        delayMillis = 0
                )
        )
    }
}

private fun rotateComposable(
        coroutineScope: CoroutineScope,
        rotationAnimatable: Animatable<Float, AnimationVector1D>,
        angle: Float
) {
    coroutineScope.launch {
        rotationAnimatable.animateTo(
                targetValue = angle,
                animationSpec = tween(
                        durationMillis = 50,
                        delayMillis = 0
                )
        )

    }
}

fun rotateComposable(edge: Edge, coroutineScope: CoroutineScope, rotationAnimatable: Animatable<Float, AnimationVector1D>) {

    val rotationAngle = when (edge) {
        Top -> {
            180f
        }
        Bottom -> {
            0f
        }
        Left -> {
            90f
        }
        Right -> {
            -90f
        }
    }
    coroutineScope.launch {
        rotationAnimatable.animateTo(
                targetValue = rotationAngle,
                animationSpec = tween(
                        durationMillis = 200,
                        delayMillis = 0
                )
        )
    }

}

fun getYIntercept(xCordinate: Float, previousPosition: Offset, slope: Float, minVal: Float = 0f, maxValue: Float): Float {
    return (slope * (xCordinate - previousPosition.x) + previousPosition.y).coerceIn(minVal, maxValue)
}

fun getXIntercept(yCordinate: Float, previousPosition: Offset, slope: Float, parentSize: Size, composableSize: Size): Float {
    return ((1 / slope) * (yCordinate - previousPosition.y) + previousPosition.x).coerceIn(0f, parentSize.width - composableSize.width)
}