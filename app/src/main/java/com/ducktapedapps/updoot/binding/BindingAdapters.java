package com.ducktapedapps.updoot.binding;

import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.model.Preview;
import com.ducktapedapps.updoot.utils.MarkdownUtils;

import me.kaelaela.opengraphview.OpenGraphView;

public class BindingAdapters {

    @BindingAdapter("submissionThumbnailSource")
    public static void setThumbnail(ImageView thumbnailImageView, String thumbnail) {
        if (thumbnail != null) {
            switch (thumbnail) {
                case "self":
                case "":
                    thumbnailImageView.setImageResource(R.drawable.ic_selftext);
                    break;
                case "default": //links
                    thumbnailImageView.setImageResource(R.drawable.ic_link);
                    break;

                default:
                    Glide.with(thumbnailImageView.getContext())
                            .load(thumbnail)
                            .apply(RequestOptions.circleCropTransform())
                            .error(R.drawable.ic_image_error)
                            .into(thumbnailImageView);
            }
        } else {
            thumbnailImageView.setImageResource(R.drawable.ic_selftext);
        }
    }

    @BindingAdapter("mediaPreview")
    public static void setMediaPreview(ImageView imageView, Preview mediaPreview) {
        if (mediaPreview != null && !mediaPreview.toString().isEmpty()) {
            Glide.with(imageView.getContext())
                    .load(mediaPreview.toString())
                    .transform(new FitCenter(), new RoundedCorners(16))
                    .into(imageView)
                    .getView()
                    .setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter("submissionMetadata")
    public static void setSubmissionMetadata(TextView textView, LinkData linkData) {
        String upVotes;
        if (linkData.getUps() > 999) {
            upVotes = linkData.getUps() / 1000 + "K";
        } else {
            upVotes = String.valueOf(linkData.getUps());
        }
        String metadata =
                "By " + linkData.getAuthor() + " in " + linkData.getSubreddit_name_prefixed()
                        + "\n"
                        + upVotes + " \u2191 " + DateUtils.getRelativeTimeSpanString(linkData.getCreated() * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS);
        textView.setText(metadata);
    }

    @BindingAdapter("gildingsCount")
    public static void setGildingsCount(TextView textView, int gildingCount) {
        if (gildingCount != 0) {
            textView.setText(String.valueOf(gildingCount));
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("cardViewContent")
    public static void setCardViewContent(CardView cardView, LinkData data) {
        if ((data.getSelftext() != null && !data.getSelftext().isEmpty() || (data.getUrl() != null && !data.getUrl().isEmpty()))) {
            cardView.setVisibility(View.VISIBLE);
        } else {
            cardView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("markdownSelftext")
    public static void setMarkdownSelftext(TextView selftextTextView, String selfText) {
        if (selfText != null && !selfText.isEmpty()) {
            selftextTextView.setVisibility(View.VISIBLE);
            MarkdownUtils.decodeAndSet(selfText, selftextTextView);
        } else {
            selftextTextView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("richLinkPreview")
    public static void setRichLinkPreview(OpenGraphView openGraphView, LinkData data) {
        if (data.getUrl() != null && !data.getUrl().isEmpty() && data.getPost_hint() != null && data.getPost_hint().equals("link") && data.getSelftext() == null) {
            openGraphView.loadFrom(data.getUrl());
            openGraphView.setVisibility(View.VISIBLE);
        } else {
            openGraphView.setVisibility(View.GONE);
        }
    }
}
