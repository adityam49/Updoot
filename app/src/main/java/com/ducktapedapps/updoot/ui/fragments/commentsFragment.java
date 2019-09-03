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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

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
    private SlidrConfig slidrConfig;
    private SlidrInterface slidrInterface;

    public static commentsFragment newInstance(LinkData data) {
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
        Log.i(TAG, "onResume: ");
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
                }
                String selftext = data.getSelftext();
                if (selftext != null && !selftext.equals("")) {
                    selfText_tv.setText(selftext);
                    selfTextCardView.setVisibility(View.VISIBLE);
                } else {
                    selfText_tv.setVisibility(GONE);
                }
                String upvotes;
                if (data.getUps() > 999) {
                    upvotes = getResources().getString(R.string.thousandSuffix, data.getUps() / 1000);
                } else {
                    upvotes = String.valueOf(data.getUps());
                }
                StringBuilder metadata = new StringBuilder()
                        .append("In ")
                        .append(data.getSubreddit_name_prefixed())
                        .append(" by ")
                        .append(data.getAuthor());
                metadata_Tv.setText(metadata);
                StringBuilder metadata2 = new StringBuilder()
                        .append(upvotes)
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
            }
        }
        return view;
    }
}
