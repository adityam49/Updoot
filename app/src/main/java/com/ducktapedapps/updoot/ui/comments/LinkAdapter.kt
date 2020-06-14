package com.ducktapedapps.updoot.ui.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ItemLinkBinding
import com.ducktapedapps.updoot.ui.comments.LinkAdapter.LinkViewHolder
import com.ducktapedapps.updoot.utils.linkMetaData.LinkModel

class LinkAdapter : Adapter<LinkViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder =
            LinkViewHolder(ItemLinkBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    var linkModel: LinkModel? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = if (linkModel == null) 0 else 1

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        holder.bind(linkModel!!)
    }

    class LinkViewHolder(private val binding: ItemLinkBinding) : ViewHolder(binding.root) {
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
}