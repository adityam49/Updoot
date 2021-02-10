package com.ducktapedapps.updoot.data.remote.moshiAdapters

import com.squareup.moshi.JsonQualifier

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.FUNCTION)
@JsonQualifier
annotation class InconsistentApiResponse