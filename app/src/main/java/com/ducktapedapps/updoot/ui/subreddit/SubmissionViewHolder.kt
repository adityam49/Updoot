package com.ducktapedapps.updoot.ui.subreddit

import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
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
import com.ducktapedapps.updoot.model.Gildings
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.common.SwipeableViewHolder
import com.ducktapedapps.updoot.utils.CenteredImageSpan
import com.ducktapedapps.updoot.utils.Truss
import com.ducktapedapps.updoot.utils.getCompactCountAsString

sealed class SubmissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), SwipeableViewHolder {
    abstract fun bind(submissions: LinkData,
                      actionOpenComments: (String, String) -> Unit,
                      actionOpenOption: (String) -> Unit,
                      actionOpenImage: (String, String) -> Unit
    )

    class CompactImageViewHolder(val binding: CompactSubmissionImageBinding) : SubmissionViewHolder(binding.root) {
        override fun bind(
                submissions: LinkData,
                actionOpenComments: (String, String) -> Unit,
                actionOpenOption: (String) -> Unit,
                actionOpenImage: (String, String) -> Unit
        ) {
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        actionOpenOption(id)
                        true
                    }
                    if (imageSet != null) thumbnailImageView.setOnClickListener {
                        actionOpenImage(imageSet.lowResUrl!!, imageSet.highResUrl!!)
                    }
                    setGildings(gildingTextView, gildings)
                    setThumbnail(thumbnailImageView, thumbnail, over_18)
                    setVotes(scoreTextView, ups, likes)
                    titleTextView.text = title
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }
    }

    class CompactSelfTextViewHolder(val binding: CompactSubmissionSelftextBinding) : SubmissionViewHolder(binding.root) {
        override fun bind(
                submissions: LinkData,
                actionOpenComments: (String, String) -> Unit,
                actionOpenOption: (String) -> Unit,
                actionOpenImage: (String, String) -> Unit
        ) {
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        actionOpenOption(id)
                        true
                    }
                    setGildings(gildingTextView, gildings)
                    setVotes(scoreTextView, ups, likes)
                    titleTextView.text = title
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }
    }

    class LargeSubmissionImageViewHolder(val binding: LargeSubmissionImageBinding) : SubmissionViewHolder(binding.root) {
        override fun bind(
                submissions: LinkData,
                actionOpenComments: (String, String) -> Unit,
                actionOpenOption: (String) -> Unit,
                actionOpenImage: (String, String) -> Unit
        ) {
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        actionOpenOption(id)
                        true
                    }
                    previewImageView.apply {
                        setOnClickListener { actionOpenImage(submissions.imageSet!!.lowResUrl!!, submissions.imageSet.highResUrl!!) }
                        Glide.with(this)
                                .load(submissions.imageSet?.lowResUrl)
                                .thumbnail(Glide.with(previewImageView).load(thumbnail))
                                .placeholder(R.color.color_on_surface)
                                .error(R.drawable.ic_image_error_24dp)
                                .into(this)
                    }
                    setGildings(gildingTextView, gildings)
                    setVotes(scoreTextView, ups, likes)
                    titleTextView.text = title
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }
    }

    class LargeSubmissionSelfTextViewHolder(val binding: LargeSubmissionSelftextBinding) : SubmissionViewHolder(binding.root) {
        override fun bind(
                submissions: LinkData,
                actionOpenComments: (String, String) -> Unit,
                actionOpenOption: (String) -> Unit,
                actionOpenImage: (String, String) -> Unit
        ) {

            binding.apply {
                with(submissions) {
                    root.setOnClickListener { actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        actionOpenOption(id)
                        true
                    }
                    setGildings(gildingTextView, gildings)
                    setVotes(scoreTextView, ups, likes)
                    titleTextView.text = title
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                    selftextTextView.apply {
                        if (selftext.isNullOrBlank()) visibility = View.GONE
                        else {
                            visibility = View.VISIBLE
                            text = selftext
                            maxLines = 10
                            isClickable = true
                            setSelfText(this)
                        }
                    }
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

private fun setGildings(textView: TextView, gildings: Gildings) {
    val truss = Truss()
    if (gildings.platinum != 0)
        truss.pushSpan(CenteredImageSpan(getDrawable(R.drawable.ic_platinum_gilding_14dp, textView)))
                .append(" ")
                .popSpan()
                .append("x${gildings.platinum} ")
    if (gildings.gold != 0)
        truss.pushSpan(CenteredImageSpan(getDrawable(R.drawable.ic_gold_gilding_14dp, textView)))
                .append(" ")
                .popSpan()
                .append("x${gildings.gold} ")
    if (gildings.silver != 0)
        truss.pushSpan(CenteredImageSpan(getDrawable(R.drawable.ic_silver_gilding_14dp, textView)))
                .append(" ")
                .popSpan()
                .append("x${gildings.silver} ")
    textView.text = truss.build()
}

private fun getDrawable(@DrawableRes res: Int, textView: TextView): Drawable = ContextCompat.getDrawable(textView.context, res)!!.apply {
    setBounds(0, 0, intrinsicHeight, intrinsicWidth)
}

private fun setVotes(textView: TextView, votes: Int, likes: Boolean?) {
    when {
        likes == null -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.color_on_background))
        likes -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.upVoteColor))
        else -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.downVoteColor))
    }

    textView.text = getCompactCountAsString(votes.toLong())
}

private fun setThumbnail(thumbnailImageView: ImageView, thumbnail: String?, isNsfw: Boolean) {
    if (thumbnail != null) {
        Glide.with(thumbnailImageView.context)
                .load(
                        if (isNsfw) R.drawable.ic_nsfw_24dp
                        else
                            when (thumbnail) {
                                "self" -> R.drawable.ic_selftext_24dp
                                "default", "", null -> R.drawable.ic_link_24dp
                                else -> thumbnail
                            }
                )
                .apply(RequestOptions.circleCropTransform())
                .error(R.drawable.ic_image_error_24dp)
                .into(thumbnailImageView)
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