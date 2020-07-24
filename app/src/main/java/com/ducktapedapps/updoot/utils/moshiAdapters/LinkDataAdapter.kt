package com.ducktapedapps.updoot.utils.moshiAdapters

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
            imageSet = getImagePreviews(preview)
        }
        val url = data["url"] as String
        val selfText = data["selftext"] as? String
        val postHint = data["post_hint"] as? String
                ?: if (imageSet != null) "image"
                else "self"
        val videoUrl = (data["secure_media"] as? Map<String, *>)?.run {
            (this["reddit_video"] as? Map<String, *>)?.run {
                this["dash_url"] as? String?
            }
        }

        return LinkData(
                selftext = selfText,
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
                url = url,
                permalink = data["permalink"] as String,
                over_18 = data["over_18"] as Boolean,
                gildings = getGildings(data["gildings"] as Map<String, *>),
                imageSet = imageSet,
                lastUpdated = System.currentTimeMillis() / 1000,
                post_hint = postHint,
                stickied = data["stickied"] as Boolean,
                videoUrl = videoUrl
        )
    }

    //TODO : refactor this monstrosity
    @Suppress("Unchecked_cast")
    private fun getImagePreviews(preview: Map<*, *>): ImageSet? {

        var highRes: ImageModel? = null
        var lowRes: ImageModel? = null
        (preview["enabled"] as? Boolean)?.let { shouldFetchImage ->
            (preview["images"] as? List<Map<*, *>>)?.let { images ->
                images.first().let { allImages ->
                    (allImages["source"] as? Map<String, *>)?.let { source ->
                        highRes = ImageModel(
                                (source["url"] as String).replace("amp;", "").replace("\u0026", "&"),
                                (source["height"] as Double).toInt(),
                                (source["width"] as Double).toInt()
                        )
                    }
                    (allImages["resolutions"] as? List<Map<*, *>>)?.map { image ->
                        ImageModel(
                                (image["url"] as String).replace("amp;", "").replace("\u0026", "&"),
                                (image["height"] as Double).toInt(),
                                (image["width"] as Double).toInt()
                        )
                    }?.toList()?.let {
                        lowRes = chooseLowResImage(it)
                    }
                }
            }
        }
        if (lowRes == null) return null
        return ImageSet(
                lowResUrl = lowRes!!.url,
                lowResHeight = lowRes!!.height,
                lowResWidth = lowRes!!.width,
                highResHeight = highRes!!.height,
                highResUrl = highRes!!.url,
                highResWidth = highRes!!.width
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