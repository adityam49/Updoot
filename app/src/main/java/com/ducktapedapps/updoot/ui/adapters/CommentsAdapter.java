package com.ducktapedapps.updoot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.databinding.CommentItemBinding;
import com.ducktapedapps.updoot.model.CommentData;
import com.ducktapedapps.updoot.ui.fragments.commentsFragment;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends ListAdapter<CommentData, CommentsAdapter.commentHolder> {
    private static final String TAG = "CommentsAdapter";
    private static DiffUtil.ItemCallback<CommentData> CALLBACK = new DiffUtil.ItemCallback<CommentData>() {
        @Override
        public boolean areItemsTheSame(@NonNull CommentData oldItem, @NonNull CommentData newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CommentData oldItem, @NonNull CommentData newItem) {
            return (oldItem.getUps() == newItem.getUps())
                    && oldItem.getRepliesExpanded() == newItem.getRepliesExpanded();
        }

    };
    private commentsFragment.ClickHandler clickHandler;

    public CommentsAdapter(commentsFragment.ClickHandler handler) {
        super(CALLBACK);
        clickHandler = handler;
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
        CommentItemBinding commentItemBinding = DataBindingUtil
                .inflate(
                        LayoutInflater.from(parent.getContext()),
                        R.layout.comment_item,
                        parent,
                        false
                );
        return new commentHolder(commentItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull commentHolder holder, int position) {
        holder.binding.setCommentData(getItem(position));
        holder.binding.executePendingBindings();
    }

    class commentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CommentItemBinding binding;

        commentHolder(CommentItemBinding binding) {
            super(binding.getRoot());
            binding.getRoot().setOnClickListener(this);
            this.binding = binding;
        }

        @Override
        public void onClick(View v) {
            clickHandler.onClick(getAdapterPosition());
        }
    }
}
