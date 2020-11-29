package com.ducktapedapps.updoot.ui.comments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.ducktapedapps.updoot.data.local.model.Comment.CommentData
import com.ducktapedapps.updoot.data.local.model.Comment.MoreCommentData
import com.ducktapedapps.updoot.data.local.model.Gildings
import com.ducktapedapps.updoot.data.local.model.Listing
import com.ducktapedapps.updoot.ui.common.AllGildings
import com.ducktapedapps.updoot.ui.common.VoteCounter
import com.ducktapedapps.updoot.ui.theme.*
import com.ducktapedapps.updoot.utils.getCompactCountAsString


private val commentData = CommentData(
        body = """
    Drift all you like
    From ocean to ocean
    Search the whole world
    But drunken confessions
    And hijacked affairs
    Will just make you more alone
    When you come home
    I'll bake you a cake
    Made of all their eyes
    I wish you could see me
    Dressed for the kill
    You're my man of war
    You're my man of war
    And the worms will come for you
    Big Boots
    Yeah, yeah, yeah
    So unplug the phones
    Stop all the taps
    It all comes flooding back… 
""".trimIndent(),
        depth = 10,
        parent_id = "id_kjflsdf",
        name = "id_jfdskj",
        author = "u/some_username_owo",
        ups = 32,
        likes = false,
        gildings = Gildings(1, 2, 3),
        is_submitter = false,
        replies = Listing(children = emptyList())
)

@Composable
private fun CommentBody(text: String, modifier: Modifier) {
    Text(
            text = text,
            style = MaterialTheme.typography.body2,
            modifier = modifier.fillMaxWidth().wrapContentHeight().padding(8.dp)
    )
}

@Composable
private fun CommentHeader(
        commentData: CommentData,
        modifier: Modifier
) {
    Row(modifier = modifier.fillMaxWidth().wrapContentHeight().padding(4.dp)) {
        with(commentData) {
            UserName(username = author, isOp = is_submitter)
            Spacer(modifier = Modifier.weight(1f))
            AllGildings(gildings = gildings)
            if (replies.children.isNotEmpty() && !repliesExpanded) ReplyCounter(replyCount = replies.children.size)
            Spacer(modifier = Modifier.width(2.dp))
            VoteCounter(ups = ups, likes = likes)
        }
    }
}

@Composable
private fun UserName(username: String, isOp: Boolean) {
    ColoredTag(
            color =
            if (isOp) MaterialTheme.colors.primary
            else MaterialTheme.colors.surface
    ) {
        Text(
                text = username,
                style = MaterialTheme.typography.overline,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        )
    }
}

@Composable
private fun ReplyCounter(replyCount: Int) {
    val background = MaterialTheme.colors.ScoreBackground
    ColoredTag(color = background) {
        Text(
                color = MaterialTheme.colors.ColorOnScoreBackground,
                text = "+ ${getCompactCountAsString(replyCount.toLong())}",
                style = MaterialTheme.typography.overline,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        )
    }
}


@Composable
private fun ColoredTag(
        color: Color,
        body: @Composable () -> Unit
) {
    Card(
            backgroundColor = color,
            shape = RoundedCornerShape(20),
            modifier = Modifier.wrapContentSize(),
            content = body
    )
}

@Composable
@Preview
private fun PreviewComment() {
    UpdootTheme {
        Surface(color = MaterialTheme.colors.background) {
            FullComment(comment = commentData, onClickComment = {}, threadSpacingWidth = 6.dp, threadWidth = 2.dp, singleThreadMode = false)
        }
    }
}

@Composable
@Preview
private fun PreviewDarkComment() {
    UpdootTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colors.background) {
            FullComment(comment = commentData, onClickComment = {}, threadSpacingWidth = 6.dp, threadWidth = 2.dp, singleThreadMode = false)
        }
    }
}

@Composable
fun FullComment(
        threadWidth: Dp,
        threadSpacingWidth: Dp,
        singleThreadMode: Boolean,
        comment: CommentData,
        onClickComment: () -> Unit,
) {
    ConstraintLayout(modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(onClick = onClickComment)
    ) {
        val (indentation, header, body) = createRefs()
        IndentationThread(
                indentLevel = comment.depth,
                singleThreadMode = singleThreadMode,
                threadWidth = threadWidth,
                spacingWidth = threadSpacingWidth,
                modifier = Modifier.constrainAs(indentation) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(header.start)
                    height = Dimension.fillToConstraints
                    width = Dimension.wrapContent
                })

        CommentHeader(
                commentData = comment,
                modifier = Modifier.constrainAs(header) {
                    start.linkTo(indentation.end)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    bottom.linkTo(body.top)
                    height = Dimension.wrapContent
                    width = Dimension.fillToConstraints
                })
        CommentBody(text = comment.body ?: "", modifier = Modifier.constrainAs(body) {
            start.linkTo(indentation.end)
            top.linkTo(header.bottom)
            end.linkTo(header.end)
            bottom.linkTo(parent.bottom)
            height = Dimension.wrapContent
            width = Dimension.fillToConstraints
        })

    }

}

@Composable
fun MoreComment(
        data: MoreCommentData,
        loadMoreComments: () -> Unit,
        singleThreadMode: Boolean,
        threadSpacingWidth: Dp,
        threadWidth: Dp,
) {
    ConstraintLayout {
        val (indentation, body) = createRefs()
        IndentationThread(
                modifier = Modifier.constrainAs(indentation) {
                    height = Dimension.fillToConstraints
                    width = Dimension.wrapContent
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(body.start)
                    bottom.linkTo(parent.bottom)
                },
                singleThreadMode = singleThreadMode,
                indentLevel = data.depth,
                threadWidth = threadWidth,
                spacingWidth = threadSpacingWidth,
        )
        OutlinedButton(
                onClick = loadMoreComments,
                modifier = Modifier.constrainAs(body) {
                    start.linkTo(indentation.end, margin = 8.dp)
                    top.linkTo(parent.top, margin = 4.dp)
                    bottom.linkTo(parent.bottom, margin = 4.dp)
                    end.linkTo(parent.end)
                    width = Dimension.wrapContent
                    height = Dimension.wrapContent
                }
        ) {
            Text(
                    text = "Load ${data.children.size} comments",
                    style = MaterialTheme.typography.body2
            )
        }

    }
}



@Composable
private fun IndentationThread(
        modifier: Modifier,
        singleThreadMode: Boolean,
        indentLevel: Int,
        threadWidth: Dp,
        spacingWidth: Dp,
) {
    if (indentLevel == 0) return
    val isLight = MaterialTheme.colors.isLight
    Canvas(modifier = modifier.width(((spacingWidth.value + threadWidth.value) * indentLevel).dp), onDraw = {
        if (singleThreadMode) drawLine(
                color = if (isLight) LightThreadColors[indentLevel - 1] else DarkThreadColors[indentLevel - 1],
                start = Offset((spacingWidth.toPx() + threadWidth.toPx()) * indentLevel, 0f),
                end = Offset((spacingWidth.toPx() + threadWidth.toPx()) * indentLevel, drawContext.size.height),
                strokeWidth = threadWidth.toPx(),
        )
        else for (thread in 1..indentLevel) drawLine(
                color = if (isLight) LightThreadColors[thread - 1] else DarkThreadColors[thread - 1],
                start = Offset(x = thread * (spacingWidth.toPx() + threadWidth.toPx()), 0f),
                end = Offset(x = thread * (spacingWidth.toPx() + threadWidth.toPx()), drawContext.size.height),
                strokeWidth = threadWidth.toPx(),
        )
    })
}