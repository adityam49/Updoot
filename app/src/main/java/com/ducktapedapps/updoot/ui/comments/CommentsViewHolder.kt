package com.ducktapedapps.updoot.ui.comments

import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.CommentItemBinding
import com.ducktapedapps.updoot.databinding.MoreCommentItemBinding
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.MoreCommentData
import com.ducktapedapps.updoot.utils.RoundedBackgroundSpan
import com.ducktapedapps.updoot.utils.Truss
import com.ducktapedapps.updoot.utils.getCompactCountAsString


sealed class CommentsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class MoreCommentHolder(val binding: MoreCommentItemBinding) : CommentsViewHolder(binding.root) {
        fun bind(data: MoreCommentData) = binding.apply {
            indentView.setIndentLevel(data.depth)
            moreCommentCountTv.text = String.format("Load %d more %s", data.children.size, if (data.children.size == 1) "comment" else "comments")
        }
    }

    class CommentHolder(val binding: CommentItemBinding) : CommentsViewHolder(binding.root) {
        fun bind(data: CommentData, expandCollapseComment: (index: Int) -> Unit) {
            binding.apply {
                indentView.setIndentLevel(data.depth)
                textViewCommentHeader.bindHeader(data, expandCollapseComment)
                textViewCommentBody.bindCommentBody(data, expandCollapseComment)
                textScore.bindScore(data)
                textViewChildrenCount.bindChildCount(data.repliesExpanded, data.replies.size)
            }
        }

        private fun TextView.bindChildCount(repliesExpanded: Boolean, childrenCount: Int) = apply {
            val formattedString = "+${getCompactCountAsString(childrenCount.toLong())}"
            visibility = if (childrenCount == 0) View.GONE
            else if (!repliesExpanded) {
                setText(SpannableString(formattedString).apply {
                    setSpan(
                            RoundedBackgroundSpan(getColor(R.color.color_secondary_alt), getColor(R.color.color_on_secondary)),
                            0,
                            formattedString.length,
                            Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }, TextView.BufferType.SPANNABLE)
                View.VISIBLE
            } else View.GONE
        }


        private fun TextView.bindCommentBody(data: CommentData, expandCollapseComment: (index: Int) -> Unit) = apply {
            setOnClickListener { expandCollapseComment(bindingAdapterPosition) }
            text = data.body
            movementMethod = LinkMovementMethod.getInstance()
        }

        private fun TextView.bindScore(data: CommentData) = apply {
            setTextColor(getColor(when (data.likes) {
                true -> R.color.upVoteColor
                false -> R.color.downVoteColor
                else -> R.color.color_on_primary
            }))
            text = data.ups?.let { getCompactCountAsString(it.toLong()) }
                    ?: "[score hidden]"
        }

        private fun TextView.bindHeader(data: CommentData, expandCollapseComment: (index: Int) -> Unit) = apply {
            setOnClickListener { expandCollapseComment(bindingAdapterPosition) }
            setText(Truss()
                    .apply {
                        pushSpan(
                                if (data.is_submitter) RoundedBackgroundSpan(getColor(R.color.color_secondary), getColor(R.color.color_on_secondary))
                                else RoundedBackgroundSpan(getColor(R.color.color_on_primary_light), getColor(R.color.color_on_primary))
                        )
                        append(data.author)
                        popSpan()
                        append(if (!data.author_flair_text.isBlank()) String.format(" · %s ", data.author_flair_text) else "")
                    }.build(), TextView.BufferType.SPANNABLE)
        }

        private fun getColor(@ColorRes color: Int): Int = ContextCompat.getColor(binding.root.context, color)
    }
}
