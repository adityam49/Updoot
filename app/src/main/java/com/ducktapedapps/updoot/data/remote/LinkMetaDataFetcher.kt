package com.ducktapedapps.updoot.data.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ducktapedapps.updoot.data.local.LinkMetaDataDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.util.*
import kotlin.collections.HashMap

fun String.fetchMetaData(linkMetaDataDAO: LinkMetaDataDAO): Flow<LinkModel> {
    val url = if (this.startsWith("http:")) this.replaceFirst("http:", "https:") else this
    return fetchMetaDataFromLocalSource(url, linkMetaDataDAO).flatMapLatest { cachedUrlMetaData ->
        if (cachedUrlMetaData == null)
            fetchMetaDataFromRemoteSource(url).saveToLocalSourceAndEmit(linkMetaDataDAO)
        else flow { emit(cachedUrlMetaData) }
    }
}

fun fetchMetaDataFromRemoteSource(url: String) =
        flow {
            @Suppress("BlockingMethodInNonBlockingContext")
            emit(Jsoup.connect(url).get())
        }
                .toMetaDataMap()
                .toLinkModel(url)
                .flowOn(Dispatchers.IO)

fun fetchMetaDataFromLocalSource(url: String, linkMetaDataDAO: LinkMetaDataDAO) =
        linkMetaDataDAO
                .observeUrlMetaData(url)
                .distinctUntilChanged()
                .flowOn(Dispatchers.IO)

private fun Flow<Document>.toMetaDataMap(): Flow<Map<String, String>> =
        map {
            val metaDataMap = HashMap<String, String>()
            it.select("meta").forEach { element ->
                val property: String? = element.attr("property")
                val content: String? = element.attr("content")
                if (property != null && content != null) metaDataMap[property] = content
            }
            metaDataMap
        }

private fun Flow<Map<String, String>>.toLinkModel(url: String): Flow<LinkModel> =
        map { metaDataMap ->
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
            LinkModel(url, siteName, title, description, icon, Date())
        }

private fun Flow<LinkModel>.saveToLocalSourceAndEmit(linkMetaDataDAO: LinkMetaDataDAO) =
        transform {
            val metaData = it.copy(lastUpdated = Date())
            linkMetaDataDAO.insertUrlMetaData(metaData)
            emit(metaData)
        }

@Entity
data class LinkModel(
        @PrimaryKey val url: String,
        val siteName: String,
        val title: String?,
        val description: String?,
        val image: String?,
        val lastUpdated: Date,
)