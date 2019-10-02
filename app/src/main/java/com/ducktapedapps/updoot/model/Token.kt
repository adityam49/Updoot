//package com.ducktapedapps.updoot.model
//
//import com.google.gson.annotations.SerializedName
//
//class Token(
//        val access_token :String?,
//        val refresh_token:String?,
//        @field:SerializedName("expires_in") var absolute_expiry:Long,
//        val token_type:String
//) {
//}

package com.ducktapedapps.updoot.model

import java.io.Serializable

class Token(val access_token: String,
            val refresh_token: String,
            val absolute_expiry: Long = 3600.times(1000).plus(System.currentTimeMillis()),
            val token_type: String) : Serializable
