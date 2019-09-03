package com.ducktapedapps.updoot.ui.fragments;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.ui.adapters.submissionsAdapter;
import com.ducktapedapps.updoot.utils.InfinteScrollListener;
import com.ducktapedapps.updoot.utils.constants;
import com.ducktapedapps.updoot.utils.swipeUtils;
import com.ducktapedapps.updoot.viewModels.ActivityVM;
import com.ducktapedapps.updoot.viewModels.submissionsVM;
import com.ducktapedapps.updoot.viewModels.submissionsVMFactory;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class subredditFragment extends Fragment {
    private static final String TAG = "subredditFragment";
    private static final String SUBREDDIT_KEY = "subreddit_key";
    private static final String IS_BASE_FRAG_KEY = "isBaseFragmentKey";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Inject
    Application appContext;

    private submissionsVM submissionsVM;

    private Unbinder unbinder;
    private SlidrConfig slidrConfig;
    private SlidrInterface slidrInterface;
    private submissionsAdapter adapter;
    private boolean isBaseFragment;


    public static subredditFragment newInstance(String subreddit, boolean isFragmentAtBase) {
        Bundle args = new Bundle();
        args.putString(SUBREDDIT_KEY, subreddit);
        args.putBoolean(IS_BASE_FRAG_KEY, isFragmentAtBase);
        subredditFragment fragment = new subredditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((UpdootApplication) getActivity().getApplication()).getUpdootComponent().inject(this);
        assert getArguments() != null;
        this.isBaseFragment = getArguments().getBoolean(IS_BASE_FRAG_KEY);
        if (!isBaseFragment)
            slidrConfig = new SlidrConfig.Builder()
                    .edge(true)
                    .edgeSize(5f)
                    .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        if (slidrInterface == null && !isBaseFragment) {
            slidrInterface = Slidr.replace(getView().findViewById(R.id.subredditFragment), slidrConfig);

        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddit, container, false);
        unbinder = ButterKnife.bind(this, view);

        submissionsVM = new ViewModelProvider(this, new submissionsVMFactory(appContext, getArguments().getString(SUBREDDIT_KEY))).get(submissionsVM.class);
        ActivityVM activityVM = new ViewModelProvider(this.getActivity()).get(ActivityVM.class);
        submissionsVM.setCurrentAccount(activityVM.getCurrentAccount().getValue());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new submissionsAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new swipeUtils(getActivity(), new swipeUtils.swipeActionCallback() {
            @Override
            public void performSlightLeftSwipeAction(int adapterPosition) {
                submissionsVM.castVote(adapterPosition, -1);
            }

            @Override
            public void performSlightRightSwipeAction(int adapterPosition) {
                submissionsVM.castVote(adapterPosition, 1);
            }

            @Override
            public void performLeftSwipeAction(int adapterPosition) {
                LinkData data = adapter.getCurrentList().get(adapterPosition);
                if (!getArguments().get(SUBREDDIT_KEY).equals(data.getSubreddit_name_prefixed())) {
                    setHasOptionsMenu(false);
                    if (getFragmentManager() != null)
                        getFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                                .add(R.id.fragmentContainer, subredditFragment.newInstance(data.getSubreddit_name_prefixed(), false), String.valueOf(getFragmentManager().getBackStackEntryCount() + 1))
                                .addToBackStack(null)
                                .commit();
                }
            }

            @Override
            public void performRightSwipeAction(int adapterPosition) {
                submissionsVM.save(adapterPosition);
            }
        })).attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new InfinteScrollListener(linearLayoutManager, submissionsVM));

        adapter.setOnItemClickListener(data -> {
            Log.i(TAG, "onItemClick: " + data);
            if (getFragmentManager() != null)
                getFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right)
                        .add(R.id.fragmentContainer, commentsFragment.newInstance(data), String.valueOf(getFragmentManager().getBackStackEntryCount() + 1))
                        .addToBackStack(null)
                        .commit();
        });

        submissionsVM.getState().observe(getViewLifecycleOwner(), state -> {
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
        submissionsVM.getAllSubmissions().observe(getViewLifecycleOwner(), things -> adapter.submitList(things));

        submissionsVM.getToastMessage().observe(getViewLifecycleOwner(), message -> Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show());

        activityVM.getCurrentAccount().observe(getViewLifecycleOwner(), account -> {
            if (account != null) {
                if (submissionsVM.getCurrentAccount() == null || !submissionsVM.getCurrentAccount().equals(account)) {
                    if (getFragmentManager().getBackStackEntryCount() == Integer.parseInt(subredditFragment.this.getTag())) {
                        Toast.makeText(appContext, account + " logged in!", Toast.LENGTH_SHORT).show();
                        submissionsVM.setCurrentAccount(account);
                        reloadFragmentContent();
                    }
                }
            }

        });

        return view;
    }

    private void reloadFragmentContent() {
        submissionsVM.reload(null, null);
    }

    public void inflateSortPopup(View popupSourceView) {
        Log.i(TAG, "inflateSortPopup: " + popupSourceView);
        if (this.getContext() != null) {
            PopupMenu popup = new PopupMenu(this.getContext(), popupSourceView);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.subreddit_sort_menu, popup.getMenu());
            popup.show();
            popup.setOnMenuItemClickListener(item -> {
                String sortBy = null, timePeriod = null;
                Log.i(TAG, "inflateSortPopup: " + item.getTitle());
                switch (item.getItemId()) {
                    case R.id.best:
                        sortBy = constants.BEST;
                        break;
                    case R.id.hot:
                        sortBy = constants.HOT;
                        break;
                    case R.id.New:
                        sortBy = constants.NEW;
                        break;
                    case R.id.rising:
                        sortBy = constants.RISING;
                        break;
                    //controversial
                    case R.id.controversial_hour:
                        sortBy = constants.CONTROVERSIAL;
                        timePeriod = constants.NOW;
                        break;
                    case R.id.controversial_day:
                        sortBy = constants.CONTROVERSIAL;
                        timePeriod = constants.TODAY;
                        break;
                    case R.id.controversial_week:
                        sortBy = constants.CONTROVERSIAL;
                        timePeriod = constants.THIS_WEEK;
                        break;
                    case R.id.controversial_month:
                        sortBy = constants.CONTROVERSIAL;
                        timePeriod = constants.THIS_MONTH;
                        break;
                    case R.id.controversial_year:
                        sortBy = constants.CONTROVERSIAL;
                        timePeriod = constants.THIS_YEAR;
                        break;
                    case R.id.controversial_all_time:
                        sortBy = constants.CONTROVERSIAL;
                        timePeriod = constants.ALL_TIME;
                        break;
                    //top
                    case R.id.top_hour:
                        sortBy = constants.TOP;
                        timePeriod = constants.NOW;
                        break;
                    case R.id.top_day:
                        sortBy = constants.TOP;
                        timePeriod = constants.TODAY;
                        break;
                    case R.id.top_week:
                        sortBy = constants.TOP;
                        timePeriod = constants.THIS_WEEK;
                        break;
                    case R.id.top_month:
                        sortBy = constants.TOP;
                        timePeriod = constants.THIS_MONTH;
                        break;
                    case R.id.top_year:
                        sortBy = constants.TOP;
                        timePeriod = constants.THIS_YEAR;
                        break;
                    case R.id.top_all_time:
                        sortBy = constants.TOP;
                        timePeriod = constants.ALL_TIME;
                        break;
                }
                if (sortBy == null) {
                    return false;
                }
                submissionsVM.reload(sortBy, timePeriod);
                return true;
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }

}

