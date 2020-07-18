package com.ducktapedapps.updoot.ui.subreddit

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ViewSideBinding
import com.ducktapedapps.updoot.model.Subreddit
import io.noties.markwon.Markwon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewSideBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    val binding = ViewSideBinding.inflate(LayoutInflater.from(context), this)

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.color_surface))
    }

    fun loadSubredditIconAndTitle(subreddit: Subreddit) {
        binding.apply {
            val placeHolder = ContextCompat.getDrawable(context, R.drawable.ic_subreddit_default_24dp)?.apply {
                setTint(ContextCompat.getColor(context, R.color.color_on_surface))
            }
            Glide.with(context)
                    .load(subreddit.community_icon)
                    .placeholder(placeHolder)
                    .error(placeHolder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(subredditIcon)
            subredditTitle.text = subreddit.display_name
        }
    }

    fun setSideBarContent(viewLifecycleOwner: LifecycleOwner, subreddit: LiveData<Subreddit?>, markwon: Markwon) {
        subreddit.observe(viewLifecycleOwner) {
            it?.let {
                binding.apply {
                    if (subredditSideBar.text.isNullOrEmpty()) {
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                            val spannedText = markwon.toMarkdown(it.description.replace("(#+)".toRegex(), "$1 "))
                            withContext(Dispatchers.Main) {
                                subredditSideBar.apply {
                                    text = spannedText
                                    movementMethod = LinkMovementMethod.getInstance()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}