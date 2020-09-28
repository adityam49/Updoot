package com.ducktapedapps.updoot.data.remote.moshiAdapters

import com.ducktapedapps.updoot.data.local.model.ImageVariants
import com.squareup.moshi.*
import java.lang.reflect.Type

class ImageJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? =
            if (!Types.getRawType(type).isAssignableFrom(ImageVariants::class.java)) null
            else ImageJsonAdapter()
}

class ImageJsonAdapter : JsonAdapter<ImageVariants>() {

    private val TAG = "ImageJsonAdapter"

    @Suppress("UNCHECKED_CAST")
    override fun fromJson(reader: JsonReader): ImageVariants? {
        val jsonValue = reader.readJsonValue()
        var imageVariants: ImageVariants? = null
        if (jsonValue == null) return imageVariants
        val map = jsonValue as? Map<String, *>
                ?: throw JsonDataException("$jsonValue can't be mapped")
        (map["images"] as? List<Map<String, *>>)?.let { preview ->
            imageVariants = getImagePreviews(preview)
        }
        return imageVariants
    }

    @Suppress("Unchecked_cast")
    private fun getImagePreviews(images: List<Map<String, *>>): ImageVariants? {
        var highRes: ImageModel? = null
        var lowRes: ImageModel? = null
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
                lowRes = chooseLowResImageFrom(it)
            }
        }
        if (lowRes == null) return null
        return ImageVariants(
                lowResUrl = lowRes!!.url,
                lowResHeight = lowRes!!.height,
                lowResWidth = lowRes!!.width,
                highResHeight = highRes!!.height,
                highResUrl = highRes!!.url,
                highResWidth = highRes!!.width
        )
    }

    private fun chooseLowResImageFrom(images: List<ImageModel>): ImageModel? {
        val lowResAcceptableQuality = 400
        return images.let { allImages ->
            val (highResImages, lowResImages) = allImages.partition { it.height > lowResAcceptableQuality }
            if (highResImages.isNotEmpty()) highResImages.minByOrNull { it.height }
            else lowResImages.maxByOrNull { it.height }
        }
    }

    private data class ImageModel(
            val url: String,
            val height: Int,
            val width: Int
    )

    override fun toJson(writer: JsonWriter, value: ImageVariants?) {}


}