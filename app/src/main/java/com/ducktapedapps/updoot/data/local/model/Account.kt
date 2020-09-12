package com.ducktapedapps.updoot.data.local.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Account(val name: String, val icon_img: String)
