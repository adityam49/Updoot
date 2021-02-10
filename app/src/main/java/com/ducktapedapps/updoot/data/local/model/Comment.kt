package com.ducktapedapps.updoot.data.local.model

sealed class LocalComment(
        open val id: String,
        open val depth: Int,
        open val parentId: String,
)

data class FullComment(
        override val id: String,
        override val depth: Int = 0,
        override val parentId: String,
        val author: String,
        var body: String?,
        var upVotes: Int?,
        val userHasUpVoted: Boolean?,
        val replies: List<LocalComment>,
        val gildings: Gildings,
        val repliesExpanded: Boolean = false,
        val userIsOriginalPoster: Boolean,
        val userFlair: String? = null,
) : LocalComment(id, depth, parentId)

data class MoreComment(
        override val id: String,
        override val parentId: String,
        override val depth: Int = 0,
        val children: List<String>,
) : LocalComment(id, depth, parentId)