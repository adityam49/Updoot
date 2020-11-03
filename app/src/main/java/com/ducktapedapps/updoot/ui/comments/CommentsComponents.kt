package com.ducktapedapps.updoot.ui.comments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import com.ducktapedapps.updoot.data.local.model.Comment.CommentData
import com.ducktapedapps.updoot.data.local.model.Gildings
import com.ducktapedapps.updoot.data.local.model.Listing
import com.ducktapedapps.updoot.ui.theme.*


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
            style = MaterialTheme.typography.body1,
            modifier = modifier.fillMaxWidth().wrapContentHeight().padding(4.dp)
    )
}

@Composable
private fun CommentHeader(
        authorName: String,
        replies: Int,
        ups: Int?,
        likes: Boolean?,
        isOp: Boolean,
        isExpanded: Boolean,
        modifier: Modifier
) {
    Row(modifier = modifier.fillMaxWidth().wrapContentHeight().padding(4.dp)) {
        UserName(username = authorName, isOp = isOp)
        Spacer(modifier = Modifier.weight(1f))
        if (replies != 0 && !isExpanded) ReplyCounter(replyCount = replies)
        VoteCounter(ups = ups, likes = likes)
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
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        )
    }
}

@Composable
private fun ReplyCounter(replyCount: Int) {
    val background = if (isSystemInDarkTheme()) lightYellow else darkYellow
    ColoredTag(color = background) {
        Text(
                color = contentColorFor(color = background),
                text = "+ $replyCount",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
        )
    }
}

@Composable
private fun VoteCounter(ups: Int?, likes: Boolean?) {
    Text(
            text = ups?.toString() ?: "?",
            style = MaterialTheme.typography.caption,
            color = when (likes) {
                true -> upVoteColor
                false -> downVoteColor
                null -> MaterialTheme.colors.onBackground
            },
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
    )
}

@Composable
private fun ColoredTag(
        color: Color,
        body: @Composable () -> Unit
) {
    Surface(
            color = color,
            shape = RoundedCornerShape(20),
            modifier = Modifier.wrapContentSize()
    ) { body() }
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
                authorName = comment.author,
                replies = comment.replies.children.size,
                ups = comment.ups,
                likes = comment.likes,
                isOp = comment.is_submitter,
                isExpanded = comment.repliesExpanded,
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
private fun IndentationThread(
        modifier: Modifier,
        singleThreadMode: Boolean,
        indentLevel: Int,
        threadWidth: Dp,
        spacingWidth: Dp,
) {
    val isLight = MaterialTheme.colors.isLight
    Canvas(modifier = modifier.width(((spacingWidth.value + threadWidth.value) * indentLevel).dp), onDraw = {
        if (singleThreadMode) drawLine(
                color = if (isLight) LightThreadColors[indentLevel] else DarkThreadColors[indentLevel],
                start = Offset((spacingWidth.toPx() + threadWidth.toPx()) * indentLevel, 0f),
                end = Offset((spacingWidth.toPx() + threadWidth.toPx()) * indentLevel, drawContext.size.height),
                strokeWidth = threadWidth.toPx(),
        )
        else for (thread in 1..indentLevel) drawLine(
                color = if (isLight) LightThreadColors[thread] else DarkThreadColors[thread],
                start = Offset(x = thread * (spacingWidth.toPx() + threadWidth.toPx()), 0f),
                end = Offset(x = thread * (spacingWidth.toPx() + threadWidth.toPx()), drawContext.size.height),
                strokeWidth = threadWidth.toPx(),
        )
    })
}