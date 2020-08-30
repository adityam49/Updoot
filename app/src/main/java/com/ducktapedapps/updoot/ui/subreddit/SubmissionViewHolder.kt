package com.ducktapedapps.updoot.ui.subreddit

import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.format.DateUtils
import android.util.Log
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
import com.ducktapedapps.updoot.ui.subreddit.SubmissionsAdapter.SubmissionClickHandler
import com.ducktapedapps.updoot.utils.CenteredImageSpan
import com.ducktapedapps.updoot.utils.ImgurResult
import com.ducktapedapps.updoot.utils.Truss
import com.ducktapedapps.updoot.utils.getImgurMedia

sealed class SubmissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), SwipeableViewHolder {
    abstract fun bind(submissions: LinkData)
    abstract fun updateVote(submissions: LinkData)

    //TODO : implement save indication changed on submission
    fun updateSaveState(submissions: LinkData) = Unit

    private var extremeLeftSwipeDataValue: String? = null
    private var leftSwipeDataValue: String? = null
    private var rightSwipeDataValue: String? = null
    private var extremeRightSwipeDataValue: String? = null

    override fun getExtremeLeftSwipeData(): String? = extremeLeftSwipeDataValue
    override fun getLeftSwipeData(): String? = leftSwipeDataValue
    override fun getRightSwipeData(): String? = rightSwipeDataValue
    override fun getExtremeRightSwipeData(): String? = extremeRightSwipeDataValue

    private fun setSwipeData(linkData: LinkData) {
        extremeLeftSwipeDataValue = linkData.subredditName
        leftSwipeDataValue = linkData.id
        rightSwipeDataValue = linkData.id
        extremeRightSwipeDataValue = linkData.id
    }

    class CompactImageViewHolder(
            private val binding: CompactSubmissionImageBinding,
            private val clickHandler: SubmissionClickHandler
    ) : SubmissionViewHolder(binding.root) {

        override fun bind(submissions: LinkData) {
            super.setSwipeData(submissions)
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { clickHandler.actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        clickHandler.actionOpenOption(id)
                        true
                    }
                    thumbnailImageView.setOnClickListener {
                        clickHandler.performAction(this)
                    }
                    setGildings(gildingTextView, gildings)
                    setThumbnail(thumbnailImageView, thumbnail, over_18)
                    scoreView.setData(ups, likes)
                    setTitle(stickied, title, titleTextView)
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }

        override fun updateVote(submissions: LinkData) {
            binding.scoreView.apply {
                when (submissions.likes) {
                    true -> upVote(submissions.ups)
                    false -> downVote(submissions.ups)
                    null -> unVote(submissions.ups)
                }
            }
        }


    }

    class CompactSelfTextViewHolder(
            private val binding: CompactSubmissionSelftextBinding,
            private val clickHandler: SubmissionClickHandler
    ) : SubmissionViewHolder(binding.root) {
        override fun bind(submissions: LinkData) {
            super.setSwipeData(submissions)
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { clickHandler.actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        clickHandler.actionOpenOption(id)
                        true
                    }
                    setGildings(gildingTextView, gildings)
                    scoreView.setData(ups, likes)
                    setTitle(stickied, title, titleTextView)
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }

        override fun updateVote(submissions: LinkData) {
            binding.scoreView.apply {
                when (submissions.likes) {
                    true -> upVote(submissions.ups)
                    false -> downVote(submissions.ups)
                    null -> unVote(submissions.ups)
                }
            }
        }
    }

