package com.ducktapedapps.updoot.data.remote.moshiAdapters

import com.ducktapedapps.updoot.data.remote.model.Listing
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class MoreCommentAdapter(private val polymorphicAdapter: PolymorphicAdapter) : JsonAdapter<Listing<Any>>() {
    private val options = JsonReader.Options.of("json", "data", "things")
    override fun fromJson(reader: JsonReader): Listing<Any>? {
        val children: MutableList<Any> = mutableListOf()
        reader.apply {
            readObject {
                when (selectName(options)) {
                    0 -> readObject {
                        when (selectName(options)) {
                            1 -> readObject {
                                when (selectName(options)) {
                                    2 -> readArray {
                                        polymorphicAdapter.fromJson(this)?.let { children += it }
                                    }
                                    -1 -> skipNameAndValue()
                                }
                            }
                            -1 -> skipNameAndValue()
                        }
                    }
                    -1 -> skipNameAndValue()
                }
            }
        }

        return Listing(children = children)
    }

    override fun toJson(writer: JsonWriter, value: Listing<Any>?) {}
}

fun JsonReader.skipNameAndValue() {
    skipName()
    skipValue()
}

inline fun JsonReader.readObject(body: () -> Unit) {
    beginObject()
    while (hasNext()) body()
    endObject()
}

inline fun JsonReader.readArray(body: () -> Unit) {
    beginArray()
    while (hasNext()) body()
    endArray()
}