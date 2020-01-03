package com.ducktapedapps.updoot.ui.explore

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R

@BindingAdapter("subredditThumbnail")
fun iconThumbnail(thumbnailImageView: ImageView, thumbnail: String?) {
    Glide.with(thumbnailImageView.context)
            .load(thumbnail ?: "")
            .apply(RequestOptions.circleCropTransform())
            .error(R.drawable.ic_image_error)
            .into(thumbnailImageView)
}

@BindingAdapter("subscriberCount")
fun subscriberCount(textView: TextView, count: Long) {
    val text = "${when {
        count < 1000 -> count.toString()
        count > 1_000_000 -> "${count / 1_000_000}M"
        else -> "${count / 1000}K"
    }} Subscribers"

    textView.text = text
}
