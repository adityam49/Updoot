package com.ducktapedapps.updoot.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class CommentListing(val comments: List<BaseComment>)