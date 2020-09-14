package com.ducktapedapps.updoot.ui.subreddit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.databinding.CompactSubmissionImageBinding
import com.ducktapedapps.updoot.databinding.CompactSubmissionSelftextBinding
import com.ducktapedapps.updoot.databinding.LargeSubmissionImageBinding
import com.ducktapedapps.updoot.databinding.LargeSubmissionSelftextBinding
import com.ducktapedapps.updoot.ui.subreddit.SubmissionViewHolder.*
import com.ducktapedapps.updoot.ui.subreddit.SubmissionsAdapter.Companion.PayLoad.SaveChange
import com.ducktapedapps.updoot.ui.subreddit.SubmissionsAdapter.Companion.PayLoad.VoteChange
import com.ducktapedapps.updoot.utils.SubmissionUiType
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT

class SubmissionsAdapter(private val clickHandler: SubmissionClickHandler) : ListAdapter<LinkData, SubmissionViewHolder>(CALLBACK) {
    interface SubmissionClickHandler {
        fun actionOpenComments(linkDataId: String, commentId: String)
        fun actionOpenOption(linkDataId: String)
        fun actionOpenImage(lowResUrl: String, highResUrl: String)
        fun actionOpenLink(link: String)
        fun actionOpenVideo(videoUrl: String)
    }

    var itemUi: SubmissionUiType = COMPACT
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (itemUi == COMPACT) {
            when (viewType) {
                SELF -> CompactSelfTextViewHolder(CompactSubmissionSelftextBinding.inflate(inflater, parent, false), clickHandler)
                else -> CompactImageViewHolder(CompactSubmissionImageBinding.inflate(inflater, parent, false), clickHandler)
            }
        } else {
            when (viewType) {
                SELF -> LargeSubmissionSelfTextViewHolder(LargeSubmissionSelftextBinding.inflate(inflater, parent, false), clickHandler)
                else -> LargeSubmissionImageViewHolder(LargeSubmissionImageBinding.inflate(inflater, parent, false), clickHandler)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        !currentList[position].selftext.isNullOrBlank() -> SELF
        else -> IMAGE
    }

    override fun submitList(list: List<LinkData>?) {
        val updatedList: MutableList<LinkData> = mutableListOf()
        if (list != null) {
            updatedList.addAll(list)
        }
        super.submitList(updatedList)
    }

    private val TAG = "SubmissionAdapter"

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) onBindViewHolder(holder, position)
        else (payloads as List<List<PayLoad>>).first().forEach {
            when (it) {
                is VoteChange -> holder.updateVote(getItem(position))
                is SaveChange -> holder.updateSaveState(getItem(position))
            }
        }
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object {
        const val IMAGE = 1
        const val LINK = 2
        const val SELF = 3
        val CALLBACK = object : DiffUtil.ItemCallback<LinkData>() {
            override fun areItemsTheSame(oldItem: LinkData, newItem: LinkData): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: LinkData, newItem: LinkData): Boolean {
                return ((oldItem.likes == null && newItem.likes == null)
                        || (oldItem.likes != null && newItem.likes != null && oldItem.likes == newItem.likes))
                        && (oldItem.saved == newItem.saved)
            }

            override fun getChangePayload(oldItem: LinkData, newItem: LinkData): Any? = mutableListOf<PayLoad>().apply {
                if (oldItem.likes != newItem.likes) add(VoteChange)
                if (oldItem.saved != newItem.saved) add(SaveChange)
            }
        }

        sealed class PayLoad {
            object VoteChange : PayLoad()
            object SaveChange : PayLoad()
        }
    }
}