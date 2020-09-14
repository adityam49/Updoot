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
import com.ducktapedapps.updoot.data.local.model.CommentData
import com.ducktapedapps.updoot.data.local.model.MoreCommentData
import com.ducktapedapps.updoot.databinding.CommentItemBinding
import com.ducktapedapps.updoot.databinding.MoreCommentItemBinding
import com.ducktapedapps.updoot.ui.common.SwipeableViewHolder
import com.ducktapedapps.updoot.utils.RoundedBackgroundSpan
import com.ducktapedapps.updoot.utils.Truss
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.mapToRepliesModel


sealed class CommentsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    class MoreCommentHolder(val binding: MoreCommentItemBinding, singleThreadMode: Boolean, singleThreadColorMode: Boolean) : CommentsViewHolder(binding.root) {
        init {
            binding.indentView.apply {
                this.singleThreadMode = singleThreadMode
                this.singleThreadColor = singleThreadColorMode
            }
        }

        fun bind(data: MoreCommentData) = binding.apply {
            indentView.setIndentLevel(data.depth)
            moreCommentCountTv.text = String.format("Load %d more %s", 1, "comments")
        }
    }

    class CommentHolder(val binding: CommentItemBinding, singleThreadMode: Boolean, singleThreadColorMode: Boolean) : CommentsViewHolder(binding.root), SwipeableViewHolder {
        init {
            binding.indentView.apply {
                this.singleThreadMode = singleThreadMode
                this.singleThreadColor = singleThreadColorMode
            }
        }

        private var extremeLeftSwipeDataValue: String? = null
        private var leftSwipeDataValue: String? = null
        private var rightSwipeDataValue: String? = null
        private var extremeRightSwipeDataValue: String? = null

        override fun getExtremeLeftSwipeData(): String? = extremeLeftSwipeDataValue
        override fun getLeftSwipeData(): String? = leftSwipeDataValue
        override fun getRightSwipeData(): String? = rightSwipeDataValue
        override fun getExtremeRightSwipeData(): String? = extremeRightSwipeDataValue

        fun bind(data: CommentData, expandCollapseComment: (index: Int) -> Unit) {
            leftSwipeDataValue = data.name
            rightSwipeDataValue = data.name
            extremeRightSwipeDataValue = data.name
            extremeLeftSwipeDataValue = data.name

            binding.apply {
                indentView.setIndentLevel(data.depth)
                textViewCommentHeader.bindHeader(data, expandCollapseComment)
                textViewCommentBody.bindCommentBody(data, expandCollapseComment)
                scoreView.setData(data.ups ?: 0, data.likes)
                textViewChildrenCount.bindChildCount(data.repliesExpanded, data.replies.mapToRepliesModel().size)
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
                        append(if (!data.author_flair_text.isNullOrBlank()) String.format(" · %s ", data.author_flair_text) else "")
                    }.build(), TextView.BufferType.SPANNABLE)
        }

        private fun getColor(@ColorRes color: Int): Int = ContextCompat.getColor(binding.root.context, color)
    }
}
