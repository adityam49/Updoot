package com.ducktapedapps.updoot.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.TrendingSubredditItemBinding
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.ui.explore.TrendingSubsAdapter.TrendingSubVh
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.getCompactDateAsString

class TrendingSubsAdapter : androidx.recyclerview.widget.ListAdapter<Subreddit, TrendingSubVh>(CALLBACK) {
    inner class TrendingSubVh(val binding: TrendingSubredditItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            TrendingSubVh(TrendingSubredditItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: TrendingSubVh, position: Int) {
        holder.binding.apply {
            subreddit = getItem(position)
            executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: TrendingSubVh, position: Int, payloads: MutableList<Any>) =
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
            } else {
                if (payloads.contains(SUBS_OR_ONLINE_CHANGED))
                    subscriberCount(holder.binding.subscriberCountTv, getItem(position).active_user_count, getItem(position).subscribers)
                else {
                }
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

@BindingAdapter("bannerUrlLoad")
fun bannerUrlLoad(view: ImageView, url: String) =
        Glide.with(view).load(url).into(view)

@BindingAdapter("onlineCount", "subscriberCount")
fun subscriberCount(view: TextView, onlineCount: Long, subscriberCount: Long) {
    view.text = if (onlineCount <= -0L || subscriberCount <= 0L) ""
    else String.format("%s Online / %s Subscribers", getCompactCountAsString(onlineCount), getCompactCountAsString(subscriberCount))
}

@BindingAdapter("subredditIconUrl")
fun subredditIconUrl(view: ImageView, url: String?) {
    Glide
            .with(view)
            .load(url)
            .apply(RequestOptions.circleCropTransform())
            .placeholder(R.drawable.ic_explore_24dp)
            .into(view)
}

@BindingAdapter("trendingSubredditMetaData")
fun trendingSubredditMetaData(view: TextView, subreddit: Subreddit) {
    view.text = String.format("%s%s", subreddit.public_description, if (subreddit.public_description.length < 30) "\n\n" else "")
}

@BindingAdapter("subredditName", "subredditAge")
fun subredditNameAge(view: TextView, subredditName: String, subredditAge: Long) {
    view.text = view.resources.getString(
            R.string.r_subreddit_name_age,
            subredditName,
            getCompactDateAsString(subredditAge)
    )
}