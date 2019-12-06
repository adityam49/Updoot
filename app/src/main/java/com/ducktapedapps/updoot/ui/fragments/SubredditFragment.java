package com.ducktapedapps.updoot.ui.fragments;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.ducktapedapps.updoot.ui.adapters.SubmissionsAdapter;
import com.ducktapedapps.updoot.utils.CustomItemAnimator;
import com.ducktapedapps.updoot.utils.InfiniteScrollListener;
import com.ducktapedapps.updoot.utils.SwipeUtils;
import com.ducktapedapps.updoot.viewModels.ActivityVM;
import com.ducktapedapps.updoot.viewModels.SubmissionsVM;
import com.ducktapedapps.updoot.viewModels.SubmissionsVMFactory;

import javax.inject.Inject;

public class SubredditFragment extends Fragment {
    private static final String TAG = "SubredditFragment";
    private FragmentSubredditBinding binding;

    @Inject
    Application appContext;

    private SubmissionsVM submissionsVM;

    private SubmissionsAdapter adapter;

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
        adapter = new SubmissionsAdapter(new ClickHandler());
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
                if (!submissionsVM.getSubreddit().equals(data.getSubredditName())) {
                    SubredditFragmentDirections.ActionGoToSubreddit action = SubredditFragmentDirections.actionGoToSubreddit().setRSubreddit(data.getSubredditName());
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
        if (getArguments() != null) {
            String subreddit = SubredditFragmentArgs.fromBundle(getArguments()).getRSubreddit();
            subreddit = subreddit == null ? "" : subreddit;
            submissionsVM = new ViewModelProvider(this, new SubmissionsVMFactory(appContext, subreddit))
                    .get(SubmissionsVM.class);
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

    }

    private void reloadFragmentContent() {
        submissionsVM.reload(null, null);
    }

    public class ClickHandler {
        public void onClick(LinkData linkData) {
            SubredditFragmentDirections.ActionGoToComments action = SubredditFragmentDirections.actionGoToComments(linkData);
            navController.navigate(action);
        }

        public void handleImagePreview(LinkData data) {
            navController.navigate(
                    MediaPreviewFragmentDirections.
                            actionGlobalMediaPreviewFragment(
                                    data.getPreview().getImages().get(0).getSource().getUrl()
                            )
            );
        }

        public void handleExpansion(int index) {
            submissionsVM.expandSelfText(index);
        }
    }
}

