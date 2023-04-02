package com.ducktapedapps.updoot.data.local.model

data class Gildings(
    val silverAwardCount: Int = 0,
    val goldAwardCount: Int = 0,
    val platinumAwardCount: Int = 0,
) {
    fun hasGilding() :Boolean {
        return silverAwardCount != 0 || goldAwardCount != 0 || platinumAwardCount != 0
    }
}