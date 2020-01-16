package com.ducktapedapps.updoot.ui.subreddit

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.LargeSubmissionsItemBinding
import com.ducktapedapps.updoot.model.LinkData

class SubmissionsAdapter(
        private val clickHandler: SubredditFragment.ClickHandler
) : ListAdapter<LinkData, SubmissionsAdapter.SubmissionHolder>(CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionHolder {
        return SubmissionHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.large_submissions_item,
                        parent,
                        false
                )
        )
    }

    override fun submitList(list: List<LinkData>?) {
        val updatedList: MutableList<LinkData> = mutableListOf()
        if (list != null) {
            updatedList.addAll(list)
        }
        super.submitList(updatedList)
    }

    override fun onBindViewHolder(holder: SubmissionHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        if (payloads.isNotEmpty()) {
            if (payloads.contains(PartialChanges.VOTE_CHANGE)) {
                setVotes(holder.binding.scoreTv, getItem(position).ups, getItem(position).likes)
            }
            if (payloads.contains(PartialChanges.SAVE_STATE_CHANGE)) {
                //TODO : add saved state ui indication
                Log.i("submissionsAdapter", "save state change ")
            }
        }
    }

    override fun onBindViewHolder(holder: SubmissionHolder, position: Int) {
        holder.binding.linkdata = getItem(position)
        holder.binding.itemIndex = position
        holder.binding.clickHandler = clickHandler
        holder.binding.executePendingBindings()
    }

    class SubmissionHolder(val binding: LargeSubmissionsItemBinding) : RecyclerView.ViewHolder(binding.root)

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
