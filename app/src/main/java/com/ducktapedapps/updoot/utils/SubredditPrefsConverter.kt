
package com.ducktapedapps.updoot.utils

import androidx.room.TypeConverter
import com.ducktapedapps.updoot.subreddit.SubredditSorting
import com.ducktapedapps.updoot.subreddit.SubredditSorting.*

class SubredditPrefsConverter {
    @TypeConverter
    fun sortingPrimitiveToNonPrimitive(sorting: Int): SubredditSorting = when (sorting) {
        0 -> Hot
        1 -> Rising
        2 -> Best
        3 -> New

        4 -> TopHour
        5 -> TopDay
        6 -> TopWeek
        7 -> TopMonth
        8 -> TopYear
        9 -> TopAll

        10 -> ControversialHour
        11 -> ControversialDay
        12 -> ControversialWeek
        13 -> ControversialMonth
        14 -> ControversialYear
        else -> ControversialAll
    }

    @TypeConverter
    fun sortingNonPrimitiveToPrimitive(subredditSorting: SubredditSorting): Int = when (subredditSorting) {
        is Hot -> 0
        is Rising -> 1
        is Best -> 2
        is New -> 3

        is TopHour -> 4
        is TopDay -> 5
        is TopWeek -> 6
        is TopMonth -> 7
        is TopYear -> 8
        is TopAll -> 9

        is ControversialHour -> 10
        is ControversialDay -> 11
        is ControversialWeek -> 12
        is ControversialMonth -> 13
        is ControversialYear -> 14
        else -> 15
    }

    @TypeConverter
    fun viewTypeIntToEnum(viewType: Int): PostViewType = when (viewType) {
        0 -> PostViewType.COMPACT
        else -> PostViewType.LARGE
    }

    @TypeConverter
    fun viewTypeEnumToInt(viewType: PostViewType): Int = viewType.ordinal
}