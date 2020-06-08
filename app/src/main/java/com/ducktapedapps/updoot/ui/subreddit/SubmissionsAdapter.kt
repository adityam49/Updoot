package com.ducktapedapps.updoot.ui.subreddit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.CompactSubmissionImageBinding
import com.ducktapedapps.updoot.databinding.CompactSubmissionSelftextBinding
import com.ducktapedapps.updoot.databinding.LargeSubmissionImageBinding
import com.ducktapedapps.updoot.databinding.LargeSubmissionSelftextBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.subreddit.SubmissionViewHolder.*
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT

class SubmissionsAdapter(private val actionOpenComments: (String, String) -> Unit) : ListAdapter<LinkData, SubmissionViewHolder>(CALLBACK) {
    lateinit var itemUi: SubmissionUiType
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (itemUi == COMPACT) {
            if (viewType == R.layout.compact_submission_selftext) {
                CompactSelfTextViewHolder(CompactSubmissionSelftextBinding.inflate(inflater, parent, false))
            } else {
                CompactImageViewHolder(CompactSubmissionImageBinding.inflate(inflater, parent, false))
            }
        } else {
            if (viewType == R.layout.large_submission_image) {
                LargeSubmissionImageViewHolder(LargeSubmissionImageBinding.inflate(inflater, parent, false))
            } else {
                LargeSubmissionSelfTextViewHolder(LargeSubmissionSelftextBinding.inflate(inflater, parent, false))
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

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(getItem(position), actionOpenComments)
    }

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<LinkData>() {
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
                val partialChanges: MutableList<Int> = mutableListOf()
                //checking for vote change
                if ((oldItem.likes == null && newItem.likes != null)
                        || (oldItem.likes != null && newItem.likes == null)
                        || (oldItem.likes != newItem.likes)) {
                    partialChanges += VOTE_CHANGE
                }

                //checking for submission save state change
//            if (oldItem.saved != newItem.saved) {
//                partialChanges += SAVE_STATE_CHANGE
//            }
                return partialChanges
            }
        }
        const val VOTE_CHANGE = 1
        const val SAVE_STATE_CHANGE = 2
    }
}