package com.ducktapedapps.updoot.utils.linkMetaData

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI

@Throws(Exception::class)
suspend fun fetchMetaDataFrom(url: String): Document = withContext(Dispatchers.IO) {
    Jsoup.connect(url).get()
}

fun Document.extractMetaData(): Map<String, String> {
    val map = HashMap<String, String>()
    this.select("meta").forEach {
        val property: String? = it.attr("property")
        val content: String? = it.attr("content")
        if (property != null && content != null) map[property] = content
    }
    return map
}

fun Map<String, String>.toLinkModel(url: String): LinkModel {
    var title: String? = null
    var icon: String? = null
    val siteName: String = URI.create(url).authority
    var description: String? = null
    this.keys.forEach {
        when (it) {
            "og:title" -> title = this["og:title"]
            "og:description" -> description = this["og:description"]
            "og:image" -> icon = this["og:image"]
        }
    }
    return LinkModel(url, siteName, title, description, icon)
}

