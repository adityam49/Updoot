package com.ducktapedapps.updoot.ui.comments

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ducktapedapps.updoot.databinding.ItemContentImageBinding
import com.ducktapedapps.updoot.databinding.ItemContentLinkBinding
import com.ducktapedapps.updoot.databinding.ItemContentSelftextBinding
import com.ducktapedapps.updoot.ui.comments.ContentViewHolder.*
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.*

class ContentAdapter : ListAdapter<SubmissionContent, ContentViewHolder>(CALLBACK) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        Log.d("OnCreateViewHolder", "onCreateViewHolder() called  with: parent = [$parent], viewType = [$viewType]")
        return when (viewType) {
            SELF_TEXT -> SelfTextViewHolder(ItemContentSelftextBinding.inflate(layoutInflater, parent, false))
            LINK -> LinkViewHolder(ItemContentLinkBinding.inflate(layoutInflater, parent, false))
            IMAGE -> ImageViewHolder(ItemContentImageBinding.inflate(layoutInflater, parent, false))
            PLACEHOLDER -> PlaceHolder(FrameLayout(parent.context, null))
            else -> throw  RuntimeException("Invalid view type requested : $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int =
            when (currentList[position]) {
                is Image -> IMAGE
                is Video -> PLACEHOLDER
                is SelfText -> SELF_TEXT
                is Link -> LINK
                JustTitle -> PLACEHOLDER
            }


    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        when (holder) {
            is LinkViewHolder -> holder.bind((currentList[0] as Link).linkModel)
            is SelfTextViewHolder -> holder.bind((currentList[0] as SelfText).parsedMarkdown)
            is ImageViewHolder -> holder.bind((currentList[0] as Image).data.lowResUrl, (currentList[0] as Image).data.highResUrl)
        }
    }

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<SubmissionContent>() {
            override fun areItemsTheSame(oldItem: SubmissionContent, newItem: SubmissionContent): Boolean =
                    oldItem is Link && newItem is Link
                            || oldItem is Image && newItem is Image
                            || oldItem is Video && newItem is Video
                            || oldItem is SelfText && newItem is SelfText
                            || oldItem is JustTitle && newItem is JustTitle

            override fun areContentsTheSame(oldItem: SubmissionContent, newItem: SubmissionContent): Boolean = true
        }
        const val SELF_TEXT = 1
        const val LINK = 2
        const val IMAGE = 3
        const val PLACEHOLDER = 4
    }
}