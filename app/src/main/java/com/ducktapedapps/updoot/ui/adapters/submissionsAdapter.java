package com.ducktapedapps.updoot.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.model.thing;

import java.util.ArrayList;
import java.util.List;

public class submissionsAdapter extends RecyclerView.Adapter<submissionsAdapter.submissionHolder> {

    private List<thing> allSubmissions;

    public submissionsAdapter() {
        this.allSubmissions = new ArrayList<>();
    }

    public void addSubmissions(List<thing> submissions) {
        this.allSubmissions.addAll(submissions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public submissionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.linear_submission, parent, false);
        return new submissionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull submissionHolder holder, int position) {
        holder.bind((LinkData) allSubmissions.get(position).getData());
    }

    @Override
    public int getItemCount() {
        return allSubmissions == null ? 0 : allSubmissions.size();
    }

    class submissionHolder extends RecyclerView.ViewHolder {
        private TextView titleTv;
        private TextView authorTv;

        submissionHolder(View view) {
            super(view);
            titleTv = view.findViewById(R.id.title_tv);
            authorTv = view.findViewById(R.id.author_tv);
        }

        void bind(LinkData data) {
            titleTv.setText(data.getTitle());
            authorTv.setText(data.getAuthor());
        }
    }
}
