package com.ducktapedapps.updoot.ui.subreddit

import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Preview
import com.ducktapedapps.updoot.utils.MarkdownUtils
import com.ducktapedapps.updoot.utils.getCompactCountAsString

@BindingAdapter("metadata")

fun setMetadata(textView: TextView, data: LinkData) {
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


@BindingAdapter("submissionVotes", "voteColor")
fun setVotes(textView: TextView, votes: Int, likes: Boolean?) {
    when {
        likes == null -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.color_on_background))
        likes -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.upVoteColor))
        else -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.downVoteColor))
    }

    textView.text = getCompactCountAsString(votes.toLong())
}

@BindingAdapter("ellipsizeSelfText")
fun showEllipsizedSelfText(textView: TextView, htmlSelfText: String?) = MarkdownUtils.decodeAndSet(htmlSelfText
        ?: "", textView)


@BindingAdapter("selfTextVisibility", "selfText")
fun toggleSelfTextVisibility(selfTextView: TextView, isSelfTextExpanded: Boolean, htmlSelfText: String?) {
    if (isSelfTextExpanded) {
        MarkdownUtils.decodeAndSet(htmlSelfText ?: "", selfTextView)
    } else {
        selfTextView.text = ""
    }
}

@BindingAdapter("submissionThumbnailSource")
fun setThumbnail(thumbnailImageView: ImageView, thumbnail: String?) {
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

@BindingAdapter("imageBind", "thumbnailBind")
fun setPreview(view: ImageView, image: Preview?, thumbnail: String) {
    if (image != null) {
        val lowResImage = image.images[0].source
        Glide.with(view.context)
                .load(lowResImage.url)
                .override(view.width, view.width * (lowResImage.height / lowResImage.width))
                .placeholder(R.color.color_background)
                .thumbnail(Glide.with(view).load(thumbnail))
                .fitCenter()
                .into(view)
        view.visibility = View.VISIBLE
    } else view.visibility = View.GONE
}

@BindingAdapter("submissionMetadata")
fun setSubmissionMetadata(textView: TextView, linkData: LinkData) {
    val upVotes: String = if (linkData.ups > 999) {
        (linkData.ups / 1000).toString() + "K"
    } else {
        linkData.ups.toString()
    }
    val metadata = ("By " + linkData.author + " in " + linkData.subredditName
            + "\n"
            + upVotes + " \u2191 " + DateUtils.getRelativeTimeSpanString(linkData.created * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))
    textView.text = metadata
}

@BindingAdapter("gildingCount")
fun setGildingCount(textView: TextView, gildingCount: Int) {
    if (gildingCount != 0) {
        textView.text = gildingCount.toString()
        textView.visibility = View.VISIBLE
    } else {
        textView.visibility = View.GONE
    }
}

