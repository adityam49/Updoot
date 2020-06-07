package com.ducktapedapps.updoot.api.local.submissionsCache

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ducktapedapps.updoot.model.LinkData

@Database(entities = [LinkData::class], version = 1, exportSchema = false)
abstract class SubmissionsDB : RoomDatabase() {
    abstract fun submissionsCacheDAO(): SubmissionsCacheDAO
}

