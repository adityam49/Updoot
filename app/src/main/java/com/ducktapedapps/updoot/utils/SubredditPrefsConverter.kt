package com.ducktapedapps.updoot.utils

import androidx.room.TypeConverter

/**
 * Helper functions to aid in type conversion of enum to primitives and vice-versa
 * @see com.ducktapedapps.updoot.utils.Sorting
 * @see com.ducktapedapps.updoot.utils.SubmissionUiType
 */
class SubredditPrefsConverter {
    @TypeConverter
    fun sortingStringToEnum(sorting: String): Sorting = when (sorting) {
        "hot" -> Sorting.HOT
        "best" -> Sorting.BEST
        "top" -> Sorting.TOP
        "rising" -> Sorting.RISING
        "new" -> Sorting.NEW
        else -> Sorting.CONTROVERSIAL
    }

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