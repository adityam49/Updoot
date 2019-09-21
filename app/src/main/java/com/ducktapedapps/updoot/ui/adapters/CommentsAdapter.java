package com.ducktapedapps.updoot.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.CommentData;
import com.ducktapedapps.updoot.utils.constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CommentsAdapter extends ListAdapter<CommentData, CommentsAdapter.commentHolder> {
    private static final String TAG = "CommentsAdapter";
    private static DiffUtil.ItemCallback<CommentData> CALLBACK = new DiffUtil.ItemCallback<CommentData>() {
        @Override
        public boolean areItemsTheSame(@NonNull CommentData oldItem, @NonNull CommentData newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CommentData oldItem, @NonNull CommentData newItem) {
            return oldItem.getUps() == newItem.getUps();
        }

        @Override
        public Object getChangePayload(@NonNull CommentData oldItem, @NonNull CommentData newItem) {
            Bundle diffBundle = new Bundle();
            if (oldItem.getUps() != newItem.getUps()) {
                diffBundle.putInt(constants.DIFF_VOTE_KEY, newItem.getUps());
            }
            if (diffBundle.isEmpty()) return super.getChangePayload(oldItem, newItem);
            else return diffBundle;
        }
    };
    private Context mContext;
    private OnItemClickListener mListener;

    public CommentsAdapter(Context context) {
        super(CALLBACK);
        mContext = context;
    }

    // DiffUtils doesn't calculate diff if passed with same list object
    // https://stackoverflow.com/questions/49726385/listadapter-not-updating-item-in-reyclerview/50062174#50062174
    @Override
    public void submitList(List<CommentData> list) {
        List<CommentData> updatedList = new ArrayList<>();
        if (list != null) updatedList.addAll(list);
        super.submitList(updatedList);
    }

    @NonNull
    @Override
    public commentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new commentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull commentHolder holder, int position, @NonNull List<Object> payloads) {
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
    public void onBindViewHolder(@NonNull commentHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mListener = onItemClickListener;
    }


    public interface OnItemClickListener {
        void onItemClick(CommentData data);
    }

    public class commentHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.comment_tv)
        TextView commentBody;
        @BindView(R.id.score_tv)
        TextView scoreTv;
        @BindView(R.id.username_tv)
        TextView username;
        @BindView(R.id.commentThreadView)
        View view;

        commentHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.commentView)
        void OnClick() {
            if (mListener != null)
                mListener.onItemClick(getItem(getAdapterPosition()));
        }

        void bind(CommentData data) {
            if (data.getUps() > 999) {
                scoreTv.setText(mContext.getResources().getString(R.string.thousandSuffix, data.getUps() / 1000));
            } else {
                scoreTv.setText(String.valueOf(data.getUps()));
            }
            if (data.getDepth() == 0) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
            if (data.getLikes() == null) {
                scoreTv.setTextColor(Color.WHITE);
            } else if (data.getLikes()) {
                scoreTv.setTextColor(ContextCompat.getColor(mContext, R.color.upVoteColor));
            } else {
                scoreTv.setTextColor(ContextCompat.getColor(mContext, R.color.downVoteColor));
            }

            commentBody.setText(data.getBody());
            username.setText(data.getAuthor());
        }
    }
}
