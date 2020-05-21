package com.ducktapedapps.updoot.ui.comments

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentCommentsBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.common.SwipeCallback
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

        viewModel.apply {
            allComments.observe(viewLifecycleOwner) { commentsAdapter.submitList(it) }
            isLoading.observe(viewLifecycleOwner) { binding.swipeToRefreshLayout.isRefreshing = it }
        }
    }

    private fun setUpRecyclerView() {
        val handler = ClickHandler()
        binding.clickhandler = handler
        commentsAdapter = CommentsAdapter(handler)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentsAdapter
            ItemTouchHelper(SwipeCallback(
                    ContextCompat.getColor(requireContext(), R.color.saveContentColor),
                    ContextCompat.getColor(requireContext(), R.color.upVoteColor),
                    ContextCompat.getColor(requireContext(), R.color.downVoteColor),
                    ContextCompat.getColor(requireContext(), R.color.color_on_primary_light),
                    ContextCompat.getColor(requireContext(), R.color.color_background),
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_black_24dp)!!,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_upvote_24dp)!!,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_downvote_24dp)!!,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_expand_more_black_14dp)!!,
                    object : SwipeCallback.Callback {
                        override fun extremeLeftAction(position: Int) = Unit

                        override fun leftAction(position: Int) = viewModel.castVote(position, 1)

                        override fun rightAction(position: Int) = viewModel.castVote(position, -1)

                        override fun extremeRightAction(position: Int) = Unit
                    }

            )).attachToRecyclerView(this)
        }
    }

    inner class ClickHandler {
        fun onClick(index: Int) = viewModel.toggleChildrenVisibility(index)

        fun onImageClick(data: LinkData) {}
    }
}