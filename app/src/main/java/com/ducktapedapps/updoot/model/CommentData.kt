package com.ducktapedapps.updoot.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CommentData(
        //common for normal comments and load more comments
        val id: String,
        val depth: Int,
        val parent_id: String,

        val author: String,
        var body: String,
        var ups: Int,
        var likes: Boolean?,
        val replies: List<CommentData>,
        val gildings: Gildings,
        val repliesExpanded: Boolean = false,

        //only for load more comments
        val count: Int?,
        @SerializedName("children")
        val loadMoreChildren: List<String>?
) : Data, Serializable