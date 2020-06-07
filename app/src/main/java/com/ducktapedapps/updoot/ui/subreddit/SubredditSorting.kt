package com.ducktapedapps.updoot.ui.subreddit

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
}
