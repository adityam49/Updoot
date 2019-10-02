package com.ducktapedapps.updoot.model

import java.io.Serializable

data class Preview(
        val images: List<Images>,
        val enabled: Boolean
) : Serializable