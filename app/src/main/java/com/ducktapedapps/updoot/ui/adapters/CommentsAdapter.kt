package com.ducktapedapps.updoot.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.CommentItemBinding
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.ui.fragments.commentsFragment

class CommentsAdapter(
        private val clickHandler: commentsFragment.ClickHandler
) : ListAdapter<CommentData, CommentsAdapter.CommentHolder>(CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        return CommentHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.comment_item,
                        parent,
                        false
                )
        )
    }

    override fun submitList(list: MutableList<CommentData>?) {
        val updateList: MutableList<CommentData> = mutableListOf()
        if (list != null) updateList.addAll(list)
        super.submitList(updateList)
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.binding.commentData = getItem(position)
        holder.binding.executePendingBindings()
    }

    inner class CommentHolder(val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        init {
            binding.root.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            clickHandler.onClick(adapterPosition)
        }
    }

    private object CALLBACK : DiffUtil.ItemCallback<CommentData>() {
        override fun areItemsTheSame(oldItem: CommentData, newItem: CommentData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommentData, newItem: CommentData): Boolean {
            return oldItem.ups == newItem.ups
                    && oldItem.repliesExpanded == newItem.repliesExpanded
        }
    }
}
