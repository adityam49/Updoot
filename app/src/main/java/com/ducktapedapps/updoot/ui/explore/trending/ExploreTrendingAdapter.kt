package com.ducktapedapps.updoot.ui.explore.trending

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.databinding.ExploreItemHeaderBinding
import com.ducktapedapps.updoot.databinding.ExploreItemTrendingSubsBinding
import com.ducktapedapps.updoot.ui.explore.ExploreUiModel
import com.ducktapedapps.updoot.ui.explore.HeaderUiModel

class ExploreTrendingAdapter : ListAdapter<ExploreUiModel, TrendingAdapterVH>(CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingAdapterVH =
            if (viewType == HEADER_VIEW) {
                TrendingAdapterVH.HeaderVH(ExploreItemHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            } else {
                TrendingAdapterVH.TrendingVH(ExploreItemTrendingSubsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }


    override fun onBindViewHolder(holder: TrendingAdapterVH, position: Int) {
        when (holder) {
            is TrendingAdapterVH.HeaderVH -> holder.bind(getItem(position) as HeaderUiModel)
            is TrendingAdapterVH.TrendingVH -> holder.bind(getItem(position) as TrendingUiModel)
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is HeaderUiModel -> HEADER_VIEW
        else -> TRENDING_SUBS
    }

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<ExploreUiModel>() {
            override fun areItemsTheSame(oldItem: ExploreUiModel, newItem: ExploreUiModel) =
                    if (oldItem is TrendingUiModel && newItem is TrendingUiModel) true
                    else oldItem is HeaderUiModel && newItem is HeaderUiModel

            override fun areContentsTheSame(oldItem: ExploreUiModel, newItem: ExploreUiModel): Boolean =
                    if (oldItem is TrendingUiModel && newItem is TrendingUiModel) {
                        oldItem.subs.size == newItem.subs.size && oldItem.subs.containsAll(newItem.subs)
                    } else if (oldItem is HeaderUiModel && newItem is HeaderUiModel) {
                        oldItem.title == newItem.title
                    } else false

        }

        val HEADER_VIEW = 1
        val TRENDING_SUBS = 2
    }
}

sealed class TrendingAdapterVH(view: View) : RecyclerView.ViewHolder(view) {
    class TrendingVH(private val binding: ExploreItemTrendingSubsBinding) : TrendingAdapterVH(binding.root) {
        fun bind(trendingUiModel: TrendingUiModel) = binding.trendingRecyclerView.apply {
            onFlingListener = null
            LinearSnapHelper().attachToRecyclerView(this)
            adapter = HorizontalTrendingSubsAdapter().apply { submitList(trendingUiModel.subs) }
        }
    }

    class HeaderVH(private val binding: ExploreItemHeaderBinding) : TrendingAdapterVH(binding.root) {
        fun bind(headerUiModel: HeaderUiModel) = binding.textView.apply {
            text = headerUiModel.title
            setCompoundDrawablesWithIntrinsicBounds(0, 0, headerUiModel.icon, 0)
        }
    }

}
