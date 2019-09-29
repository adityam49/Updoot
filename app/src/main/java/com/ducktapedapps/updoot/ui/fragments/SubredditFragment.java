package com.ducktapedapps.updoot.ui.fragments;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.databinding.FragmentSubredditBinding;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.ui.adapters.submissionsAdapter;
import com.ducktapedapps.updoot.utils.Constants;
import com.ducktapedapps.updoot.utils.CustomItemAnimator;
import com.ducktapedapps.updoot.utils.InfiniteScrollListener;
import com.ducktapedapps.updoot.utils.SwipeUtils;
import com.ducktapedapps.updoot.viewModels.ActivityVM;
import com.ducktapedapps.updoot.viewModels.SubmissionsVM;
import com.ducktapedapps.updoot.viewModels.SubmissionsVMFactory;

import javax.inject.Inject;

public class SubredditFragment extends Fragment {
    private static final String TAG = "SubredditFragment";
    private static final String IS_BASE_FRAG_KEY = "isBaseFragmentKey";
    private FragmentSubredditBinding binding;

    @Inject
    Application appContext;

    private SubmissionsVM submissionsVM;

    private submissionsAdapter adapter;

    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null)
            ((UpdootApplication) getActivity().getApplication()).getUpdootComponent().inject(this);
        assert getArguments() != null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subreddit, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        setUpViewModel();
        setUpRecyclerView();
        return binding.getRoot();
    }

    private void setUpRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new submissionsAdapter(submissionsVM, new ClickHandler());
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new CustomItemAnimator());

        new ItemTouchHelper(new SwipeUtils(getActivity(), new SwipeUtils.swipeActionCallback() {
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
                if (!submissionsVM.getSubreddit().equals(data.getSubreddit_name_prefixed())) {
                    SubredditFragmentDirections.ActionGoToSubreddit action = SubredditFragmentDirections.actionGoToSubreddit().setRSubreddit(data.getSubreddit_name_prefixed());
                    navController.navigate(action);
                }
            }

            @Override
            public void performRightSwipeAction(int adapterPosition) {
                submissionsVM.save(adapterPosition);
            }
        })).attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new InfiniteScrollListener(linearLayoutManager, submissionsVM));


        SwipeRefreshLayout swipeRefreshLayout = binding.swipeToRefreshLayout;
        swipeRefreshLayout.setColorSchemeResources(
                R.color.DT_primaryColor,
                R.color.secondaryColor,
                R.color.secondaryDarkColor);
        swipeRefreshLayout.setOnRefreshListener(this::reloadFragmentContent);
    }

    private void setUpViewModel() {
        String subreddit = SubredditFragmentArgs.fromBundle(getArguments()).getRSubreddit();
        subreddit = subreddit == null ? "" : subreddit;
        submissionsVM = new ViewModelProvider(this, new SubmissionsVMFactory(appContext, subreddit, getArguments().getBoolean(IS_BASE_FRAG_KEY))).get(SubmissionsVM.class);
        binding.setSubmissionViewModel(submissionsVM);

        if (this.getActivity() != null) {
            ActivityVM activityVM = new ViewModelProvider(this.getActivity()).get(ActivityVM.class);
            activityVM.getCurrentAccount().observe(getViewLifecycleOwner(), account -> {
                if (account != null && account.getContentIfNotHandled() != null) {
                    reloadFragmentContent();
                    Toast.makeText(this.getContext(), account.peekContent() + " is logged in!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        submissionsVM.getAllSubmissions().observe(getViewLifecycleOwner(), things -> adapter.submitList(things));
        submissionsVM.getToastMessage().observe(getViewLifecycleOwner(), toastMessage -> {
            String toast = toastMessage.getContentIfNotHandled();
            if (toast != null) {
                Toast.makeText(this.getContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void inflateSortPopup(View popupSourceView) {
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
                        sortBy = Constants.BEST;
                        break;
                    case R.id.hot:
                        sortBy = Constants.HOT;
                        break;
                    case R.id.New:
                        sortBy = Constants.NEW;
                        break;
                    case R.id.rising:
                        sortBy = Constants.RISING;
                        break;
                    //controversial
                    case R.id.controversial_hour:
                        sortBy = Constants.CONTROVERSIAL;
                        timePeriod = Constants.NOW;
                        break;
                    case R.id.controversial_day:
                        sortBy = Constants.CONTROVERSIAL;
                        timePeriod = Constants.TODAY;
                        break;
                    case R.id.controversial_week:
                        sortBy = Constants.CONTROVERSIAL;
                        timePeriod = Constants.THIS_WEEK;
                        break;
                    case R.id.controversial_month:
                        sortBy = Constants.CONTROVERSIAL;
                        timePeriod = Constants.THIS_MONTH;
                        break;
                    case R.id.controversial_year:
                        sortBy = Constants.CONTROVERSIAL;
                        timePeriod = Constants.THIS_YEAR;
                        break;
                    case R.id.controversial_all_time:
                        sortBy = Constants.CONTROVERSIAL;
                        timePeriod = Constants.ALL_TIME;
                        break;
                    //top
                    case R.id.top_hour:
                        sortBy = Constants.TOP;
                        timePeriod = Constants.NOW;
                        break;
                    case R.id.top_day:
                        sortBy = Constants.TOP;
                        timePeriod = Constants.TODAY;
                        break;
                    case R.id.top_week:
                        sortBy = Constants.TOP;
                        timePeriod = Constants.THIS_WEEK;
                        break;
                    case R.id.top_month:
                        sortBy = Constants.TOP;
                        timePeriod = Constants.THIS_MONTH;
                        break;
                    case R.id.top_year:
                        sortBy = Constants.TOP;
                        timePeriod = Constants.THIS_YEAR;
                        break;
                    case R.id.top_all_time:
                        sortBy = Constants.TOP;
                        timePeriod = Constants.ALL_TIME;
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

    private void reloadFragmentContent() {
        submissionsVM.reload(null, null);
    }

    public class ClickHandler {
        public void onClick(LinkData linkData) {
            SubredditFragmentDirections.ActionGoToComments action = SubredditFragmentDirections.actionGoToComments(linkData);
            navController.navigate(action);

        }
    }
}

