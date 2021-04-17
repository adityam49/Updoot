package com.ducktapedapps.updoot.subreddit

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

//TODO
enum class Sorting {
    Rising, Best, New, Hot, Controversial, Top,
}

enum class Duration {
    Hour, Day, Week, Month, Year, All,
}