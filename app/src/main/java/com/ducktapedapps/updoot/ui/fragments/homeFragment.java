package com.ducktapedapps.updoot.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.ui.adapters.submissionsAdapter;
import com.ducktapedapps.updoot.utils.constants;
import com.ducktapedapps.updoot.utils.swipeUtils;
import com.ducktapedapps.updoot.viewModels.submissionsVM;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class homeFragment extends Fragment {
    private static final String TAG = "homeFragment";
    public static final String SUBREDDIT_KEY = "subreddit_key";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private Unbinder unbinder;

    private submissionsAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private submissionsVM viewModel;

    public static homeFragment newInstance() {
        Bundle args = new Bundle();
        homeFragment fragment = new homeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), RecyclerView.VERTICAL));
        adapter = new submissionsAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        viewModel = ViewModelProviders.of(this).get(submissionsVM.class);

        new ItemTouchHelper(new swipeUtils(getActivity(), new swipeUtils.swipeActionCallback() {
            @Override
            public void performSlightLeftSwipeAction(int adapterPosition) {
                viewModel.castVote(adapterPosition, -1);
            }

            @Override
            public void performSlightRightSwipeAction(int adapterPosition) {
                viewModel.castVote(adapterPosition, 1);
            }

            @Override
            public void performLeftSwipeAction(int adapterPosition) {
            }

            @Override
            public void performRightSwipeAction(int adapterPosition) {
                viewModel.save(adapterPosition);
            }
        })).attachToRecyclerView(recyclerView);

        viewModel.getState().observe(this, state -> {
            switch (state) {
                case constants.LOADING_STATE:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case constants.SUCCESS_STATE:
                    progressBar.setVisibility(View.GONE);
                    break;
                default:
                    Toast.makeText(getActivity(), state, Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    break;
            }
        });
        viewModel.getAllSubmissions().observe(this, things -> adapter.submitList(things));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {
                //check for scroll down
                if (dy > 0) {
                    int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                    int totalItems = linearLayoutManager.getItemCount();

                    // 10 is next page prefetch threshold
                    if (totalItems <= 10) return; //condition for no more pages

                    if (lastVisiblePosition == totalItems - 10) {
                        if ((viewModel.getState().getValue() != null && !viewModel.getState().getValue().equals(constants.LOADING_STATE)) && viewModel.getAfter() != null) {
                            viewModel.loadNextPage();
                        }
                    }
                }
            }
        });

        viewModel.getToastMessage().observe(this, message -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());

        adapter.setOnItemClickListener(new submissionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LinkData data) {
            }

            @Override
            public void onSubredditClick(LinkData data) {
            }
        });

        return view;
    }

    public void reload() {
        viewModel.reload(null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.frontpage_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.best:
                viewModel.reload(constants.BEST);
                break;
            case R.id.hot:
                viewModel.reload(constants.HOT);
                break;
            case R.id.New:
                viewModel.reload(constants.NEW);
                break;
            case R.id.rising:
                viewModel.reload(constants.RISING);
                break;
            case R.id.controversial:
                viewModel.reload(constants.CONTROVERSIAL);
                break;
            case R.id.top:
                viewModel.reload(constants.TOP);
                break;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

