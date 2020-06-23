package com.ducktapedapps.updoot.ui.comments

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.ItemContentImageBinding
import com.ducktapedapps.updoot.databinding.ItemContentLinkBinding
import com.ducktapedapps.updoot.databinding.ItemContentSelftextBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.comments.ContentViewHolder.*
import com.ducktapedapps.updoot.utils.linkMetaData.LinkModel
import io.noties.markwon.Markwon
import javax.inject.Inject

class ContentAdapter @Inject constructor(private val markwonInstance: Markwon) : RecyclerView.Adapter<ContentViewHolder>() {
    var content: CommentScreenContent? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        Log.d("OnCreateViewHolder", "onCreateViewHolder() called  with: parent = [$parent], viewType = [$viewType]")
        return when (viewType) {
            SELF_TEXT -> SelfTextViewHolder(ItemContentSelftextBinding.inflate(layoutInflater, parent, false))
            LINK -> LinkViewHolder(ItemContentLinkBinding.inflate(layoutInflater, parent, false))
            IMAGE -> ImageViewHolder(ItemContentImageBinding.inflate(layoutInflater, parent, false))
            else -> throw  RuntimeException("Invalid view type requested : $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int =
            when (content) {
                is LinkModel -> LINK
                is LinkData -> {
                    when {
                        !(content as LinkData).selftext.isNullOrBlank() -> {
                            SELF_TEXT
                        }
                        (content as LinkData).imageSet != null -> {
                            IMAGE
                        }
                        else -> throw RuntimeException("Invalid content type : $content")
                    }
                }
                else -> throw RuntimeException("Invalid content type : $content")
            }

    override fun getItemCount() = if (content == null) 0 else 1

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        when (holder) {
            is LinkViewHolder -> holder.bind(content as LinkModel)
            is SelfTextViewHolder -> holder.bind((content as LinkData).selftext!!, markwonInstance)
            is ImageViewHolder -> holder.bind((content as LinkData).imageSet!!)
        }
    }

    private companion object {
        const val SELF_TEXT = 1
        const val LINK = 2
        const val IMAGE = 3
    }
}