package com.ducktapedapps.updoot.ui.navDrawer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ItemNavDrawerEntryBinding
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination.*

class NavDrawerDestinationAdapter(private val clickHandler: ClickHandler) : ListAdapter<NavigationDestination, NavDrawerEntryViewHolder>(CALLBACK) {
    interface ClickHandler {
        fun openDestination(destination: NavigationDestination)
    }

    private companion object {

        val CALLBACK = object : DiffUtil.ItemCallback<NavigationDestination>() {
            override fun areItemsTheSame(oldItem: NavigationDestination, newItem: NavigationDestination) =
                    oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: NavigationDestination, newItem: NavigationDestination) = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavDrawerEntryViewHolder(ItemNavDrawerEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: NavDrawerEntryViewHolder, position: Int) {
        holder.bind(getItem(position), clickHandler)
    }
}

class NavDrawerEntryViewHolder(private val binding: ItemNavDrawerEntryBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(destinationModel: NavigationDestination, clickHandler: NavDrawerDestinationAdapter.ClickHandler) = binding.apply {
        titleTextView.text = destinationModel.title
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, destinationModel.icon))
        binding.root.setOnClickListener { clickHandler.openDestination(destinationModel) }
    }
}

sealed class NavigationDestination(val title: String, @DrawableRes val icon: Int, val isUserSpecific: Boolean) {
    object Search : NavigationDestination("Search", R.drawable.ic_search_24dp, false)
    object Explore : NavigationDestination("Explore", R.drawable.ic_explore_24dp, false)
    object CreatePost : NavigationDestination("Create Post", R.drawable.ic_baseline_edit_24, true)
    object Inbox : NavigationDestination("Inbox", R.drawable.ic_baseline_inbox_24, true)
    object History : NavigationDestination("History", R.drawable.ic_baseline_history_24, false)
    object Exit : NavigationDestination("Exit", R.drawable.ic_baseline_exit_to_app_24, false)
}

val AllNavigationEntries = listOf(
        Search,
        Explore,
        CreatePost,
        Inbox,
        History,
        Exit
)