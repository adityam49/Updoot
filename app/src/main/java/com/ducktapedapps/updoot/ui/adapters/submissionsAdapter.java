package com.ducktapedapps.updoot.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.utils.MarkdownUtils;
import com.ducktapedapps.updoot.utils.constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class submissionsAdapter extends ListAdapter<LinkData, submissionsAdapter.submissionHolder> {
    private static final String TAG = "submissionsAdapter";
    private Context mContext;
    private static DiffUtil.ItemCallback<LinkData> CALLBACK = new DiffUtil.ItemCallback<LinkData>() {
        @Override
        public boolean areItemsTheSame(@NonNull LinkData oldItem, @NonNull LinkData newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull LinkData oldItem, @NonNull LinkData newItem) {
            return oldItem.getUps() == newItem.getUps();
        }

        @Override
        public Object getChangePayload(@NonNull LinkData oldItem, @NonNull LinkData newItem) {
            Bundle diffBundle = new Bundle();
            if (oldItem.getUps() != newItem.getUps()) {
                diffBundle.putInt(constants.DIFF_VOTE_KEY, newItem.getUps());
            }
            if (diffBundle.isEmpty()) return super.getChangePayload(oldItem, newItem);
            else return diffBundle;
        }
    };
    private OnItemClickListener mListener;

    public submissionsAdapter(Context context) {
        super(CALLBACK);
        mContext = context;
    }

    // DiffUtils doesn't calculate diff if passed with same list object
    // https://stackoverflow.com/questions/49726385/listadapter-not-updating-item-in-reyclerview/50062174#50062174
    @Override
    public void submitList(List<LinkData> list) {
        List<LinkData> updatedList = new ArrayList<>();
        if (list != null) updatedList.addAll(list);
        super.submitList(updatedList);
    }

    @NonNull
    @Override
    public submissionsAdapter.submissionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.linear_submissions_item, parent, false);
        return new submissionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull submissionHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Bundle diff = (Bundle) payloads.get(0);
            int newUps = diff.getInt(constants.DIFF_VOTE_KEY);
            if (newUps > 999) {
                holder.scoreTv.setText(mContext.getResources().getString(R.string.thousandSuffix, newUps / 1000));
            } else {
                holder.scoreTv.setText(String.valueOf(newUps));
            }
            if (getItem(position).getLikes() == null) {
                holder.scoreTv.setTextColor(Color.WHITE);
            } else if (getItem(position).getLikes()) {
                holder.scoreTv.setTextColor(ContextCompat.getColor(mContext, R.color.upVoteColor));
            } else {
                holder.scoreTv.setTextColor(ContextCompat.getColor(mContext, R.color.downVoteColor));
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull submissionsAdapter.submissionHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mListener = onItemClickListener;
    }


    public interface OnItemClickListener {
        void onItemClick(LinkData data);
    }

    class submissionHolder extends RecyclerView.ViewHolder {

        boolean selfTextExpanded = false;

        @BindView(R.id.silverGildingsView)
        TextView silverGildingsView;
        @BindView(R.id.goldGildingsView)
        TextView goldGildingsView;
        @BindView(R.id.platinumGildingsView)
        TextView platinumGildingsView;
        @BindView(R.id.title_tv)
        TextView titleTv;
        @BindView(R.id.score_tv)
        TextView scoreTv;
        @BindView(R.id.mediaPreview)
        ImageView thumbnail;
        @BindView(R.id.metadata)
        TextView metadata_tv;
        @BindView(R.id.revel_more_view)
        FrameLayout revealMore;
        @BindView(R.id.selftext_cardView)
        CardView selfText_cardView;
        @BindView(R.id.selfText_tv)
        TextView selfText_Tv;

        @OnClick(R.id.submissionsView)
        void OnClick() {
            mListener.onItemClick(getItem(getAdapterPosition()));
        }

        @OnClick(R.id.revel_more_view)
        void onClick() {
            if (!selfTextExpanded) {
                MarkdownUtils.decodeAndSet(getItem(getAdapterPosition()).getSelftext(), selfText_Tv);
                selfText_cardView.setVisibility(VISIBLE);
                selfText_Tv.setVisibility(VISIBLE);
                selfTextExpanded = true;
            } else {
                selfText_cardView.setVisibility(GONE);
                selfTextExpanded = false;
            }
        }

        submissionHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(LinkData data) {
            if (data.getThumbnail() != null) {
                switch (data.getThumbnail()) {
                    case "self":
                        thumbnail.setImageResource(R.drawable.ic_selftext);
                        break;
                    case "default": //links
                        thumbnail.setImageResource(R.drawable.ic_link);
                        break;
                    default:
                        Glide
                                .with(mContext)
                                .load(data.getThumbnail())
                                .apply(RequestOptions.circleCropTransform())
                                .error(R.drawable.ic_image_error)
                                .into(thumbnail);
                }
            } else {
                thumbnail.setImageResource(R.drawable.ic_image_error);
            }
            if (data.getUps() > 999) {
                scoreTv.setText(mContext.getResources().getString(R.string.thousandSuffix, data.getUps() / 1000));
            } else {
                scoreTv.setText(String.valueOf(data.getUps()));
            }

            if (data.getLikes() == null) {
                scoreTv.setTextColor(Color.WHITE);
            } else if (data.getLikes()) {
                scoreTv.setTextColor(ContextCompat.getColor(mContext, R.color.upVoteColor));
            } else {
                scoreTv.setTextColor(ContextCompat.getColor(mContext, R.color.downVoteColor));
            }

            titleTv.setText(data.getTitle());
            StringBuilder metadata = new StringBuilder().append(data.getSubreddit_name_prefixed());
            metadata.append(" \u2022 ");
            if (data.getCommentsCount() > 999) {
                metadata.append(mContext.getString(R.string.commentsAbbreviation, mContext.getString(R.string.thousandSuffix, data.getCommentsCount() / 1000)));
            } else {
                metadata.append(mContext.getString(R.string.commentsAbbreviation, String.valueOf(data.getCommentsCount())));
            }
            metadata.append(" \u2022 ");

            metadata.append(DateUtils.getRelativeTimeSpanString(data.getCreated() * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)).append(" ");

            metadata_tv.setText(metadata.toString());
            if (data.getGildings().getSilver() == 0) {
                silverGildingsView.setVisibility(GONE);
            } else {
                silverGildingsView.setVisibility(VISIBLE);
                silverGildingsView.setText(String.valueOf(data.getGildings().getSilver()));
            }
            if (data.getGildings().getGold() == 0) {
                goldGildingsView.setVisibility(GONE);
            } else {
                goldGildingsView.setVisibility(VISIBLE);
                goldGildingsView.setText(String.valueOf(data.getGildings().getGold()));
            }
            if (data.getGildings().getPlatinum() == 0) {
                platinumGildingsView.setVisibility(GONE);
            } else {
                platinumGildingsView.setVisibility(VISIBLE);
                platinumGildingsView.setText(String.valueOf(data.getGildings().getPlatinum()));
            }

            selfTextExpanded = false;
            if (data.getSelftext() != null && !data.getSelftext().equals("")) {
                revealMore.setVisibility(VISIBLE);
                selfText_cardView.setVisibility(GONE);
            } else {
                selfText_cardView.setVisibility(GONE);
                revealMore.setVisibility(GONE);
            }
        }
    }
}

