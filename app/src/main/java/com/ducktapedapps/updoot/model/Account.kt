package com.ducktapedapps.updoot.model

import com.google.gson.annotations.SerializedName

import java.io.Serializable

data class Account(@field:SerializedName("name") val name: String) : Serializable
