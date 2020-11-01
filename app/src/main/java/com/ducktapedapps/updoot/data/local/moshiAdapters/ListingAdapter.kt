package com.ducktapedapps.updoot.data.local.moshiAdapters

import com.ducktapedapps.updoot.data.local.model.Listing
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

class ListingAdapter(private val childrenAdapter: JsonAdapter<Any>) : JsonAdapter<Listing<Any>>() {
    private val wrapperOptions = JsonReader.Options.of("data")
    private val options = JsonReader.Options.of("after", "children")

    override fun fromJson(reader: JsonReader): Listing<Any>? {
        val children: MutableList<Any> = mutableListOf()
        var after: String? = null

        if (reader.peek() == JsonReader.Token.STRING) {
            reader.nextString()
            return Listing(null, emptyList())
        }
        reader.apply {
            readObject {
                when (selectName(wrapperOptions)) {
                    0 -> readObject {
                        when (selectName(options)) {
                            0 -> after = nextStringOrNull()
                            1 -> {
                                beginArray()
                                while (hasNext()) {
                                    childrenAdapter.fromJson(reader)?.let { children.add(it) }
                                }
                                endArray()
                            }
                            -1 -> skipNameAndValue()
                        }
                    }
                    -1 -> skipNameAndValue()
                }
            }
        }
        return Listing(
                children = children,
                after = after
        )
    }

    private fun JsonReader.skipNameAndValue() {
        skipName()
        skipValue()
    }

    private inline fun JsonReader.readObject(body: () -> Unit) {
        beginObject()
        while (hasNext()) {
            body()
        }
        endObject()
    }

    private fun JsonReader.nextStringOrNull(): String? {
        return if (peek() == JsonReader.Token.NULL) {
            nextNull<String?>()
            null
        } else {
            nextString()
        }
    }

    private companion object {
        const val TAG = "ListingAdapter"
    }

    override fun toJson(writer: JsonWriter, value: Listing<Any>?) {}
}

