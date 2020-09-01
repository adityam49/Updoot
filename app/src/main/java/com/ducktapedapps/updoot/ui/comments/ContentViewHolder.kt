package com.ducktapedapps.updoot.ui.comments

import android.text.Spanned
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ItemContentImageBinding
import com.ducktapedapps.updoot.databinding.ItemContentLinkBinding
import com.ducktapedapps.updoot.databinding.ItemContentSelftextBinding
import com.ducktapedapps.updoot.utils.linkMetaData.LinkModel

sealed class ContentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class LinkViewHolder(private val binding: ItemContentLinkBinding) : ContentViewHolder((binding.root)) {
        fun bind(model: LinkModel) = binding.apply {
            titleTextView.text = model.title
            Glide
                    .with(thumbnailImageView)
                    .load(model.image)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_link_24dp)
                    .error(R.drawable.ic_image_error_24dp)
                    .into(thumbnailImageView)
            descriptionTextView.text = model.description
        }
    }

    class SelfTextViewHolder(val binding: ItemContentSelftextBinding) : ContentViewHolder(binding.root) {
        fun bind(parsedMarkdown: Spanned) = binding.apply {
            selftextTextView.text = parsedMarkdown
        }
    }

    class ImageViewHolder(val binding: ItemContentImageBinding) : ContentViewHolder(binding.root) {
        fun bind(lowResUrl: String?, highResUrl: String?) = binding.apply {
            Glide
                    .with(imageView)
                    .load(highResUrl)
                    .thumbnail(Glide.with(imageView).load(lowResUrl))
                    .apply(RequestOptions().transform(CenterInside(), RoundedCorners(16)))
                    .placeholder(R.color.color_background)
                    .into(imageView)
        }
    }

    class PlaceHolder(val binding: FrameLayout) : ContentViewHolder(binding)
    //class VideoViewHolder(val binding: ) : ContentViewHolder(binding.root)
}