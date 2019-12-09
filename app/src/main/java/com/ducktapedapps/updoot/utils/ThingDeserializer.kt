package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.ListingData
import com.ducktapedapps.updoot.model.Thing
import com.google.gson.*
import java.lang.reflect.Type

class ThingDeserializer : JsonDeserializer<Thing?> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Thing? {
        var jsonObject: JsonObject? = null
        if (json is JsonObject) {
            jsonObject = json.getAsJsonObject()
        } else if (json is JsonArray) {
            jsonObject = json.getAsJsonArray()[1].asJsonObject
        }
        if (jsonObject != null) {
            val kind = jsonObject["kind"].asString
            if (kind != null) {
                val element = jsonObject["data"]
                if (element != null) {
                    return when (kind) {
                        "Listing" -> Thing("Listing", context.deserialize(element, ListingData::class.java))
                        "t3" -> Thing("t3", context.deserialize(element, LinkData::class.java))
                        "more", "t1" -> Thing("t1", context.deserialize(element, CommentData::class.java))
                        else -> null
                    }
                }
            }
        }
        return null
    }


}