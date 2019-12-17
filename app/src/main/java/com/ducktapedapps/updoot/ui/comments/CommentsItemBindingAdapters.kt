package com.ducktapedapps.updoot.ui.comments

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Preview
import com.ducktapedapps.updoot.utils.MarkdownUtils
import me.kaelaela.opengraphview.OpenGraphView

@BindingAdapter("bind:childCommentCount", "bind:loadMoreCommentCount", "bind:isExpanded")

fun setChildCount(textView: TextView, childCount: Int, loadMoreCommentCount: Int, isExpanded: Boolean) {
    if (loadMoreCommentCount != 0 || childCount == 0) textView.visibility = View.GONE
    else {
        if (!isExpanded) {
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE
        }
        textView.text = textView.context.getString(R.string.childrenCommentCount, childCount)
    }
}

@BindingAdapter("bind:commentDepth", "bind:loadCommentsCount")
fun setCommentDepthMargin(view: View, depth: Int, loadCommentsCount: Int) {
    if (depth != 0) {
        val newLayoutParams = view.layoutParams as ConstraintLayout.LayoutParams
        newLayoutParams.leftMargin = 8 + depth * 16
        if (loadCommentsCount != 0) view.visibility = View.INVISIBLE
        view.layoutParams = newLayoutParams
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("bind:isAuthorOp")
fun setOPColor(authorTV: TextView, isOP: Boolean) {
    if (isOP) {
        authorTV.setBackgroundColor(Color.WHITE)
    } else {
        authorTV.setBackgroundColor(Color.TRANSPARENT)
    }
}

@BindingAdapter("richLinkPreview")
fun setRichLinkPreview(openGraphView: OpenGraphView, data: LinkData) {
    if (
            data.url.isNotEmpty()
            && data.post_hint != null
            && data.post_hint == "link"
            && data.selftext == null
    ) {
        openGraphView.loadFrom(data.url)
        openGraphView.visibility = View.VISIBLE
    } else {
        openGraphView.visibility = View.GONE
    }
}

@BindingAdapter("mediaPreview")
fun setMediaPreview(imageView: ImageView, mediaPreview: Preview?) {
    if (mediaPreview != null && !mediaPreview.images[0].source.url.isEmpty()) {
        Glide.with(imageView.context)
                .load(mediaPreview.images[0].source.url)
                .transform(FitCenter(), RoundedCorners(16))
                .into(imageView)
                .view.visibility = View.VISIBLE
    }
}

@BindingAdapter("cardViewContent")
fun setCardViewContent(cardView: CardView, data: LinkData) {
    if (data.selftext != null && data.selftext.isNotEmpty() || data.url.isNotEmpty()) {
        cardView.visibility = View.VISIBLE
    } else {
        cardView.visibility = View.GONE
    }
}

@BindingAdapter("markdownSelftext")
fun setMarkdownSelftext(selftextTextView: TextView, selfText: String?) {
    if (selfText != null && selfText.isNotEmpty()) {
        selftextTextView.visibility = View.VISIBLE
        MarkdownUtils.decodeAndSet(selfText, selftextTextView)
    } else {
        selftextTextView.visibility = View.GONE
    }
}