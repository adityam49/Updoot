package com.ducktapedapps.updoot.data.mappers

import com.ducktapedapps.updoot.data.local.model.*
import com.ducktapedapps.updoot.data.remote.model.Comment
import com.ducktapedapps.updoot.data.remote.model.Comment.CommentData
import com.ducktapedapps.updoot.data.remote.model.Comment.MoreCommentData
import com.ducktapedapps.updoot.data.remote.model.LinkData
import com.ducktapedapps.updoot.data.remote.model.RemoteSubreddit
import java.util.*

fun LinkData.toPost(): Post {
    return Post(
            id = name,
            title = title,
            author = author,
            upVotes = ups,
            userHasUpVoted = likes,
            saved = saved,
            archived = archived,
            locked = locked,
            stickied = stickied,
            isNsfw = over_18,
            subredditName = subreddit,
            created = Date(created_utc),
            commentsCount = num_comments,
            postContentUrl = url,
            permalink = permalink,
            gildings = Gildings(gildings.gid_1, gildings.gid_2, gildings.gid_3),
            postThumbnail = thumbnail,
            mediaImage = preview?.let { ImageVariants(it.lowResUrl, it.lowResHeight, it.lowResWidth, it.highResUrl, it.highResHeight, it.highResWidth) }
                    ?: ImageVariants(),
            mediaText = selftext,
            mediaVideo = media?.let { Video(dashUrl = it.dash_url, duration = it.duration, fallbackUrl = it.fallback_url) }
                    ?: Video(),
            lastUpdated = Date(System.currentTimeMillis())

    )
}


fun Comment.toLocalComment(): LocalComment = when (this) {
    is CommentData -> toLocalFullComment()
    is MoreCommentData -> toLocalMoreComment()
}

fun CommentData.toLocalFullComment(): LocalComment {
    return FullComment(
            id = name,
            depth = depth,
            parentId = parent_id,
            author = author,
            body = body,
            upVotes = ups,
            userHasUpVoted = likes,
            replies = replies.children.map { it.toLocalComment() },
            gildings = Gildings(gildings.gid_1, gildings.gid_2, gildings.gid_3),
            repliesExpanded = false,
            userIsOriginalPoster = is_submitter,
            userFlair = author_flair_text,
    )
}

fun MoreCommentData.toLocalMoreComment(): MoreComment {
    return MoreComment(
            id = name,
            depth = depth,
            parentId = parent_id,
            children = children,
    )
}


fun RemoteSubreddit.toLocalSubreddit(): LocalSubreddit {
    return LocalSubreddit(
            subredditName = display_name,
            icon = community_icon,
            subscribers = subscribers,
            accountsActive = accounts_active,
            shortDescription = public_description,
            longDescription = description,
            created = Date(created),
            lastUpdated = Date(System.currentTimeMillis())
    )
}