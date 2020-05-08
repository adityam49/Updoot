package com.ducktapedapps.updoot.ui.comments

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Preview
import com.ducktapedapps.updoot.utils.MarkdownUtils
import me.kaelaela.opengraphview.OpenGraphView


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

@BindingAdapter("moreCommentCount")
fun setMoreCommentCount(view: TextView, count: Int) {
    val stringBuilder = StringBuilder()
            .append("Load more ")
            .append(count)
            .append(if (count == 1) " comment" else " comments")
    view.text = stringBuilder.toString()
}

