package com.ducktapedapps.updoot.ui.fragments;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.ui.adapters.CommentsAdapter;
import com.ducktapedapps.updoot.utils.CustomItemAnimator;
import com.ducktapedapps.updoot.utils.MarkdownUtils;
import com.ducktapedapps.updoot.utils.swipeUtils;
import com.ducktapedapps.updoot.viewModels.commentsVM;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.kaelaela.opengraphview.OpenGraphView;

public class commentsFragment extends Fragment {
    private static final String TAG = "commentsFragment";
    private static final String SUBMISSIONS_DATA_KEY = "submissions_data_key";
    @BindView(R.id.metadata2)
    TextView metadata2_tv;
    @BindView(R.id.silverGildingsView)
    TextView silver_tv;
    @BindView(R.id.goldGildingsView)
    TextView gold_tv;
    @BindView(R.id.platinumGildingsView)
    TextView platinum_tv;
    @BindView(R.id.mediaPreview)
    ImageView preview;
    @BindView(R.id.title_tv)
    TextView title_tv;
    @BindView(R.id.selfText_tv)
    TextView selfText_tv;
    @BindView(R.id.metadata)
    TextView metadata_Tv;
    @BindView(R.id.selftext_cardView)
    CardView selfTextCardView;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.richLinkPreview)
    OpenGraphView richtextView;

    private commentsVM viewModel;
    private SlidrConfig slidrConfig;
    private SlidrInterface slidrInterface;

    private CommentsAdapter adapter;

    static commentsFragment newInstance(LinkData data) {
        Log.i(TAG, "newInstance: " + data);
        Bundle args = new Bundle();
        args.putSerializable(SUBMISSIONS_DATA_KEY, data);
        commentsFragment fragment = new commentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        slidrConfig = new SlidrConfig.Builder()
                .edge(true)
                .edgeSize(5f)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (slidrInterface == null && getView() != null) {
            slidrInterface = Slidr.replace(getView().findViewById(R.id.commentsFragmentContent), slidrConfig);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);
        ButterKnife.bind(this, view);

        if (getArguments() != null) {
            LinkData data = (LinkData) getArguments().getSerializable(SUBMISSIONS_DATA_KEY);
            if (data != null) {
                Log.i(TAG, "onCreateView: " + data);

                title_tv.setText(data.getTitle());
                if (data.getPost_hint() != null && data.getPost_hint().equals("image")) {
                    Glide.with(this)
                            .load(data.getPreview().toString())
                            .transform(new FitCenter(), new RoundedCorners(16))
                            .into(preview)
                            .getView().setVisibility(View.VISIBLE);
                } else if (data.getSelftext() != null && !data.getSelftext().isEmpty()) {
                    setSelfText(data.getSelftext());
                } else if (data.getUrl() != null && !data.getUrl().isEmpty()) {
                    setRichLinkPreview(data.getUrl());
                }


                String upVotes;
                if (data.getUps() > 999) {
                    upVotes = getResources().getString(R.string.thousandSuffix, data.getUps() / 1000);
                } else {
                    upVotes = String.valueOf(data.getUps());
                }
                StringBuilder metadata = new StringBuilder()
                        .append("In ")
                        .append(data.getSubreddit_name_prefixed())
                        .append(" by ")
                        .append(data.getAuthor());
                metadata_Tv.setText(metadata);
                StringBuilder metadata2 = new StringBuilder()
                        .append(upVotes)
                        .append("\u2191")
                        .append(" \u2022 ")
                        .append(DateUtils.getRelativeTimeSpanString(data.getCreated() * 1000, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))
                        .append(" \u2022 ");
                metadata2_tv.setText(metadata2);
                if (data.getGildings().getSilver() != 0) {
                    silver_tv.setText(String.valueOf(data.getGildings().getSilver()));
                    silver_tv.setVisibility(View.VISIBLE);
                }
                if (data.getGildings().getGold() != 0) {
                    gold_tv.setText(String.valueOf(data.getGildings().getGold()));
                    gold_tv.setVisibility(View.VISIBLE);
                }
                if (data.getGildings().getPlatinum() != 0) {
                    platinum_tv.setText(String.valueOf(data.getGildings().getPlatinum()));
                    platinum_tv.setVisibility(View.VISIBLE);
                }

                setUpRecyclerView();
                setUpViewModel(data);
            }


        }
        return view;
    }

    private void setUpViewModel(LinkData data) {
        viewModel = new ViewModelProvider(this).get(commentsVM.class);

        viewModel.loadComments(data.getSubreddit_name_prefixed(), data.getId());

        viewModel.getAllComments().observe(this, commentDataList -> adapter.submitList(commentDataList));
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(commentsFragment.this.getContext());
        adapter = new CommentsAdapter(commentsFragment.this.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new CustomItemAnimator());

        new ItemTouchHelper(new swipeUtils(commentsFragment.this.getContext(), new swipeUtils.swipeActionCallback() {
            @Override
            public void performSlightLeftSwipeAction(int adapterPosition) {
            }

            @Override
            public void performSlightRightSwipeAction(int adapterPosition) {
            }

            @Override
            public void performLeftSwipeAction(int adapterPosition) {
            }

            @Override
            public void performRightSwipeAction(int adapterPosition) {
            }
        })).attachToRecyclerView(recyclerView);
    }

    private void setSelfText(String html_selfText) {
        MarkdownUtils.decodeAndSet(html_selfText, selfText_tv);
        selfTextCardView.setVisibility(View.VISIBLE);
        selfText_tv.setVisibility(View.VISIBLE);
    }

    private void setRichLinkPreview(String url) {
        richtextView.loadFrom(url);
        richtextView.setVisibility(View.VISIBLE);
        selfTextCardView.setVisibility(View.VISIBLE);
    }
}
