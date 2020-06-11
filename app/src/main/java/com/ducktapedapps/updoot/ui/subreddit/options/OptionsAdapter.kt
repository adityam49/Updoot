package com.ducktapedapps.updoot.ui.subreddit.options

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ducktapedapps.updoot.databinding.SubmissionOptionItemBinding

class OptionsAdapter(private val copyLink: (link: String) -> Unit) : ListAdapter<SubmissionOptionUiModel, SubmissionOptionViewHolder>(CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionOptionViewHolder =
            SubmissionOptionViewHolder(SubmissionOptionItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: SubmissionOptionViewHolder, position: Int) {
        holder.bind(getItem(position), copyLink)
    }

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<SubmissionOptionUiModel>() {
            override fun areItemsTheSame(oldItem: SubmissionOptionUiModel, newItem: SubmissionOptionUiModel) =
                    oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: SubmissionOptionUiModel, newItem: SubmissionOptionUiModel) = true
        }
    }
}