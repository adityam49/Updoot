package com.ducktapedapps.updoot.ui.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        return when (viewType) {
            SELF_TEXT -> SelfTextViewHolder(ItemContentSelftextBinding.inflate(layoutInflater, parent, false))
            LINK -> LinkViewHolder(ItemContentLinkBinding.inflate(layoutInflater, parent, false))
            else -> throw  RuntimeException("Invalid view type requested : $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int =
            when (content) {
                is LinkData -> SELF_TEXT
                is LinkModel -> LINK
                else -> throw RuntimeException("Invalid content type : $content")
            }

    override fun getItemCount() = if (content == null) 0 else 1

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        when (holder) {
            is LinkViewHolder -> holder.bind(content as LinkModel)
            is SelfTextViewHolder -> holder.bind((content as LinkData).selftext!!, markwonInstance)
        }
    }

    private companion object {
        const val SELF_TEXT = 1
        const val LINK = 2
    }
}