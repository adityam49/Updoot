package com.ducktapedapps.updoot.ui.subreddit.options

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.SubmissionOptionItemBinding

class SubmissionOptionViewHolder(val binding: SubmissionOptionItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(option: SubmissionOptionUiModel, copyLink: (link: String) -> Unit, onClick: (String) -> Unit) = binding.apply {
        icon.setImageDrawable(ContextCompat.getDrawable(icon.context, option.icon))
        optionTitle.text = option.name
        if (option.name == "Copy link") root.setOnClickListener { copyLink(option.additionalData!!) }
        else root.setOnClickListener { onClick(option.name) }
    }

}