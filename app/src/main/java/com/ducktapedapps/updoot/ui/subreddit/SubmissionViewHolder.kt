package com.ducktapedapps.updoot.ui.subreddit

import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.CompactSubmissionImageBinding
import com.ducktapedapps.updoot.databinding.CompactSubmissionSelftextBinding
import com.ducktapedapps.updoot.databinding.LargeSubmissionImageBinding
import com.ducktapedapps.updoot.databinding.LargeSubmissionSelftextBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.common.SwipeableViewHolder
import com.ducktapedapps.updoot.utils.getCompactCountAsString

sealed class SubmissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) , SwipeableViewHolder {
    abstract fun bind(submissions: LinkData,
                      actionOpenComments: (String, String) -> Unit,
                      actionOpenOption: (String) -> Unit)

    class CompactImageViewHolder(val binding: CompactSubmissionImageBinding) : SubmissionViewHolder(binding.root) {
        override fun bind(submissions: LinkData, actionOpenComments: (String, String) -> Unit, actionOpenOption: (String) -> Unit) {
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        actionOpenOption(id)
                        true
                    }
                    setThumbnail(thumbnailImageView, thumbnail)
                    setVotes(scoreTextView, ups, likes)
                    titleTextView.text = title
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }
    }

    class CompactSelfTextViewHolder(val binding: CompactSubmissionSelftextBinding) : SubmissionViewHolder(binding.root) {
        override fun bind(submissions: LinkData, actionOpenComments: (String, String) -> Unit, actionOpenOption: (String) -> Unit) {
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        actionOpenOption(id)
                        true
                    }
                    setVotes(scoreTextView, ups, likes)
                    titleTextView.text = title
                    selftextTextView.text = selftext
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }
    }

    class LargeSubmissionImageViewHolder(val binding: LargeSubmissionImageBinding) : SubmissionViewHolder(binding.root) {
        override fun bind(submissions: LinkData, actionOpenComments: (String, String) -> Unit, actionOpenOption: (String) -> Unit) {
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        actionOpenOption(id)
                        true
                    }
                    setVotes(scoreTextView, ups, likes)
                    titleTextView.text = title
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }
    }

    class LargeSubmissionSelfTextViewHolder(val binding: LargeSubmissionSelftextBinding) : SubmissionViewHolder(binding.root) {
        override fun bind(submissions: LinkData, actionOpenComments: (String, String) -> Unit, actionOpenOption: (String) -> Unit) {

            binding.apply {
                with(submissions) {
                    root.setOnClickListener { actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        actionOpenOption(id)
                        true
                    }
                    setVotes(scoreTextView, ups, likes)
                    titleTextView.text = title
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                    selftextTextView.apply {
                        text = selftext
                        maxLines = 10
                        isClickable = true
                    }
                    setSelfText(selftextTextView)
                }
            }
        }

        private fun setSelfText(textView: TextView) {
            textView.apply {
                post {
                    if (maxLines < lineCount) {
                        setOnClickListener { expandSelfText() }
                        setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, R.drawable.ic_expand_more_black_14dp)
                    } else setOnClickListener(null)
                }
            }
        }

        private fun expandSelfText() {
            ConstraintSet().apply {
                binding.apply {
                    clone(constraintLayout)
                    selftextTextView.maxLines = selftextTextView.maxLines + (selftextTextView.lineCount * 0.2).toInt()
                    TransitionManager.beginDelayedTransition(root)
                    applyTo(constraintLayout)
                    with(selftextTextView) {
                        if (maxLines >= lineCount) {
                            setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                            isClickable = false
                        }
                    }
                }
            }
        }
    }
}

private fun setVotes(textView: TextView, votes: Int, likes: Boolean?) {
    when {
        likes == null -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.color_on_background))
        likes -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.upVoteColor))
        else -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.downVoteColor))
    }

    textView.text = getCompactCountAsString(votes.toLong())
}

private fun setThumbnail(thumbnailImageView: ImageView, thumbnail: String?) {
    if (thumbnail != null) {
        when (thumbnail) {
            "self", "" -> thumbnailImageView.setImageResource(R.drawable.ic_selftext_24dp)
            "default" -> thumbnailImageView.setImageResource(R.drawable.ic_link_24dp)
            else -> Glide.with(thumbnailImageView.context)
                    .load(thumbnail)
                    .apply(RequestOptions.circleCropTransform())
                    .error(R.drawable.ic_image_error_24dp)
                    .into(thumbnailImageView)
        }
    } else {
        thumbnailImageView.setImageResource(R.drawable.ic_selftext_24dp)
    }
}

private fun setMetadata(textView: TextView, data: LinkData) {
    val metadata = StringBuilder()
    if (data.commentsCount > 999) {
        metadata.append(data.commentsCount / 1000).append("K replies")
    } else {
        metadata.append(data.commentsCount).append(" replies")
    }
    metadata.append(" \u2022 ")

    metadata.append(DateUtils.getRelativeTimeSpanString(data.created * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))
            .append(" ")
    textView.text = metadata.toString()
}