package com.ducktapedapps.updoot.ui.subreddit.options

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.SubmissionOptionItemBinding

class SubmissionOptionViewHolder(val binding: SubmissionOptionItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(option: SubmissionOptionUiModel) = binding.apply {
        icon.setImageDrawable(ContextCompat.getDrawable(icon.context, option.icon))
        optionTitle.text = option.name
    }

}