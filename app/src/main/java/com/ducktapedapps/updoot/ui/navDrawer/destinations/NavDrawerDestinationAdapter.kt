package com.ducktapedapps.updoot.ui.navDrawer.destinations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.ItemNavDrawerEntryBinding

class NavDrawerDestinationAdapter(private val clickHandler: ClickHandler) : ListAdapter<NavDrawerItemModel, NavDrawerEntryViewHolder>(CALLBACK) {
    interface ClickHandler {
        fun openExplore()
    }

    private companion object {

        val CALLBACK = object : DiffUtil.ItemCallback<NavDrawerItemModel>() {
            override fun areItemsTheSame(oldItem: NavDrawerItemModel, newItem: NavDrawerItemModel) = oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: NavDrawerItemModel, newItem: NavDrawerItemModel) = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavDrawerEntryViewHolder(ItemNavDrawerEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: NavDrawerEntryViewHolder, position: Int) {
        holder.bind(getItem(position), clickHandler)
    }
}

class NavDrawerEntryViewHolder(private val binding: ItemNavDrawerEntryBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(model: NavDrawerItemModel, clickHandler: NavDrawerDestinationAdapter.ClickHandler) = binding.apply {
        titleTextView.text = model.title
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, model.icon))
        if (model.title == "Explore") binding.root.setOnClickListener { clickHandler.openExplore() }
    }
}