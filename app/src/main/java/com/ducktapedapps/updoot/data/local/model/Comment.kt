package com.ducktapedapps.updoot.data.local.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
sealed class Comment(
        open val depth: Int,
        open val name: String,
        open val parent_id: String
) : RedditThing {

    @JsonClass(generateAdapter = true)
    data class CommentData(
            override val depth: Int,
            override val parent_id: String,
            override val name: String,
            val author: String,
            var body: String?,
            var ups: Int?,
            val likes: Boolean?,
            val replies: Listing<Comment> = Listing(children = emptyList()),
            val gildings: Gildings,
            val repliesExpanded: Boolean = false,
            val is_submitter: Boolean,
            val author_flair_text: String? = ""
    ) : Comment(depth, name, parent_id)

    @JsonClass(generateAdapter = true)
    data class MoreCommentData(
            val count: Int,
            override val name: String,
            override val parent_id: String,
            override val depth: Int,
            val children: List<String>,
    ) : Comment(depth, name, parent_id)
}
