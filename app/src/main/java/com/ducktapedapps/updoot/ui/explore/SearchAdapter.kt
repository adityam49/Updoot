package com.ducktapedapps.updoot.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.databinding.SubredditItemBinding
import com.ducktapedapps.updoot.model.Subreddit

class SearchAdapter(private val clickHandler: ExploreFragment.ClickHandler) : ListAdapter<Subreddit, SearchAdapter.SubredditViewHolder>(CALLBACK) {
    class SubredditViewHolder(val binding: SubredditItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun submitList(list: MutableList<Subreddit>?) {
        super.submitList(list ?: mutableListOf())
    }

    private object CALLBACK : DiffUtil.ItemCallback<Subreddit>() {
        override fun areItemsTheSame(oldItem: Subreddit, newItem: Subreddit): Boolean {
            return oldItem.display_name == newItem.display_name
        }

        override fun areContentsTheSame(oldItem: Subreddit, newItem: Subreddit) = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : SubredditViewHolder = SubredditViewHolder(SubredditItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: SubredditViewHolder, position: Int) {
        holder.binding.subreddit = getItem(position)
        holder.binding.clickHandler = clickHandler
        holder.binding.executePendingBindings()
    }
}

@BindingAdapter("searchSubredditIcon")
fun bindIcon(view: ImageView, url: String?) =
        Glide.with(view)
                .load(url)
                .apply(RequestOptions.circleCropTransform())
                .into(view)
