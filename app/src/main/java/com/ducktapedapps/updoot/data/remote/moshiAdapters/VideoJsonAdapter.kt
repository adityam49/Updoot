package com.ducktapedapps.updoot.data.remote.moshiAdapters

import com.ducktapedapps.updoot.data.local.model.Video
import com.squareup.moshi.*
import java.lang.reflect.Type

class VideoJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? =
            if (!Types.getRawType(type).isAssignableFrom(Video::class.java)) null
            else VideoJsonAdapter()
}

class VideoJsonAdapter : JsonAdapter<Video>() {
    override fun toJson(writer: JsonWriter, value: Video?) {}

    @Suppress("UNCHECKED_CAST")
    override fun fromJson(reader: JsonReader): Video? {
        val jsonValue = reader.readJsonValue()
        val jsonMap = jsonValue as? Map<String, *> ?: return null
        val redditVideo = jsonMap["reddit_video"] as? Map<String, *> ?: return null
        redditVideo.run {
            val fallbackUrl = this["fallback_url"] as? String ?: return null
            val dashUrl = this["dash_url"] as? String ?: return null
            val duration = this["duration"] as? Double ?: return null
            return Video(duration.toInt(), dashUrl, fallbackUrl)
        }
    }
}

