package com.ducktapedapps.updoot.data.remote.moshiAdapters

import com.ducktapedapps.updoot.data.remote.model.Comment
import com.ducktapedapps.updoot.data.remote.model.Listing
import com.ducktapedapps.updoot.data.remote.model.RedditThing
import com.squareup.moshi.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class UpdootAdapterFactory private constructor(
        private val typeLabelKey: String,
        private val dataKey: String,
        private val typeLabelToType: MutableMap<String, Type> = mutableMapOf()
) : JsonAdapter.Factory {

    companion object {
        fun of(typeKey: String, dataKey: String) = UpdootAdapterFactory(typeKey, dataKey)
    }

    fun <T> withSubType(typeLabel: String, classType: Class<T>): UpdootAdapterFactory {
        if (typeLabel !in typeLabelToType.keys)
            typeLabelToType += typeLabel to classType
        else throw IllegalArgumentException("type label should be unique. typeLabel : '$typeLabel' is already registered!")
        return this
    }

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val typeToAdapter = mutableMapOf<Type, JsonAdapter<Any>>()
        for ((_, dataType) in typeLabelToType) {
            typeToAdapter[dataType] = moshi.nextAdapter(this, dataType, emptySet())
        }
        val polymorphicAdapter = PolymorphicAdapter(
                typeLabelKey,
                dataKey,
                typeLabelToType,
                typeToAdapter
        )
        val requiredType = Types.getRawType(type)

        return if (requiredType.isAssignableFrom(Listing::class.java)) {
            val parameterizedType = (type as ParameterizedType).actualTypeArguments.first()
            val nextAnnotation = Types.nextAnnotations(annotations, InconsistentApiResponse::class.java)

            if (nextAnnotation != null) MoreCommentAdapter(polymorphicAdapter)
            else if (
                    parameterizedType == Comment::class.java  //TODO : remove this check
                    || parameterizedType == RedditThing::class.java //TODO : remove this check
                    || parameterizedType in typeLabelToType.values
            ) ListingAdapter(polymorphicAdapter)
            else null

        } else if (requiredType in typeLabelToType.values)
            polymorphicAdapter
        else null
    }
}

class PolymorphicAdapter(
        private val typeLabelKey: String,
        private val dataKey: String,
        private val typeLabelToType: MutableMap<String, Type>,
        private val typeToJsonAdapter: MutableMap<Type, JsonAdapter<Any>>
) : JsonAdapter<Any>() {
    override fun fromJson(reader: JsonReader): Any? {
        val path = reader.path
        val properties = reader.readJsonValue() as Map<*, *>?
                ?: throw JsonDataException("Expected object at $path")
        val label = properties[typeLabelKey] as String?
                ?: throw JsonDataException("Non-null value '$typeLabelKey' was null at $path")
        val data = properties[dataKey]
                ?: throw JsonDataException("Non-null value '$dataKey' was null at $path")

        return getAdapterForLabel(label).fromJsonValue(data)
    }


    private fun getAdapterForLabel(label: String): JsonAdapter<Any> {
        val type = typeLabelToType[label] ?: throw JsonDataException(
                "Expected one of ${typeLabelToType.keys}"
                        + " for key '$typeLabelKey' but found '$label'. Register a subtype for this label."
        )
        return typeToJsonAdapter[type]
                ?: throw IllegalArgumentException("No adapter registered for type $type")
    }

    override fun toJson(writer: JsonWriter, value: Any?) {}
}