package com.ducktapedapps.updoot.ui.comments

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentCommentsBinding
import com.ducktapedapps.updoot.model.BaseComment
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.utils.SwipeUtils
import com.google.android.material.appbar.MaterialToolbar
import javax.inject.Inject

class CommentsFragment : Fragment() {
    @Inject
    lateinit var appContext: Application

    private lateinit var binding: FragmentCommentsBinding
    private lateinit var viewModel: CommentsVM
    private lateinit var commentsAdapter: CommentsAdapter
    private val args: CommentsFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as UpdootApplication).updootComponent.inject(this@CommentsFragment)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCommentsBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        setUpRecyclerView()
        binding.linkdata = args.submissionData
        setUpViewModel(args.submissionData)

        return binding.root
    }

    private fun setUpViewModel(data: LinkData) {
        viewModel = ViewModelProvider(this@CommentsFragment,
                CommentsVMFactory(appContext as UpdootApplication, data.id, data.subredditName)
        ).get(CommentsVM::class.java)
        binding.commentsViewModel = viewModel

        viewModel.allComments.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer { commentDataList: List<BaseComment>? ->
                    commentsAdapter.submitList(commentDataList)
                }
        )
    }

    private fun setUpRecyclerView() {
        val handler = ClickHandler()
        binding.clickhandler = handler
        commentsAdapter = CommentsAdapter(handler)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentsAdapter
        }

        ItemTouchHelper(SwipeUtils(this@CommentsFragment.context, object : SwipeUtils.SwipeActionCallback {
            override fun performSlightLeftSwipeAction(adapterPosition: Int) {
                viewModel.castVote(-1, adapterPosition)
            }

            override fun performSlightRightSwipeAction(adapterPosition: Int) {
                viewModel.castVote(1, adapterPosition)
            }

            override fun performLeftSwipeAction(adapterPosition: Int) {}
            override fun performRightSwipeAction(adapterPosition: Int) {}
        })).attachToRecyclerView(binding.recyclerView)
    }

    inner class ClickHandler {
        fun onClick(index: Int) = viewModel.toggleChildrenVisibility(index)

        fun onImageClick(data: LinkData) {}
    }
}

@BindingAdapter("commentCount")
fun bindCommentCount(toolbar: MaterialToolbar, count: Int) {
    toolbar.title = if (count == 0) "No comments found"
    else String.format("%d Comments", count)
}