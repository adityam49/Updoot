package com.ducktapedapps.updoot.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        super.submitList(new ArrayList<>(list));

    }

    @NonNull
    @Override
    public submissionsAdapter.submissionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.linear_submissions, parent, false);
        return new submissionHolder(view);
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
        @BindView(R.id.title_tv)
        TextView titleTv;
        @BindView(R.id.author_tv)
        TextView authorTv;
        @BindView(R.id.score_tv)
        TextView scoreTv;
        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.subredditTv)
        TextView subredditTv;

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
                scoreTv.setText(mContext.getResources().getString(R.string.upVoteSuffix, data.getUps() / 1000));
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
            authorTv.setText(data.getAuthor());
            subredditTv.setText(data.getSubreddit());
            itemView.setOnClickListener(v -> {
                if (mListener != null && getAdapterPosition() != RecyclerView.NO_POSITION)
                    mListener.onItemClick(getItem(getAdapterPosition()));
            });
        }
    }
}

