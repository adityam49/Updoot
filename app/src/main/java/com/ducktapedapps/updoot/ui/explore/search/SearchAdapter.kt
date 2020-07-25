package com.ducktapedapps.updoot.ui.explore.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ExploreItemHeaderBinding
import com.ducktapedapps.updoot.databinding.SubredditItemBinding
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.ui.explore.ExploreUiModel
import com.ducktapedapps.updoot.ui.explore.HeaderUiModel
import com.ducktapedapps.updoot.ui.explore.search.SearchAdapter.ResultAction
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.getCompactDateAsString

class SearchAdapter(private val resultAction: ResultAction) : ListAdapter<ExploreUiModel, VH>(CALLBACK) {

    interface ResultAction {
        fun goToSubreddit(subredditName: String)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        when (holder) {
            is VH.HeaderVH -> holder.bind(getItem(position) as HeaderUiModel)
            is VH.SubredditViewHolder -> holder.bind(getItem(position) as Subreddit, resultAction)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            if (viewType == HEADER_VIEW) {
                VH.HeaderVH(ExploreItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            } else {
                VH.SubredditViewHolder(SubredditItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }

    override fun getItemViewType(position: Int): Int =
            if (getItem(position) is HeaderUiModel) HEADER_VIEW
            else SEARCH_RESULT_ITEM

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<ExploreUiModel>() {
            override fun areItemsTheSame(oldItem: ExploreUiModel, newItem: ExploreUiModel) =
                    if (oldItem is Subreddit && newItem is Subreddit) true
                    else oldItem is HeaderUiModel && newItem is HeaderUiModel

            override fun areContentsTheSame(oldItem: ExploreUiModel, newItem: ExploreUiModel): Boolean =
                    if (oldItem is Subreddit && newItem is Subreddit) {
                        //TODO : equality check without cast gives lint error?
                        oldItem == newItem
                    } else if (oldItem is HeaderUiModel && newItem is HeaderUiModel) {
                        oldItem.title == newItem.title
                    } else false

        }

        val HEADER_VIEW = 1
        val SEARCH_RESULT_ITEM = 2
    }
}

sealed class VH(view: View) : RecyclerView.ViewHolder(view) {
    class SubredditViewHolder(val binding: SubredditItemBinding) : VH(binding.root) {
        fun bind(subreddit: Subreddit, resultAction: ResultAction) = binding.apply {
            bindSubscriberCount(subscriberCountTv, subreddit.subscribers)
            bindSubredditTitle(subredditTitleTv, subreddit.display_name, subreddit.created)
            bindIcon(subredditIcon, subreddit.community_icon)
            root.setOnClickListener { resultAction.goToSubreddit(subreddit.display_name) }
        }

        private fun bindIcon(view: ImageView, url: String?) {
            val drawable = ContextCompat.getDrawable(view.context, R.drawable.ic_subreddit_default_24dp)?.apply {
                setTint(ContextCompat.getColor(view.context, R.color.color_on_background))
            }
            Glide.with(view)
                    .load(url)
                    .placeholder(drawable)
                    .apply(RequestOptions.circleCropTransform())
                    .into(view)

        }

        private fun bindSubredditTitle(view: TextView, subredditName: String, subredditAge: Long) {
            view.text = String.format("%s \u25CF %s", subredditName, getCompactDateAsString(subredditAge))
        }

        private fun bindSubscriberCount(view: TextView, subscriberCount: Long) {
            view.text = String.format("%s subscribers", getCompactCountAsString(subscriberCount))
        }
    }

    class HeaderVH(private val binding: ExploreItemHeaderBinding) : VH(binding.root) {
        fun bind(headerUiModel: HeaderUiModel) = binding.textView.apply {
            text = headerUiModel.title
            setCompoundDrawablesWithIntrinsicBounds(0, 0, headerUiModel.icon, 0)
        }
    }
}
