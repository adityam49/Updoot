package com.ducktapedapps.updoot.ui.navDrawer

import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.databinding.ItemSubscriptionSubredditBinding
import com.ducktapedapps.updoot.utils.Truss
import com.ducktapedapps.updoot.utils.getCompactAge
import com.ducktapedapps.updoot.utils.getCompactCountAsString

class SubscriptionsAdapter(private val clickHandler: ClickHandler) : ListAdapter<Subreddit, SubredditViewHolder>(CALLBACK) {
    interface ClickHandler {
        fun goToSubreddit(subredditName: String)
    }

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<Subreddit>() {
            override fun areItemsTheSame(oldItem: Subreddit, newItem: Subreddit) = newItem.display_name == oldItem.display_name

            override fun areContentsTheSame(oldItem: Subreddit, newItem: Subreddit) =
                    newItem.subscribers == oldItem.subscribers &&
                            newItem.active_user_count == oldItem.active_user_count
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubredditViewHolder = SubredditViewHolder(
            ItemSubscriptionSubredditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: SubredditViewHolder, position: Int) {
        holder.bind(currentList[position], clickHandler)
    }
}

class SubredditViewHolder(val binding: ItemSubscriptionSubredditBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(subreddit: Subreddit, clickHandler: SubscriptionsAdapter.ClickHandler) = binding.apply {
        bindIcon(subredditIcon, subreddit.community_icon)
        subredditInfo.text = Truss()
                .append(subreddit.display_name)
                .append(" \u2022 ")
                .pushSpan(RelativeSizeSpan(0.8f))
                .append(getCompactAge(subreddit.created))
                .append("\n\n${getCompactCountAsString(subreddit.subscribers ?: 0)} subscribers")
                .build()
        root.setOnClickListener { clickHandler.goToSubreddit(subreddit.display_name) }
    }

    private fun bindIcon(view: ImageView, url: String?) =
            Glide.with(view)
                    .load(url)
                    .placeholder(ContextCompat.getDrawable(view.context, R.drawable.ic_subreddit_default_24dp)?.apply {
                        setTint(ContextCompat.getColor(view.context, R.color.color_on_surface))
                    })
                    .apply(RequestOptions.circleCropTransform())
                    .into(view)
}
