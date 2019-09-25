package com.ducktapedapps.updoot.ui.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.databinding.LinearSubmissionsItemBinding;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.ui.fragments.SubredditFragment;
import com.ducktapedapps.updoot.utils.Constants;
import com.ducktapedapps.updoot.viewModels.SubmissionsVM;

import java.util.ArrayList;
import java.util.List;

public class submissionsAdapter extends ListAdapter<LinkData, submissionsAdapter.submissionHolder> {
    private static final String TAG = "submissionsAdapter";
    private static DiffUtil.ItemCallback<LinkData> CALLBACK = new DiffUtil.ItemCallback<LinkData>() {
        @Override
        public boolean areItemsTheSame(@NonNull LinkData oldItem, @NonNull LinkData newItem) {
            return oldItem.getName().equals(newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull LinkData oldItem, @NonNull LinkData newItem) {
            if (oldItem.getSelftext() == null) {
                return oldItem.getUps() == newItem.getUps();
            } else {
                return oldItem.getUps() == newItem.getUps() && oldItem.isSelfTextExpanded() == newItem.isSelfTextExpanded();
            }
        }

        @Override
        public Object getChangePayload(@NonNull LinkData oldItem, @NonNull LinkData newItem) {
            Bundle diffBundle = new Bundle();
            if (oldItem.getUps() != newItem.getUps()) {
                diffBundle.putInt(Constants.DIFF_VOTE_KEY, newItem.getUps());
            }
            if (diffBundle.isEmpty()) return super.getChangePayload(oldItem, newItem);
            else return diffBundle;
        }
    };
    private SubmissionsVM submissionsVM;
    private SubredditFragment.ClickHandler clickHandler;

    public submissionsAdapter(SubmissionsVM submissionsVM, SubredditFragment.ClickHandler clickHandler) {
        super(CALLBACK);
        this.submissionsVM = submissionsVM;
        this.clickHandler = clickHandler;
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
        LinearSubmissionsItemBinding binding = DataBindingUtil
                .inflate(
                        LayoutInflater.from(parent.getContext()),
                        R.layout.linear_submissions_item,
                        parent,
                        false
                );
        return new submissionHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull submissionsAdapter.submissionHolder holder, int position) {
        holder.binding.setLinkdata(getItem(position));
        holder.binding.setItemIndex(position);
        holder.binding.setClickHandler(clickHandler);
        holder.binding.setSubmissionVM(submissionsVM);
        holder.binding.executePendingBindings();
    }

    class submissionHolder extends RecyclerView.ViewHolder {
        private LinearSubmissionsItemBinding binding;

        submissionHolder(LinearSubmissionsItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

