package com.ducktapedapps.updoot.utils

import androidx.room.TypeConverter
import com.ducktapedapps.updoot.utils.SortTimePeriod.*
import com.ducktapedapps.updoot.utils.Sorting.*

/**
 * Helper functions to aid in type conversion of enum to primitives and vice-versa
 * @see com.ducktapedapps.updoot.utils.Sorting
 * @see com.ducktapedapps.updoot.utils.SortTimePeriod
 * @see com.ducktapedapps.updoot.utils.SubmissionUiType
 */
class SubredditPrefsConverter {
    @TypeConverter
    fun sortingStringToEnum(sorting: String): Sorting = when (sorting) {
        "hot" -> hot
        "best" -> best
        "top" -> top
        "rising" -> rising
        "new" -> new
        else -> controversial
    }

    @TypeConverter
    fun sortingPeriodStringToEnum(period: String?): SortTimePeriod? = when (period) {
        "hour" -> hour
        "day" -> day
        "week" -> week
        "month" -> month
        "year" -> year
        "all" -> all
        else -> null
    }

    @TypeConverter
    fun sortingPeriodEnumToString(timePeriod: SortTimePeriod?): String? = timePeriod?.name

    @TypeConverter
    fun sortingEnumToString(sorting: Sorting): String = sorting.toString()

    @TypeConverter
    fun viewTypeIntToEnum(viewType: Int): SubmissionUiType = when (viewType) {
        0 -> SubmissionUiType.COMPACT
        else -> SubmissionUiType.LARGE
    }

    @TypeConverter
    fun viewTypeEnumToInt(viewType: SubmissionUiType): Int = viewType.ordinal
}