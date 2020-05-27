package com.ducktapedapps.updoot.ui.navDrawer

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.NavDrawerItemBinding

class NavAdapter : androidx.recyclerview.widget.ListAdapter<NavDrawerItemModel, NavAdapter.NavDrawerItemViewHolder>(CALLBACK) {
    class NavDrawerItemViewHolder(val binding: NavDrawerItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NavDrawerItemModel) = binding.apply {
            textView.text = item.title
            imageView.setImageDrawable(ContextCompat.getDrawable(binding.root.context, item.icon))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavDrawerItemViewHolder(NavDrawerItemBinding.inflate(LayoutInflater.from(parent.context)))

    override fun onBindViewHolder(holder: NavDrawerItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object CALLBACK : DiffUtil.ItemCallback<NavDrawerItemModel>() {
        override fun areItemsTheSame(oldItem: NavDrawerItemModel, newItem: NavDrawerItemModel) = oldItem.title == newItem.title

        override fun areContentsTheSame(oldItem: NavDrawerItemModel, newItem: NavDrawerItemModel) = true
    }
}