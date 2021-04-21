package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.data.local.model.FullComment
import com.ducktapedapps.updoot.data.local.model.Post

sealed class RedditItem {
    data class CommentData(val data: FullComment) : RedditItem()
    data class PostData(val data: Post) : RedditItem()
}