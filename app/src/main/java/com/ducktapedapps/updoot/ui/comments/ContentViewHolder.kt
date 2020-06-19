package com.ducktapedapps.updoot.ui.comments

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ItemContentLinkBinding
import com.ducktapedapps.updoot.databinding.ItemContentSelftextBinding
import com.ducktapedapps.updoot.utils.linkMetaData.LinkModel
import io.noties.markwon.Markwon

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
        fun bind(selfText: String, markwon: Markwon) = binding.apply {
            selftextTextView.text = markwon.toMarkdown(selfText)
        }
    }

    //TODO
    //class ImageViewHolder(val binding: ) : ContentViewHolder(binding.root)
    //class VideoViewHolder(val binding: ) : ContentViewHolder(binding.root)
}