package com.ducktapedapps.updoot.ui.comments

import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ItemPostHeaderBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.comments.SubmissionMetaDataAdapter.PostHeaderViewHolder
import com.ducktapedapps.updoot.utils.RoundedBackgroundSpan
import com.ducktapedapps.updoot.utils.Truss
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import com.ducktapedapps.updoot.utils.getCompactDateAsString

class SubmissionMetaDataAdapter : Adapter<PostHeaderViewHolder>() {
    var linkData: LinkData? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHeaderViewHolder =
            PostHeaderViewHolder(ItemPostHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = if (linkData == null) 0 else 1

    override fun onBindViewHolder(holder: PostHeaderViewHolder, position: Int) {
        holder.bind(linkData!!)
    }

    class PostHeaderViewHolder(private val binding: ItemPostHeaderBinding) : ViewHolder(binding.root) {
        fun bind(model: LinkData) = binding.apply {
            submissionTitleTextView.text = model.title
            scoreView.text = getCompactCountAsString(model.ups.toLong())
            userName.text = Truss()
                    .pushSpan(RoundedBackgroundSpan(
                            ContextCompat.getColor(userName.context, R.color.color_secondary),
                            ContextCompat.getColor(userName.context, R.color.color_on_secondary)
                    ))
                    .append(model.author)
                    .popSpan()
                    .append(" ")
                    //TODO : add author flair to Linkdata
                    .build()
            metadataTextView.text = Truss()
                    .pushSpan(ImageSpan(metadataTextView.context, R.drawable.ic_subreddit_default_24dp))
                    .popSpan()
                    .append(model.subredditName)
                    .append("\u00b7")
                    .append(getCompactDateAsString(model.created))
                    .build()
        }
    }
}

