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
import com.ducktapedapps.updoot.ui.comments.ContentViewHolder.LinkViewHolder
import com.ducktapedapps.updoot.ui.comments.ContentViewHolder.SelfTextViewHolder
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.*
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.LoadedLink
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.LoadingLink

class ContentAdapter(private val clickHandler: ClickHandler) : ListAdapter<SubmissionContent, ContentViewHolder>(CALLBACK) {
    interface ClickHandler {
        fun onClick(content: SubmissionContent)
    }

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
                is LinkState -> LINK
                JustTitle -> PLACEHOLDER
            }


    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(currentList[0], clickHandler)
    }

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<SubmissionContent>() {
            override fun areItemsTheSame(oldItem: SubmissionContent, newItem: SubmissionContent): Boolean =
                    oldItem is LoadingLink && newItem is LoadingLink
                            || oldItem is LoadedLink && newItem is LoadedLink
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