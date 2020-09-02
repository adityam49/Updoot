package com.ducktapedapps.updoot.ui.comments

import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ItemContentImageBinding
import com.ducktapedapps.updoot.databinding.ItemContentLinkBinding
import com.ducktapedapps.updoot.databinding.ItemContentSelftextBinding
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.*
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.LoadedLink
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.LoadingLink
import com.ducktapedapps.updoot.utils.Truss

sealed class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(content: SubmissionContent, clickHandler: ContentAdapter.ClickHandler)
    class LinkViewHolder(private val binding: ItemContentLinkBinding) : ContentViewHolder((binding.root)) {
        override fun bind(content: SubmissionContent, clickHandler: ContentAdapter.ClickHandler) {
            if (content is LinkState)
                binding.apply {
                    when (content) {
                        is LoadedLink -> {
                            progressCircular.visibility = View.INVISIBLE
                            titleTextView.text = Truss().apply {
                                if (!content.linkModel.title.isNullOrBlank()) {
                                    pushSpan(AbsoluteSizeSpan(16, true))
                                    append(content.linkModel.title)
                                    popSpan()
                                    append("\n\n")
                                    pushSpan(URLSpan(content.linkModel.siteName))
                                    append(content.linkModel.siteName)
                                    popSpan()
                                    append("\n\n")
                                    content.linkModel.description?.let { append(it) }
                                } else {
                                    append(content.linkModel.url)
                                }
                            }.build()
                            thumbnailImageView.apply {
                                visibility = View.VISIBLE
                                Glide
                                        .with(this)
                                        .load(content.linkModel.image)
                                        .apply(RequestOptions.circleCropTransform())
                                        .placeholder(R.drawable.ic_link_24dp)
                                        .error(R.drawable.ic_image_error_24dp)
                                        .into(this)
                            }
                        }
                        is LoadingLink -> {
                            progressCircular.visibility = View.VISIBLE
                            thumbnailImageView.visibility = View.GONE
                            titleTextView.text = content.url
                        }
                    }
                    titleTextView.apply {
                        movementMethod = LinkMovementMethod.getInstance()
                        setOnClickListener { clickHandler.onClick(content) }
                    }

                }
        }
    }

    class SelfTextViewHolder(val binding: ItemContentSelftextBinding) : ContentViewHolder(binding.root) {
        override fun bind(content: SubmissionContent, clickHandler: ContentAdapter.ClickHandler) {
            if (content is SelfText)
                binding.apply {
                    selftextTextView.apply {
                        setText(content.parsedMarkdown, TextView.BufferType.SPANNABLE)
                        movementMethod = LinkMovementMethod.getInstance()
                        setOnClickListener { clickHandler.onClick(content) }
                    }
                }
        }
    }
}

class ImageViewHolder(val binding: ItemContentImageBinding) : ContentViewHolder(binding.root) {
    override fun bind(content: SubmissionContent, clickHandler: ContentAdapter.ClickHandler) {
        if (content is Image)
            binding.imageView.apply {
                Glide
                        .with(this)
                        .load(content.data.highResUrl)
                        .thumbnail(Glide.with(this).load(content.data.lowResUrl))
                        .apply(RequestOptions().transform(CenterInside(), RoundedCorners(16)))
                        .placeholder(R.color.color_background)
                        .into(this)
                setOnClickListener { clickHandler.onClick(content) }
            }
    }

}

class PlaceHolder(val binding: FrameLayout) : ContentViewHolder(binding) {
    override fun bind(content: SubmissionContent, clickHandler: ContentAdapter.ClickHandler) = Unit
}
//class VideoViewHolder(val binding: ) : ContentViewHolder(binding.root)
