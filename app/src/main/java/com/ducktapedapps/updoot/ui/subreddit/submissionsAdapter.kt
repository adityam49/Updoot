package com.ducktapedapps.updoot.ui.subreddit

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.CompactSubmissionImageBinding
import com.ducktapedapps.updoot.databinding.CompactSubmissionSelftextBinding
import com.ducktapedapps.updoot.databinding.LargeSubmissionImageBinding
import com.ducktapedapps.updoot.databinding.LargeSubmissionSelftextBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.subreddit.SubmissionVH.*
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT

class SubmissionsAdapter(
        private val clickHandlerGiven: SubredditFragment.ClickHandler
) : ListAdapter<LinkData, SubmissionVH>(CALLBACK) {

    var itemUi: SubmissionUiType = COMPACT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionVH {
        val inflater = LayoutInflater.from(parent.context)
        return if (itemUi == COMPACT) {
            if (viewType == R.layout.compact_submission_selftext) {
                CompactSelfTextVH(CompactSubmissionSelftextBinding.inflate(inflater, parent, false))
            } else {
                CompactImageVH(CompactSubmissionImageBinding.inflate(inflater, parent, false))
            }
        } else {
            if (viewType == R.layout.large_submission_image) {
                LargeSubmissionImageVH(LargeSubmissionImageBinding.inflate(inflater, parent, false))
            } else {
                LargeSubmissionSelfTextVH(LargeSubmissionSelftextBinding.inflate(inflater, parent, false))
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (itemUi == COMPACT) {
            if (!getItem(position).selftext.isNullOrEmpty()) R.layout.compact_submission_selftext
            else R.layout.compact_submission_image
        } else {
            if (!getItem(position).selftext.isNullOrEmpty()) R.layout.large_submission_selftext
            else R.layout.large_submission_image
        }
    }

    override fun submitList(list: List<LinkData>?) {
        val updatedList: MutableList<LinkData> = mutableListOf()
        if (list != null) {
            updatedList.addAll(list)
        }
        super.submitList(updatedList)
    }

    override fun onBindViewHolder(holder: SubmissionVH, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isNotEmpty()) {
            if (payloads.contains(PartialChanges.VOTE_CHANGE)) {
                if (holder is LargeSubmissionSelfTextVH)
                    setVotes(holder.binding.scoreTv, getItem(position).ups, getItem(position).likes)
                if (holder is LargeSubmissionImageVH)
                    setVotes(holder.binding.scoreTv, getItem(position).ups, getItem(position).likes)
                if (holder is CompactSelfTextVH)
                    setVotes(holder.binding.scoreTv, getItem(position).ups, getItem(position).likes)
                if (holder is CompactImageVH)
                    setVotes(holder.binding.scoreTv, getItem(position).ups, getItem(position).likes)
            }
            if (payloads.contains(PartialChanges.SAVE_STATE_CHANGE)) {
                //TODO : add saved state ui indication
                Log.i("submissionsAdapter", "save state change ")
            }
        }
    }


    override fun onBindViewHolder(holder: SubmissionVH, position: Int) {
        when (holder) {
            is LargeSubmissionSelfTextVH -> holder.binding.apply {
                linkdata = getItem(position)
                clickHandler = clickHandlerGiven
                executePendingBindings()
            }
            is LargeSubmissionImageVH -> holder.binding.apply {
                linkdata = getItem(position)
                clickHandler = clickHandlerGiven
                executePendingBindings()
            }

            is CompactImageVH -> holder.binding.apply {
                linkdata = getItem(position)
                clickHandler = clickHandlerGiven
                executePendingBindings()
            }

            is CompactSelfTextVH -> holder.binding.apply {
                linkdata = getItem(position)
                itemIndex = position
                clickHandler = clickHandlerGiven
                executePendingBindings()
            }
        }

    }

    private object CALLBACK : DiffUtil.ItemCallback<LinkData>() {
        override fun areItemsTheSame(oldItem: LinkData, newItem: LinkData): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: LinkData, newItem: LinkData): Boolean {
            val voteChanged = ((oldItem.likes == null && newItem.likes == null)
                    || (oldItem.likes != null && newItem.likes != null && oldItem.likes == newItem.likes))
            return if (oldItem.selftext != null) {
                (oldItem.isSelfTextExpanded == newItem.isSelfTextExpanded
                        && voteChanged)
            } else
                voteChanged
        }

        override fun getChangePayload(oldItem: LinkData, newItem: LinkData): Any? {
            val partialChanges: MutableList<PartialChanges> = mutableListOf()
            //checking for vote change
            if ((oldItem.likes == null && newItem.likes != null)
                    || (oldItem.likes != null && newItem.likes == null)
                    || (oldItem.likes != newItem.likes)) {
                partialChanges += PartialChanges.VOTE_CHANGE
            }

            //checking for submission save state change
//            if (oldItem.saved != newItem.saved) {
//                partialChanges += PartialChanges.SAVE_STATE_CHANGE
//            }
            Log.i("submissionsAdapter", "changes found : ${partialChanges.size}")
            return partialChanges
        }
    }

    enum class PartialChanges {
        VOTE_CHANGE,
        SAVE_STATE_CHANGE
    }


}

sealed class SubmissionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class CompactImageVH(val binding: CompactSubmissionImageBinding) : SubmissionVH(binding.root)

    class CompactSelfTextVH(val binding: CompactSubmissionSelftextBinding) : SubmissionVH(binding.root)

    class LargeSubmissionImageVH(val binding: LargeSubmissionImageBinding) : SubmissionVH(binding.root)

    class LargeSubmissionSelfTextVH(val binding: LargeSubmissionSelftextBinding) : SubmissionVH(binding.root)
}