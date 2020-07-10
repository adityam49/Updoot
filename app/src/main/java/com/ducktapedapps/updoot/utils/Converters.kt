package com.ducktapedapps.updoot.utils

fun getCompactCountAsString(count: Long): String =
        when (count) {
            in -Long.MAX_VALUE..-1001 -> {
                val temp = count / 1000f
                val temp2 = (temp * 10).toInt()
                String.format("%.1fK <", temp2 / 10f)
            }
            in -1000..1000 -> count.toString()
            else -> {
                val temp = count / 1000f
                val temp2 = (temp * 10).toInt()
                String.format("%.1fK+", temp2 / 10f)
            }
        }

fun getCompactDateAsString(date: Long): String =
        when (val days = (System.currentTimeMillis() - date * 1000) / 86400000) {
            0L -> "Today"
            1L -> "Yesterday"
            in 2L..30L -> "${days}D ago"
            in 31L..364L -> "${(days / 30).toInt()}M ago"
            else -> "${(days / 365).toInt()}Y ago"
        }

fun getCompactAge(date: Long): String = when (val days = (System.currentTimeMillis() - date * 1000) / 86400000) {
    0L -> "Created Today"
    1L -> "1D Old"
    in 2L..30L -> "${days}D old"
    in 31L..364L -> "${(days / 30).toInt()}M old"
    else -> "${(days / 365).toInt()}Y old"
}