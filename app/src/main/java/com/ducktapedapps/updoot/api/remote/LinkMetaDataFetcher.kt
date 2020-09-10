package com.ducktapedapps.updoot.api.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

fun fetchMetaDataFrom(url: String) =
        flow {
            @Suppress("BlockingMethodInNonBlockingContext")
            emit(Jsoup.connect(url).get())
        }
                .flowOn(Dispatchers.IO)
                .toMetaDataMap()
                .toLinkModel(url)

fun Flow<Document>.toMetaDataMap(): Flow<Map<String, String>> = map {
    val metaDataMap = HashMap<String, String>()
    it.select("meta").forEach { element ->
        val property: String? = element.attr("property")
        val content: String? = element.attr("content")
        if (property != null && content != null) metaDataMap[property] = content
    }
    metaDataMap
}

fun Flow<Map<String, String>>.toLinkModel(url: String): Flow<LinkModel> = map { metaDataMap ->
    var title: String? = null
    var icon: String? = null
    val siteName: String = URI.create(url).authority
    var description: String? = null
    metaDataMap.keys.forEach { key ->
        when (key) {
            "og:title" -> title = metaDataMap["og:title"]
            "og:description" -> description = metaDataMap["og:description"]
            "og:image" -> icon = metaDataMap["og:image"]
        }
    }
    LinkModel(url, siteName, title, description, icon)
}

data class LinkModel(
        val url: String,
        val siteName: String,
        val title: String?,
        val description: String?,
        val image: String?
)