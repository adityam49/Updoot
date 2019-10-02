package com.ducktapedapps.updoot.model

import java.io.Serializable

data class CommentData(
        val author: String,
        val depth: Int,
        var body: String,
        var ups: Int,
        var likes: Boolean?,
        val id: String,
        val replies: Thing?,
        val gildings: Gildings
) : Data, Serializable