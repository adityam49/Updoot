package com.ducktapedapps.updoot.data.local.moshiAdapters

import com.ducktapedapps.updoot.data.local.model.*
import com.squareup.moshi.*
import java.lang.reflect.Type

enum class ContentType(val derivedClass: Class<out ThingData>) {
    Listing(ListingThing::class.java),
    t1(CommentData::class.java),
    t3(LinkData::class.java),
    t5(Subreddit::class.java),
    more(MoreCommentData::class.java)
}

class ThingFactory : JsonAdapter.Factory {
    override fun create(
            type: Type,
            annotations: MutableSet<out Annotation>,
            moshi: Moshi
    ): JsonAdapter<*>? {
        val contentTypeAdapter = moshi.adapter(ContentType::class.java)
        return if (!Types.getRawType(type).isAssignableFrom(Thing::class.java)) null
        else object : JsonAdapter<Thing>() {
            override fun fromJson(reader: JsonReader): Thing? {
                val jsonValue = reader.readJsonValue()

                @Suppress("Unchecked_cast")
                val value = jsonValue as? Map<String, *> ?: return null
                val contentTypeValue = value["kind"]
                val contentType = contentTypeAdapter.fromJsonValue(contentTypeValue)
                        ?: throw JsonDataException("Unsupported kind -> $contentTypeValue")
                val data = moshi.adapter(contentType.derivedClass).fromJsonValue(value["data"])
                        ?: throw JsonDataException(
                                "Unsupported data :$contentTypeValue"
                        )
                return Thing(data)
            }


            override fun toJson(writer: JsonWriter, value: Thing?) {
                if (value == null) return
                writer.beginObject()
                writer.name("data")
                when (value.data) {
                    is LinkData -> moshi.adapter(LinkData::class.java).toJson(writer, value.data)
                    is ListingThing -> moshi.adapter(ListingThing::class.java).toJson(writer, value.data)
                    is CommentData -> moshi.adapter(CommentData::class.java).toJson(writer, value.data)
                    is Subreddit -> moshi.adapter(Subreddit::class.java).toJson(writer, value.data)
                }
                writer.endObject()
            }
        }
    }
}

class Thing(val data: ThingData)


