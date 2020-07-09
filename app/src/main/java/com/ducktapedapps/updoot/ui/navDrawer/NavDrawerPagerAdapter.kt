package com.ducktapedapps.updoot.ui.navDrawer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.NavDrawerPageBinding

class NavDrawerPagerAdapter(
        private val pageOneAdapter: List<RecyclerView.Adapter<out RecyclerView.ViewHolder>>,
        private val pageTwoAdapter: List<RecyclerView.Adapter<out RecyclerView.ViewHolder>>
) : RecyclerView.Adapter<DrawerPageViewHolder>() {
    private companion object {
        const val PAGE_COUNT = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawerPageViewHolder =
            DrawerPageViewHolder(NavDrawerPageBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun getItemCount() = PAGE_COUNT

    override fun onBindViewHolder(holder: DrawerPageViewHolder, position: Int) {
        holder.bind(holder.itemView.context, if (position == 0) pageOneAdapter else pageTwoAdapter)
    }

}

class DrawerPageViewHolder(private val binding: NavDrawerPageBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(context: Context, adapters: List<RecyclerView.Adapter<out RecyclerView.ViewHolder>>) = binding.apply {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ConcatAdapter().apply {
                adapters.forEach { addAdapter(it) }
            }
        }
    }
}