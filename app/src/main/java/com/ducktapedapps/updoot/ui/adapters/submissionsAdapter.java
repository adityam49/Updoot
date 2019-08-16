package com.ducktapedapps.updoot.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.utils.constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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

    // DiffUtils don't calculate diff if passed with same list object
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.linear_submissions, parent, false);
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

        void onSubredditClick(LinkData data);
    }

    class submissionHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.silverGildingsView)
        Button silverGildingsView;
        @BindView(R.id.goldGildingsView)
        Button goldGildingsView;
        @BindView(R.id.platinumGildingsView)
        Button platinumGildingsView;
        @BindView(R.id.commentCountButton)
        Button commentsCountButton;
        @BindView(R.id.uploadTimeView)
        Button uploadTimeView;
        @BindView(R.id.title_tv)
        TextView titleTv;
        @BindView(R.id.score_tv)
        TextView scoreTv;
        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.subredditTv)
        TextView subredditTv;

        @OnClick(R.id.submissionsView)
        void OnClick() {
            mListener.onItemClick(getItem(getAdapterPosition()));
        }

        @OnClick(R.id.subredditTv)
        void OnSubredditClick() {
            mListener.onSubredditClick(getItem(getAdapterPosition()));
        }

        submissionHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(LinkData data) {
            switch (data.getThumbnail()) {
                case "":
                case "nsfw":
                case "self":
                    thumbnail.setImageResource(R.drawable.ic_selftext);
                    break;
                case "default":  //reddit api has "default" value for a link submission
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

            if (data.getCommentsCount() > 999) {
                commentsCountButton.setText(mContext.getResources().getString(R.string.thousandSuffix, data.getCommentsCount() / 1000));
            } else {
                commentsCountButton.setText(String.valueOf(data.getCommentsCount()));
            }
            titleTv.setText(data.getTitle());
            uploadTimeView.setText(data.getCustomRelativeTime());
            subredditTv.setText(data.getSubreddit());
            if (data.getGildings().getSilver() == 0) {
                silverGildingsView.setVisibility(View.GONE);
            } else {
                silverGildingsView.setVisibility(View.VISIBLE);
                silverGildingsView.setText(String.valueOf(data.getGildings().getSilver()));
            }
            if (data.getGildings().getGold() == 0) {
                goldGildingsView.setVisibility(View.GONE);
            } else {
                goldGildingsView.setVisibility(View.VISIBLE);
                goldGildingsView.setText(String.valueOf(data.getGildings().getGold()));
            }

            if (data.getGildings().getPlatinum() == 0) {
                platinumGildingsView.setVisibility(View.GONE);
            } else {
                platinumGildingsView.setVisibility(View.VISIBLE);
                platinumGildingsView.setText(String.valueOf(data.getGildings().getPlatinum()));
            }
        }
    }
}

