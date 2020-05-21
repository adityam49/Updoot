package com.ducktapedapps.updoot.ui.comments

import android.text.SpannableString
import android.text.Spanned
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.CommentItemBinding
import com.ducktapedapps.updoot.databinding.MoreCommentItemBinding
import com.ducktapedapps.updoot.model.BaseComment
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.MoreCommentData
import com.ducktapedapps.updoot.utils.RoundedBackgroundSpan
import com.ducktapedapps.updoot.utils.Truss
import com.ducktapedapps.updoot.utils.getCompactCountAsString


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
            binding.apply {
                rootViewComment.apply {
                    textViewCommentHeader.setOnClickListener { clickHandler.onClick(adapterPosition) }
                    textViewCommentBody.apply {
                        movementMethod = LinkMovementMethod.getInstance()
                        setOnClickListener { clickHandler.onClick(adapterPosition) }
                    }
                }
            }
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

@BindingAdapter("commentBodyContent")
fun bindCommentBody(view: TextView, text: Spanned) {
    view.text = text
}

@BindingAdapter("commentScore", "voted")
fun bindCommentScore(textView: TextView, commentScore: Int?, voted: Boolean?) {
    textView.apply {
        setTextColor(ContextCompat.getColor(textView.context, when (voted) {
            true -> R.color.upVoteColor
            false -> R.color.downVoteColor
            else -> R.color.color_on_primary
        }))
        text = commentScore?.let { getCompactCountAsString(it.toLong()) } ?: "[score hidden]"
    }
}

@BindingAdapter("commentHeaderContent")
fun bindContent(textView: TextView, commentData: CommentData) {
    textView.setText(Truss()
            .apply {
                pushSpan(
                        if (commentData.is_submitter) {
                            RoundedBackgroundSpan(
                                    ContextCompat.getColor(textView.context, R.color.color_secondary),
                                    ContextCompat.getColor(textView.context, R.color.color_on_secondary)
                            )
                        } else {
                            RoundedBackgroundSpan(
                                    ContextCompat.getColor(textView.context, R.color.color_on_primary_light),
                                    ContextCompat.getColor(textView.context, R.color.color_on_primary)
                            )
                        })
                append(commentData.author)
                popSpan()
                append(if (!commentData.author_flair_text.isBlank()) String.format(" · %s ", commentData.author_flair_text) else "")
            }.build(), TextView.BufferType.SPANNABLE)
}

@BindingAdapter("repliesExpanded", "childrenCount")
fun bindChildCount(textView: TextView, repliesExpanded: Boolean, childrenCount: Int) {
    textView.apply {
        val formattedString = "+${getCompactCountAsString(childrenCount.toLong())}"
        visibility = if (childrenCount == 0) View.GONE
        else if (!repliesExpanded) {
            setText(SpannableString(formattedString).apply {
                setSpan(RoundedBackgroundSpan(
                        ContextCompat.getColor(textView.context, R.color.color_secondary_alt),
                        ContextCompat.getColor(textView.context, R.color.color_on_secondary)
                ), 0, formattedString.length, SPAN_INCLUSIVE_INCLUSIVE)
            }, TextView.BufferType.SPANNABLE)
            View.VISIBLE
        } else View.GONE
    }
}

@BindingAdapter("threadVisibility")
fun bindThreadVisibility(indentView: IndentView, level: Int) {
    indentView.visibility = if (level == 0) View.GONE else View.VISIBLE
}