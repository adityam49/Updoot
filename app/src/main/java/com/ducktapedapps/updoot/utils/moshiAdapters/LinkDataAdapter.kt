package com.ducktapedapps.updoot.utils.moshiAdapters

import android.util.Log
import com.ducktapedapps.updoot.model.Gildings
import com.ducktapedapps.updoot.model.ImageSet
import com.ducktapedapps.updoot.model.LinkData
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

class LinkDataAdapter {
    @ToJson
    fun serializeSubmissionDetail(linkData: LinkData): String = ""

    @Suppress("UNCHECKED_CAST")
    @FromJson
    fun deserializeSubmissionDetail(data: Map<*, *>): LinkData {
        var imageSet: ImageSet? = null
        (data["preview"] as? Map<*, *>)?.let { preview ->
            imageSet = getImagePreviews(preview).also {
                Log.d(TAG, "deserializeSubmissionDetail: $it")
            }
        }
        return LinkData(
                selftext = data["selftext"] as String? ?: "",
                title = data["title"] as String,
                archived = data["archived"] as Boolean,
                author = data["author"] as String,
                locked = data["locked"] as Boolean,
                ups = (data["ups"] as Double).toInt(),
                likes = data["likes"] as Boolean?,
                subredditName = data["subreddit"] as String,
                name = data["name"] as String,
                thumbnail = data["thumbnail"] as? String ?: "",
                saved = data["saved"] as Boolean,
                created = (data["created_utc"] as Double).toLong(),
                commentsCount = (data["num_comments"] as Double).toInt(),
                id = data["id"] as String,
                url = data["url"] as String,
                permalink = data["permalink"] as String,
                over_18 = data["over_18"] as Boolean,
                gildings = getGildings(data["gildings"] as Map<String, *>),
                imageSet = imageSet,
                lastUpdated = System.currentTimeMillis() / 1000,
                post_hint = data["post_hint"] as String?
        )
    }

    //TODO : refactor this monstrosity
    @Suppress("Unchecked_cast")
    private fun getImagePreviews(preview: Map<*, *>): ImageSet {

        var highRes: ImageModel? = null
        var lowRes: ImageModel? = null
        (preview["enabled"] as? Boolean)?.let { shouldFetchImage ->
            (preview["images"] as? List<Map<*, *>>)?.let { images ->
                images.first().let { allImages ->
                    (allImages["source"] as? Map<String, *>)?.let { source ->
                        highRes = ImageModel(
                                (source["url"] as String).replace("amp;", ""),
                                (source["height"] as Double).toInt(),
                                (source["width"] as Double).toInt()
                        )
                    }
                    (allImages["resolutions"] as? List<Map<*, *>>)?.map { image ->
                        ImageModel(
                                (image["url"] as String).replace("amp;", ""),
                                (image["height"] as Double).toInt(),
                                (image["width"] as Double).toInt()
                        )
                    }?.toList()?.let {
                        lowRes = chooseLowResImage(it)
                    }
                }
            }
        }
        val l: ImageModel = lowRes!!
        val h: ImageModel = highRes!!
        return ImageSet(
                lowResUrl = l.url,
                lowResHeight = l.height,
                lowResWidth = l.width,
                highResHeight = h.height,
                highResUrl = h.url,
                highResWidth = h.width
        )
    }

    private fun chooseLowResImage(images: List<ImageModel>): ImageModel? {
        val lowResAcceptableQuality = 400
        return images.let { allImages ->
            val (highResImages, lowResImages) = allImages.partition { it.height > lowResAcceptableQuality }
            if (highResImages.isNotEmpty()) highResImages.minBy { it.height }
            else lowResImages.maxBy { it.height }
        }
    }

    private fun getGildings(map: Map<String, *>): Gildings =
            Gildings(
                    silver = (map["gid_1"] as? Double)?.toInt() ?: 0,
                    gold = (map["gid_2"] as? Double)?.toInt() ?: 0,
                    platinum = (map["gid_3"] as? Double)?.toInt() ?: 0
            )

    private data class ImageModel(
            val url: String,
            val height: Int,
            val width: Int
    )

    private companion object {
        const val TAG = "LinkDataAdapter"
    }
}