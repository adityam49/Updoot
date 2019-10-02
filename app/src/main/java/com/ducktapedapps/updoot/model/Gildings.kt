package com.ducktapedapps.updoot.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Gildings(
        @field:SerializedName("gid_1") val silver: Int,
        @field:SerializedName("gid_2") val gold: Int,
        @field:SerializedName("gid_3") val platinum: Int
) : Serializable