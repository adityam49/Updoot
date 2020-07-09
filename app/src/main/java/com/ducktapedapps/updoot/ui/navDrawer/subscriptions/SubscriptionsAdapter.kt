package com.ducktapedapps.updoot.ui.navDrawer.subscriptions

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.SubredditItemBinding
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.getCompactDateAsString

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
            SubredditItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: SubredditViewHolder, position: Int) {
        holder.bind(currentList[position], clickHandler)
    }
}

class SubredditViewHolder(val binding: SubredditItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(subreddit: Subreddit, clickHandler: SubscriptionsAdapter.ClickHandler) = binding.apply {
        bindSubscriberCount(subscriberCountTv, subreddit.subscribers)
        bindSubredditTitle(subredditTitleTv, subreddit.display_name, subreddit.created)
        bindIcon(subredditIcon, subreddit.community_icon)
        root.setOnClickListener { clickHandler.goToSubreddit(subreddit.display_name) }
    }

    private fun bindIcon(view: ImageView, url: String?) =
            Glide.with(view)
                    .load(url)
                    .placeholder(R.drawable.ic_subreddit_default_24dp)
                    .apply(RequestOptions.circleCropTransform())
                    .into(view)

    private fun bindSubredditTitle(view: TextView, subredditName: String, subredditAge: Long) {
        view.text = String.format("%s \u25CF %s", subredditName, getCompactDateAsString(subredditAge))
    }

    private fun bindSubscriberCount(view: TextView, subscriberCount: Long) {
        view.text = String.format("%s subscribers", getCompactCountAsString(subscriberCount))
    }
}
