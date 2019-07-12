package com.ducktapedapps.updoot.ui.fragments;

import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.BuildConfig;
import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.ui.adapters.submissionsAdapter;
import com.ducktapedapps.updoot.utils.constants;
import com.ducktapedapps.updoot.viewModels.submissionsVM;

public class homeFragment extends Fragment {

    private static final String TAG = "homeFragment";
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
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        ProgressBar progressBar = view.findViewById(R.id.progress_circular);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new submissionsAdapter(getActivity());
        recyclerView.setAdapter(adapter);


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                //check for scroll down
                if (dy > 0) {
                    int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                    int totalItems = linearLayoutManager.getItemCount();
                    // 5 is next page prefetch threshold
                    if (totalItems <= 10) {
                        //condition for no more pages
                        return;
                    }
                    if (lastVisiblePosition == totalItems - 10
                            && !viewModel.getState().getValue().equals(constants.LOADING_STATE)
                            && viewModel.getHasNextPage().getValue()) {
                        viewModel.loadNextPage();
                    }
                }
            }
        });

        viewModel = ViewModelProviders.of(this).get(submissionsVM.class);
        viewModel.getState().observe(this, state -> {
            switch (state) {
                case constants.LOADING_STATE:
                    progressBar.setVisibility(View.VISIBLE);
                    break;
                case constants.SUCCESS_STATE:
                    progressBar.setVisibility(View.GONE);
                    break;
                default:
                    progressBar.setVisibility(View.GONE);
                    Log.i(TAG, "error " + state);
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(getActivity(), state, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        });
        viewModel.getAllSubmissions().observe(this, things -> {
            Log.i(TAG, "onChanged: ");
            if (things != null) {
                Log.i(TAG, "submitting " + things.size() + " posts ");
                adapter.submitList(things);
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
                Log.i(TAG, "onOptionsItemSelected: best");
                viewModel.reload(constants.BEST);
                break;
            case R.id.hot:
                Log.i(TAG, "onOptionsItemSelected: hot");
                viewModel.reload(constants.HOT);
                break;
            case R.id.New:
                viewModel.reload(constants.NEW);
                Log.i(TAG, "onOptionsItemSelected: new");
                break;
            case R.id.rising:
                viewModel.reload(constants.RISING);
                Log.i(TAG, "onOptionsItemSelected: rising");
                break;
            case R.id.controversial:
                viewModel.reload(constants.CONTROVERSIAL);
                Log.i(TAG, "onOptionsItemSelected: controversial");
                break;
            case R.id.top:
                viewModel.reload(constants.TOP);
                Log.i(TAG, "onOptionsItemSelected: top");
                break;
        }
        return true;
    }

}

