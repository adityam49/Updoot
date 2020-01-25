package com.ducktapedapps.updoot.ui.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.CommentItemBinding
import com.ducktapedapps.updoot.databinding.MoreCommentItemBinding
import com.ducktapedapps.updoot.model.BaseComment
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.MoreCommentData

class CommentsAdapter(
        private val clickHandler: CommentsFragment.ClickHandler
) : ListAdapter<BaseComment, RecyclerView.ViewHolder>(CALLBACK) {

    companion object {
        const val COMMENT = 1
        const val MORE_COMMENT = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == COMMENT)
            CommentHolder(CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else
            MoreCommentHolder(MoreCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemViewType(position: Int) =
            if (getItem(position) is CommentData) COMMENT
            else MORE_COMMENT


    override fun submitList(list: List<BaseComment>?) {
        val updateList: MutableList<BaseComment> = mutableListOf()
        if (list != null) updateList.addAll(list)
        super.submitList(updateList)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == COMMENT) {
            val commentHolder = holder as CommentHolder
            commentHolder.binding.commentData = getItem(position) as CommentData
            commentHolder.binding.executePendingBindings()
        } else {
            val moreCommentHolder = holder as MoreCommentHolder
            moreCommentHolder.binding.moreComment = getItem(position) as MoreCommentData
            moreCommentHolder.binding.executePendingBindings()
        }
    }

    inner class MoreCommentHolder(val binding: MoreCommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { clickHandler.onClick(adapterPosition) }
        }
    }

    inner class CommentHolder(val binding: CommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { clickHandler.onClick(adapterPosition) }
        }
    }

    private object CALLBACK : DiffUtil.ItemCallback<BaseComment>() {
        override fun areItemsTheSame(oldItem: BaseComment, newItem: BaseComment): Boolean {
            return if (oldItem is CommentData && newItem is CommentData)
                oldItem.id == newItem.id
            else if (oldItem is MoreCommentData && newItem is MoreCommentData)
                oldItem.id == newItem.id
            else false
        }

        override fun areContentsTheSame(oldItem: BaseComment, newItem: BaseComment): Boolean {
            return if (oldItem is CommentData && newItem is CommentData) {
                oldItem.ups == newItem.ups
                        && oldItem.repliesExpanded == newItem.repliesExpanded
            } else true
        }
    }
}
