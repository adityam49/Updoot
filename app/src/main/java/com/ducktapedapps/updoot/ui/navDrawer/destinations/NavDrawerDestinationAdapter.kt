package com.ducktapedapps.updoot.ui.navDrawer.destinations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.ItemNavDrawerEntryBinding

class NavDrawerDestinationAdapter : ListAdapter<NavDrawerItemModel, NavDrawerEntryViewHolder>(CALLBACK) {
    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<NavDrawerItemModel>() {
            override fun areItemsTheSame(oldItem: NavDrawerItemModel, newItem: NavDrawerItemModel) = oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: NavDrawerItemModel, newItem: NavDrawerItemModel) = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavDrawerEntryViewHolder(ItemNavDrawerEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: NavDrawerEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class NavDrawerEntryViewHolder(private val binding: ItemNavDrawerEntryBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(model: NavDrawerItemModel) = binding.apply {
        titleTextView.text = model.title
        imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, model.icon))
    }
}