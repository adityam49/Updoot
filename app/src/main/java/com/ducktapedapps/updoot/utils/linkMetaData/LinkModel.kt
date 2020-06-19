package com.ducktapedapps.updoot.utils.linkMetaData

import com.ducktapedapps.updoot.ui.comments.CommentScreenContent


data class LinkModel(
        val url: String,
        val siteName: String?,
        val title: String?,
        val description: String?,
        val image: String?
) : CommentScreenContent