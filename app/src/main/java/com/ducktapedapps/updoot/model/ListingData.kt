package com.ducktapedapps.updoot.model

data class ListingData(val after: String,
                       val children: List<Thing>) : Data