package com.ducktapedapps.updoot.utils.linkMetaData

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

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
    var siteName: String? = null
    var description: String? = null
    this.keys.forEach {
        siteName = Uri.parse(url).authority
        when (it) {
            "og:title" -> title = this["og:title"]
            "og:description" -> description = this["og:description"]
            "og:image" -> icon = this["og:image"]
        }
    }
    return LinkModel(url, siteName, title, description, icon)
}

