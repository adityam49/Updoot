package com.ducktapedapps.updoot.ui.explore.trending

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
import com.ducktapedapps.updoot.databinding.TrendingSubredditItemBinding
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.ui.explore.trending.HorizontalTrendingSubsAdapter.TrendingSubVh
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.getCompactDateAsString

class HorizontalTrendingSubsAdapter : ListAdapter<Subreddit, TrendingSubVh>(CALLBACK) {
    inner class TrendingSubVh(val binding: TrendingSubredditItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            TrendingSubVh(TrendingSubredditItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: TrendingSubVh, position: Int) {
        with(getItem(position)) {
            holder.binding.apply {
                bindSubredditIcon(subredditIcon, community_icon)
                bindSubredditTitle(subredditNameAge, display_name, created)
                bindSubscriberCount(subscriberCountTv, active_user_count, subscribers)
            }
        }
    }

    override fun onBindViewHolder(holder: TrendingSubVh, position: Int, payloads: MutableList<Any>) =
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
            } else {
                if (payloads.contains(SUBS_OR_ONLINE_CHANGED))
                    bindSubscriberCount(holder.binding.subscriberCountTv, getItem(position).active_user_count, getItem(position).subscribers)
                else {
                }
            }

    private fun bindSubscriberCount(view: TextView, onlineCount: Long, subscriberCount: Long) {
        view.text = if (onlineCount <= -0L || subscriberCount <= 0L) ""
        else String.format("%s Online / %s Subscribers", getCompactCountAsString(onlineCount), getCompactCountAsString(subscriberCount))
    }

    private fun bindSubredditIcon(view: ImageView, url: String?) = Glide
            .with(view)
            .load(url)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.ic_subreddit_default_24dp)
            .into(view)

    private fun bindSubredditTitle(view: TextView, subredditName: String, subredditAge: Long) {
        view.text = String.format("%s \u25CF %s", subredditName, getCompactDateAsString(subredditAge))
    }

    override fun submitList(list: List<Subreddit>?) {
        val updatedList: MutableList<Subreddit> = mutableListOf()
        if (list != null) {
            updatedList.addAll(list)
        }
        super.submitList(updatedList)
    }

    private object CALLBACK : DiffUtil.ItemCallback<Subreddit>() {
        override fun areItemsTheSame(oldItem: Subreddit, newItem: Subreddit) = oldItem.display_name == newItem.display_name

        override fun areContentsTheSame(oldItem: Subreddit, newItem: Subreddit): Boolean {
            val subscribersChanged = oldItem.subscribers == newItem.subscribers
            val onlineUsersChanged = oldItem.active_user_count == newItem.active_user_count
            return subscribersChanged && onlineUsersChanged
        }

        override fun getChangePayload(oldItem: Subreddit, newItem: Subreddit): List<Int> {
            val partialChanges: MutableList<Int> = mutableListOf()
            if (oldItem.subscribers != newItem.subscribers ||
                    oldItem.active_user_count != newItem.active_user_count)
                partialChanges.add(SUBS_OR_ONLINE_CHANGED)
            return partialChanges
        }
    }

    private companion object {
        const val SUBS_OR_ONLINE_CHANGED = 1
    }
}