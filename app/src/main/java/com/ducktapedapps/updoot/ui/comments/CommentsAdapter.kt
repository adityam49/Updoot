package com.ducktapedapps.updoot.ui.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ducktapedapps.updoot.data.local.model.BaseComment
import com.ducktapedapps.updoot.data.local.model.CommentData
import com.ducktapedapps.updoot.data.local.model.MoreCommentData
import com.ducktapedapps.updoot.databinding.CommentItemBinding
import com.ducktapedapps.updoot.databinding.MoreCommentItemBinding
import com.ducktapedapps.updoot.ui.comments.CommentsViewHolder.CommentHolder
import com.ducktapedapps.updoot.ui.comments.CommentsViewHolder.MoreCommentHolder


class CommentsAdapter(
        private val expandCollapseComment: (index: Int) -> Unit,
        private val singleThreadMode: Boolean,
        private val singleThreadColorMode: Boolean
) : ListAdapter<BaseComment, CommentsViewHolder>(CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder =
            when (viewType) {
                COMMENT -> CommentHolder(CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), singleThreadMode, singleThreadColorMode)
                MORE_COMMENT -> MoreCommentHolder(MoreCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), singleThreadMode, singleThreadColorMode)
                else -> throw IllegalArgumentException("invalid view holder requested :$viewType")
            }

    override fun getItemViewType(position: Int) =
            if (getItem(position) is CommentData) COMMENT
            else MORE_COMMENT


    override fun submitList(list: List<BaseComment>?) {
        val updateList: MutableList<BaseComment> = mutableListOf()
        if (list != null) updateList.addAll(list)
        super.submitList(updateList)
    }

    override fun onBindViewHolder(holder: CommentsViewHolder, position: Int) {
        when (holder) {
            is CommentHolder -> holder.bind(getItem(position) as CommentData, expandCollapseComment)
            is MoreCommentHolder -> holder.bind(getItem(position) as MoreCommentData)
        }
    }

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<BaseComment>() {
            override fun areItemsTheSame(oldItem: BaseComment, newItem: BaseComment) =
                    if (oldItem is CommentData && newItem is CommentData)
                        oldItem.id == newItem.id
                    else if (oldItem is MoreCommentData && newItem is MoreCommentData)
                        oldItem.id == newItem.id
                    else false


            override fun areContentsTheSame(oldItem: BaseComment, newItem: BaseComment) =
                    if (oldItem is CommentData && newItem is CommentData) {
                        oldItem.ups == newItem.ups
                                && oldItem.repliesExpanded == newItem.repliesExpanded
                    } else true
        }
        val COMMENT = 1
        val MORE_COMMENT = 2
    }
}