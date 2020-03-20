package com.ducktapedapps.updoot.utils

fun getCompactCountAsString(count: Long): String =
        when (count) {
            in 0..999 -> count.toString()
            in 1000..999999 -> String.format("%.1fK", count / 1000.0)
            else -> "1M+"
        }

fun getCompactDateAsString(date: Long): String =
        when (val days = (System.currentTimeMillis() - date * 1000) / 86400000) {
            0L -> "Today"
            1L -> "Yesterday"
            in 2L..30L -> "${days}D ago"
            in 31L..364L -> "${(days / 30).toInt()}M ago"
            else -> "${(days / 365).toInt()}Y ago"
        }
