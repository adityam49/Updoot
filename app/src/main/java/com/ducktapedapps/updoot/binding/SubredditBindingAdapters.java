package com.ducktapedapps.updoot.binding;

import android.graphics.Color;
import android.text.format.DateUtils;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.databinding.BindingAdapter;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.utils.MarkdownUtils;

public class SubredditBindingAdapters {
    @BindingAdapter("metadata")
    public static void setMetadata(TextView textView, LinkData data) {
        StringBuilder metadata = new StringBuilder().append(data.getSubredditName());
        metadata.append(" \u2022 ");
        if (data.getCommentsCount() > 999) {
            metadata
                    .append(data.getCommentsCount() / 1000)
                    .append("K replies");
        } else {
            metadata.append(data.getCommentsCount())
                    .append(" replies");
        }
        metadata.append(" \u2022 ");

        metadata.append(DateUtils.getRelativeTimeSpanString(data.getCreated() * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))
                .append(" ");
        textView.setText(metadata.toString());
    }

    @BindingAdapter({"submissionVotes", "voteColor"})
    public static void setVotes(TextView textView, int votes, Boolean likes) {
        String upVotes;
        if (votes > 999) {
            upVotes = votes / 1000 + "K";
        } else {
            upVotes = String.valueOf(votes);
        }

        if (likes == null) {
            textView.setTextColor(Color.WHITE);
        } else if (likes) {
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.upVoteColor));
        } else {
            textView.setTextColor(ContextCompat.getColor(textView.getContext(), R.color.downVoteColor));
        }

        textView.setText(upVotes);
    }

    @BindingAdapter({"toggleSelfTextVisibility", "selfText"})
    public static void toggleSelfTextVisibility(TextView selfTextView, boolean isSelfTextExpanded, String htmlSelfText) {
        if (isSelfTextExpanded) {
            MarkdownUtils.decodeAndSet(htmlSelfText, selfTextView);
        } else {
            selfTextView.setText("");
        }
    }
}
