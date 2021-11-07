package com.ducktapedapps.updoot.subreddit

import com.ducktapedapps.updoot.utils.Constants.ALL_TIME
import com.ducktapedapps.updoot.utils.Constants.BEST
import com.ducktapedapps.updoot.utils.Constants.CONTROVERSIAL
import com.ducktapedapps.updoot.utils.Constants.HOT
import com.ducktapedapps.updoot.utils.Constants.NEW
import com.ducktapedapps.updoot.utils.Constants.NOW
import com.ducktapedapps.updoot.utils.Constants.RISING
import com.ducktapedapps.updoot.utils.Constants.THIS_MONTH
import com.ducktapedapps.updoot.utils.Constants.THIS_WEEK
import com.ducktapedapps.updoot.utils.Constants.THIS_YEAR
import com.ducktapedapps.updoot.utils.Constants.TODAY
import com.ducktapedapps.updoot.utils.Constants.TOP

sealed class SubredditSorting {
    object Rising : SubredditSorting()
    object Best : SubredditSorting()
    object New : SubredditSorting()
    object Hot : SubredditSorting()
    object ControversialHour : SubredditSorting()
    object ControversialDay : SubredditSorting()
    object ControversialWeek : SubredditSorting()
    object ControversialMonth : SubredditSorting()
    object ControversialYear : SubredditSorting()
    object ControversialAll : SubredditSorting()
    object TopHour : SubredditSorting()
    object TopDay : SubredditSorting()
    object TopWeek : SubredditSorting()
    object TopMonth : SubredditSorting()
    object TopYear : SubredditSorting()
    object TopAll : SubredditSorting()

    companion object {
        fun SubredditSorting.mapSorting(): Pair<String, String?> = when (this) {
            Rising -> Pair(RISING, null)
            Best -> Pair(BEST, null)
            New -> Pair(NEW, null)
            Hot -> Pair(HOT, null)

            TopHour -> Pair(TOP, NOW)
            TopDay -> Pair(TOP, TODAY)
            TopWeek -> Pair(TOP, THIS_WEEK)
            TopMonth -> Pair(TOP, THIS_MONTH)
            TopYear -> Pair(TOP, THIS_YEAR)
            TopAll -> Pair(TOP, ALL_TIME)

            ControversialHour -> Pair(CONTROVERSIAL, NOW)
            ControversialDay -> Pair(CONTROVERSIAL, TODAY)
            ControversialWeek -> Pair(CONTROVERSIAL, THIS_WEEK)
            ControversialMonth -> Pair(
                CONTROVERSIAL,
                THIS_MONTH
            )
            ControversialYear -> Pair(CONTROVERSIAL, THIS_YEAR)
            ControversialAll -> Pair(CONTROVERSIAL, ALL_TIME)
        }
    }
}