    class LargeSubmissionImageViewHolder(
            private val binding: LargeSubmissionImageBinding,
            private val clickHandler: SubmissionClickHandler
    ) : SubmissionViewHolder(binding.root) {
        override fun bind(submissions: LinkData) {
            super.setSwipeData(submissions)
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { clickHandler.actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        clickHandler.actionOpenOption(id)
                        true
                    }
                    previewImageView.apply {
                        setOnClickListener { clickHandler.performAction(this@with) }
                        Glide.with(this)
                                .load(submissions.imageSet?.lowResUrl)
                                .thumbnail(Glide.with(previewImageView).load(thumbnail))
                                .placeholder(R.color.color_on_surface)
                                .error(R.drawable.ic_image_error_24dp)
                                .into(this)
                    }
                    setGildings(gildingTextView, gildings)
                    scoreView.setData(ups, likes)
                    setTitle(stickied, title, titleTextView)
                    subredditTextView.text = subredditName
                    setMetadata(metadataTextView, submissions)
                }
            }
        }

        override fun updateVote(submissions: LinkData) {
            binding.scoreView.apply {
                when (submissions.likes) {
                    true -> upVote(submissions.ups)
                    false -> downVote(submissions.ups)
                    null -> unVote(submissions.ups)
                }
            }
        }
    }

    class LargeSubmissionSelfTextViewHolder(
            private val binding: LargeSubmissionSelftextBinding,
            private val clickHandler: SubmissionClickHandler
    ) : SubmissionViewHolder(binding.root) {
        override fun bind(submissions: LinkData) {
            super.setSwipeData(submissions)
            binding.apply {
                with(submissions) {
                    root.setOnClickListener { clickHandler.actionOpenComments(subredditName, id) }
                    root.setOnLongClickListener {
                        clickHandler.actionOpenOption(id)
                        true
                    }
                    setGildings(gildingTextView, gildings)
                    scoreView.setData(ups, likes)
                    setTitle(stickied, title, titleTextView)
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

        override fun updateVote(submissions: LinkData) {
            binding.scoreView.apply {
                when (submissions.likes) {
                    true -> upVote(submissions.ups)
                    false -> downVote(submissions.ups)
                    null -> unVote(submissions.ups)
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

private fun setTitle(isSticky: Boolean, title: String, textView: TextView) = textView.apply {
    setTextColor(ContextCompat.getColor(context,
            if (isSticky) R.color.sticky_post_color
            else R.color.color_on_surface
    ))
    text = title
}

private fun setGildings(textView: TextView, gildings: Gildings) {
    textView.text = Truss().apply {
        if (gildings.platinum != 0) pushSpan(CenteredImageSpan(getDrawable(R.drawable.ic_platinum_gilding_14dp, textView)))
                .append(" ")
                .popSpan()
                .append("x${gildings.platinum} ")
        if (gildings.gold != 0) pushSpan(CenteredImageSpan(getDrawable(R.drawable.ic_gold_gilding_14dp, textView)))
                .append(" ")
                .popSpan()
                .append("x${gildings.gold} ")
        if (gildings.silver != 0) pushSpan(CenteredImageSpan(getDrawable(R.drawable.ic_silver_gilding_14dp, textView)))
                .append(" ")
                .popSpan()
                .append("x${gildings.silver} ")
    }.build()
}

private fun getDrawable(@DrawableRes res: Int, textView: TextView): Drawable = ContextCompat.getDrawable(textView.context, res)!!.apply {
    setBounds(0, 0, intrinsicHeight, intrinsicWidth)
}

private fun SubmissionClickHandler.performAction(linkData: LinkData) {
    with(linkData) {
        when (post_hint) {
            "image" ->
                if (imageSet != null) actionOpenImage(imageSet.lowResUrl!!, imageSet.highResUrl!!)
                else Log.e("SubmissionViewHolder", "performAction: content type image has not image attached :$this ")

            "link" -> when (val imgurResource = Uri.parse(url).getImgurMedia()) {
                is ImgurResult.Image -> actionOpenImage(imageSet?.lowResUrl!!, imgurResource.url)
                is ImgurResult.Video -> actionOpenVideo(imgurResource.url)
                ImgurResult.NonImgurResource -> actionOpenLink(url)
            }

            "rich:video", "hosted:video" -> videoUrl?.let { actionOpenVideo(it) }

            else -> Log.e("SubmissionViewHolder", "unsupported content type :$post_hint")
        }
    }
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