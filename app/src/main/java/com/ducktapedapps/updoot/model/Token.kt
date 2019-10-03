package com.ducktapedapps.updoot.model

import java.io.Serializable

class Token(val access_token: String,
            val refresh_token: String?,
            var absoluteExpiry: Long,
            val token_type: String) : Serializable {


    fun setAbsoluteExpiry() {
        absoluteExpiry = System.currentTimeMillis() + (3600 * 1000)
    }
}
