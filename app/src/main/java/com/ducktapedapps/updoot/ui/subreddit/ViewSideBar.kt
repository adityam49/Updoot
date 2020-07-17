package com.ducktapedapps.updoot.ui.subreddit

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ViewSideBinding
import com.ducktapedapps.updoot.model.Subreddit
import io.noties.markwon.Markwon

class ViewSideBar @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    val binding = ViewSideBinding.inflate(LayoutInflater.from(context), this)

    init {
        setBackgroundColor(ContextCompat.getColor(context, R.color.color_surface))
    }

    fun setSideBarContent(context: Context, viewLifecycleOwner: LifecycleOwner, subreddit: LiveData<Subreddit?>, markwon: Markwon) {
        subreddit.observe(viewLifecycleOwner) {
            it?.let {
                binding.apply {
                    val placeHolder = ContextCompat.getDrawable(context, R.drawable.ic_subreddit_default_24dp)?.apply {
                        setTint(ContextCompat.getColor(context, R.color.color_on_surface))
                    }

                    Glide.with(context)
                            .load(it.community_icon)
                            .placeholder(placeHolder)
                            .error(placeHolder)
                            .apply(RequestOptions.circleCropTransform())
                            .into(subredditIcon)
                    subredditTitle.apply {
                        text = markwon.toMarkdown(it.description.replace("(#+)".toRegex(), "$1 "))
                        movementMethod = ScrollingMovementMethod()
                    }
                }
            }
        }

    }
}