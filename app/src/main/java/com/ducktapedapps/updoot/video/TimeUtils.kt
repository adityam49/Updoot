package com.ducktapedapps.updoot.video


fun millisToString(millis: Long): String {
    var m = millis
    val sb = StringBuilder()
    if (m < 0) {
        m = -m
        sb.append("-")
    }
    m /= 1000
    val sec = (m % 60).toInt()
    m /= 60
    val min = (m % 60).toInt()
    m /= 60
    val hours = m.toInt()
    if (hours > 0) sb.append(hours).append(':')

    when {
        min == 0 -> sb.append("00")
        min > 0 -> sb.append("0").append(min)
        min > 9 -> sb.append(min)
    }
    sb.append(":")
    when {
        sec == 0 -> sb.append("00")
        sec in 1..9 -> sb.append("0").append(sec)
        sec > 9 -> sb.append(sec)
    }

    return sb.toString()
}