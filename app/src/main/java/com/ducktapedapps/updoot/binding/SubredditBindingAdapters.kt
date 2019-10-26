package com.ducktapedapps.updoot.binding

import android.graphics.Color
import android.text.format.DateUtils
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter

import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.utils.MarkdownUtils

object SubredditBindingAdapters {
    @JvmStatic
    @BindingAdapter("metadata")
    fun setMetadata(textView: TextView, data: LinkData) {
        val metadata = StringBuilder().append(data.subredditName)
        metadata.append(" \u2022 ")
        if (data.commentsCount > 999) {
            metadata
                    .append(data.commentsCount / 1000)
                    .append("K replies")
        } else {
            metadata.append(data.commentsCount)
                    .append(" replies")
        }
        metadata.append(" \u2022 ")

        metadata.append(DateUtils.getRelativeTimeSpanString(data.created * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))
                .append(" ")
        textView.text = metadata.toString()
    }

    @JvmStatic
    @BindingAdapter("submissionVotes", "voteColor")
    fun setVotes(textView: TextView, votes: Int, likes: Boolean?) {
        val upVotes: String = if (votes > 999) {
            (votes / 1000).toString() + "K"
        } else {
            votes.toString()
        }

        when {
            likes == null -> textView.setTextColor(Color.WHITE)
            likes -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.upVoteColor))
            else -> textView.setTextColor(ContextCompat.getColor(textView.context, R.color.downVoteColor))
        }

        textView.text = upVotes
    }

    @JvmStatic
    @BindingAdapter("toggleSelfTextVisibility", "selfText")
    fun toggleSelfTextVisibility(selfTextView: TextView, isSelfTextExpanded: Boolean, htmlSelfText: String?) {
        if (isSelfTextExpanded) {
            MarkdownUtils.decodeAndSet(htmlSelfText ?: "", selfTextView)
        } else {
            selfTextView.text = ""
        }
    }
}
