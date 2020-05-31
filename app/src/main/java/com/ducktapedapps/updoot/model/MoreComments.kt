package com.ducktapedapps.updoot.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class MoreComments(val list: List<BaseComment>)