package com.ducktapedapps.updoot.ui.subreddit

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.model.Subreddit

@BindingAdapter("subredditMetadata")
fun setMetadata(textView: TextView, subreddit: Subreddit?) {
    if (subreddit != null)
        textView.text = StringBuilder()
                .append(subreddit.public_description)
                .append("\n\n")
                .append(subreddit.subscribers)
                .append(" subscribers")
}

@BindingAdapter("imageUrl")
fun setImageFromUrl(imageView: ImageView, url: String?) =
        Glide.with(imageView.context)
                .load(url)
                .placeholder(R.drawable.ic_image_error_24dp)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)
