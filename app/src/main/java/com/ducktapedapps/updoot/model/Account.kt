package com.ducktapedapps.updoot.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Account(val name: String, val icon_img: String)
