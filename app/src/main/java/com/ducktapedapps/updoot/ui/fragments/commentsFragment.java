package com.ducktapedapps.updoot.ui.fragments;

import android.app.Application;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import com.ducktapedapps.updoot.R;
import com.ducktapedapps.updoot.UpdootApplication;
import com.ducktapedapps.updoot.databinding.FragmentCommentsBinding;
import com.ducktapedapps.updoot.model.LinkData;
import com.ducktapedapps.updoot.ui.adapters.CommentsAdapter;
import com.ducktapedapps.updoot.utils.SwipeUtils;
import com.ducktapedapps.updoot.viewModels.CommentsVM;
import com.ducktapedapps.updoot.viewModels.CommentsVMFactory;

import javax.inject.Inject;

import static com.ducktapedapps.updoot.BR.linkdata;

public class commentsFragment extends Fragment {
    @Inject
    Application appContext;
    private static final String TAG = "commentsFragment";
    private FragmentCommentsBinding binding;
    private CommentsVM viewModel;
    private CommentsAdapter adapter;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null)
            ((UpdootApplication) getActivity().getApplication()).getUpdootComponent().inject(this);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_comments, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        if (getArguments() != null) {

            LinkData data = commentsFragmentArgs.fromBundle(getArguments()).getSubmissionData();
            binding.setVariable(linkdata, data);
            setUpRecyclerView();
            setUpViewModel(data);
        }
        return binding.getRoot();
    }

    private void setUpViewModel(LinkData data) {
        viewModel = new ViewModelProvider(commentsFragment.this,
                new CommentsVMFactory(appContext, data.getId(), data.getSubredditName())
        ).get(CommentsVM.class);
        binding.setCommentsViewModel(viewModel);
        viewModel.getAllComments().observe(commentsFragment.this, commentDataList -> adapter.submitList(commentDataList));
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(commentsFragment.this.getContext());
        RecyclerView recyclerView = binding.recyclerView;
        ClickHandler handler = new ClickHandler();
        binding.setClickhandler(handler);
        adapter = new CommentsAdapter(handler);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new SwipeUtils(commentsFragment.this.getContext(), new SwipeUtils.swipeActionCallback() {
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

    public class ClickHandler {
        public void onClick(int index) {
            viewModel.toggleChildrenVisibility(index);
        }

        public void onImageClick(LinkData data) {
            navController = Navigation.findNavController(commentsFragment.this.binding.getRoot());
            navController.navigate(
                    MediaPreviewFragmentDirections.
                            actionGlobalMediaPreviewFragment(
                                    data.getPreview().getImages().get(0).getSource().getUrl()
                            )
            );
        }
    }

}
