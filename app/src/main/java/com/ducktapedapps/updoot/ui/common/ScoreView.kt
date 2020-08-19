package com.ducktapedapps.updoot.ui.common

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextSwitcher
import androidx.core.content.ContextCompat
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.common.VoteState.*
import com.ducktapedapps.updoot.utils.getCompactCountAsString

class ScoreView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : TextSwitcher(context, attrs) {
    private var score: Int = 0
    private var currentVoteState: VoteState = NoVote

    init {
        View.inflate(context, R.layout.view_score, this)
    }

    fun upVote(updatedScore: Int) {
        when (currentVoteState) {
            NoVote -> {
                setInAnimation(context, R.anim.slide_down_in)
                setOutAnimation(context, R.anim.slide_down_out)
                setText(updatedScore.toString())
                currentVoteState = UpVote
                setText(updatedScore.asUpVoted())
            }
            DownVote -> {
                setInAnimation(context, R.anim.slide_down_in)
                setOutAnimation(context, R.anim.slide_down_out)
                currentVoteState = UpVote
                setText(updatedScore.asUpVoted())
            }
            UpVote -> Unit
        }
    }

    fun unVote(updatedScore: Int) {
        when (currentVoteState) {
            UpVote -> {
                setInAnimation(context, R.anim.slide_up_in)
                setOutAnimation(context, R.anim.slide_up_out)
                currentVoteState = NoVote
                setText(updatedScore.asNonVoted())
            }
            DownVote -> {
                setInAnimation(context, R.anim.slide_down_in)
                setOutAnimation(context, R.anim.slide_down_out)
                currentVoteState = NoVote
                setText(updatedScore.asNonVoted())
            }
            NoVote -> Unit
        }
    }

    fun downVote(updatedScore: Int) {
        when (currentVoteState) {
            NoVote -> {
                setInAnimation(context, R.anim.slide_up_in)
                setOutAnimation(context, R.anim.slide_up_out)
                currentVoteState = DownVote
                setText(updatedScore.asDownVoted())
            }
            UpVote -> {
                setInAnimation(context, R.anim.slide_up_in)
                setOutAnimation(context, R.anim.slide_up_out)
                currentVoteState = DownVote
                setText(updatedScore.asDownVoted())
            }
            DownVote -> Unit
        }
    }

    private fun Int.asUpVoted(): CharSequence = SpannableString(getCompactCountAsString(toLong())).apply {
        setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.upVoteColor)), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun Int.asNonVoted(): CharSequence = SpannableString(getCompactCountAsString(toLong())).apply {
        setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.color_on_primary)), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun Int.asDownVoted(): CharSequence = SpannableString(getCompactCountAsString(toLong())).apply {
        setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.downVoteColor)), 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    fun setData(score: Int, likes: Boolean?) {
        setCurrentText(when (likes) {
            true -> {
                currentVoteState = UpVote
                score.asUpVoted()
            }
            false -> {
                currentVoteState = DownVote
                score.asDownVoted()
            }
            null -> {
                currentVoteState = NoVote
                score.asNonVoted()
            }
        })
    }
}

private sealed class VoteState {
    object NoVote : VoteState()
    object UpVote : VoteState()
    object DownVote : VoteState()
}