package com.ducktapedapps.updoot.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.LinearSubmissionsItemBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.fragments.SubredditFragment

class SubmissionsAdapter(
        private val clickHandler: SubredditFragment.ClickHandler
) : ListAdapter<LinkData, SubmissionsAdapter.SubmissionHolder>(CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionHolder {
        return SubmissionHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.linear_submissions_item,
                        parent,
                        false
                )
        )
    }

    override fun submitList(list: MutableList<LinkData>?) {
        val updatedList: MutableList<LinkData> = mutableListOf()
        if (list != null) {
            updatedList.addAll(list)
        }
        super.submitList(updatedList)
    }

    override fun onBindViewHolder(holder: SubmissionHolder, position: Int) {
        holder.binding.linkdata = getItem(position)
        holder.binding.itemIndex = position
        holder.binding.clickHandler = clickHandler
        holder.binding.executePendingBindings()
    }

    class SubmissionHolder(val binding: LinearSubmissionsItemBinding) : RecyclerView.ViewHolder(binding.root)

    private object CALLBACK : DiffUtil.ItemCallback<LinkData>() {
        override fun areItemsTheSame(oldItem: LinkData, newItem: LinkData): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: LinkData, newItem: LinkData): Boolean {
            return if (oldItem.selftext != null) {
                (oldItem.isSelfTextExpanded == newItem.isSelfTextExpanded
                        && oldItem.ups == newItem.ups)
            } else
                oldItem.ups == newItem.ups
        }
    }
}
