package com.ducktapedapps.updoot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.model.thing;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class submissionsAdapter extends ListAdapter<thing, submissionsAdapter.submissionHolder> {
    private static final String TAG = "submissionsAdapter";

    public submissionsAdapter() {
        super(callback);
    }

    //    https://stackoverflow.com/questions/49726385/listadapter-not-updating-item-in-reyclerview/50062174#50062174
    @Override
    public void submitList(@Nullable List<thing> list) {
        super.submitList(list != null ? new ArrayList<>(list) : null);
    }

    @NonNull
    @Override
    public submissionsAdapter.submissionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.linear_submission, parent, false);
        return new submissionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull submissionsAdapter.submissionHolder holder, int position) {
        if (getItem(position).getData() instanceof LinkData) {
            holder.bind((LinkData) getItem(position).getData());
        }
    }

    private static final DiffUtil.ItemCallback<thing> callback = new DiffUtil.ItemCallback<thing>() {
        @Override
        public boolean areItemsTheSame(@NonNull thing oldItem, @NonNull thing newItem) {
            if (oldItem.getData() instanceof LinkData && newItem.getData() instanceof LinkData) {
                return ((LinkData) oldItem.getData()).getId().equals(((LinkData) newItem.getData()).getId());
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull thing oldItem, @NonNull thing newItem) {
            if (oldItem.getData() instanceof LinkData && newItem.getData() instanceof LinkData) {
                return ((LinkData) oldItem.getData()).getUps() == ((LinkData) newItem.getData()).getUps();
            }
            return false;
        }
    };

    class submissionHolder extends RecyclerView.ViewHolder {
        private TextView titleTv;
        private TextView authorTv;
        private TextView scoreTv;
        private ImageView thumbnail;

        submissionHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.thumbnail);
            scoreTv = view.findViewById(R.id.scoreTv);
            titleTv = view.findViewById(R.id.title_tv);
            authorTv = view.findViewById(R.id.author_tv);
        }

        void bind(LinkData data) {
            Picasso.get().load(data.getThumbnail()).placeholder(R.drawable.ic_android_black_24dp).into(thumbnail);
            scoreTv.setText(String.valueOf(data.getUps()));
            titleTv.setText(data.getTitle());
            authorTv.setText(data.getAuthor());
        }
    }

}

