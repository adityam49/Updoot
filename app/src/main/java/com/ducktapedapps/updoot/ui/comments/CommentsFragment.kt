package com.ducktapedapps.updoot.ui.comments

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentCommentsBinding
import com.ducktapedapps.updoot.ui.common.SwipeCallback
import javax.inject.Inject

class CommentsFragment : Fragment() {
    @Inject
    lateinit var commentsVMFactory: CommentsVMFactory
    private lateinit var viewModel: CommentsVM

    private var _binding: FragmentCommentsBinding? = null
    private val binding: FragmentCommentsBinding
        get() = _binding!!

    @Inject
    lateinit var contentAdapter: ContentAdapter

    private val commentsAdapter = CommentsAdapter(::expandCollapseComment)
    private val submissionHeaderAdapter = SubmissionMetaDataAdapter()

    private val args: CommentsFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity?.application as UpdootApplication).updootComponent.inject(this@CommentsFragment)
        setUpViewModel(args.subreddit, args.id)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comment_screen_menu, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpRecyclerView()
        observeData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpViewModel(name: String, id: String) {
        viewModel = ViewModelProvider(
                this@CommentsFragment,
                commentsVMFactory.apply { setSubredditAndId(name, id) }
        ).get(CommentsVM::class.java)
    }

    private fun observeData() = viewModel.apply {
        allComments.observe(viewLifecycleOwner) { commentsAdapter.submitList(it) }
        isLoading.observe(viewLifecycleOwner) { binding.swipeToRefreshLayout.isRefreshing = it }
        submissionData.observe(viewLifecycleOwner) { submissionHeaderAdapter.linkData = it }
        content.observe(viewLifecycleOwner) { contentAdapter.content = it }
    }

    private fun setUpRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = MergeAdapter(submissionHeaderAdapter, contentAdapter, commentsAdapter)
            ItemTouchHelper(SwipeCallback(
                    getColor(R.color.saveContentColor),
                    getColor(R.color.upVoteColor),
                    getColor(R.color.downVoteColor),
                    getColor(R.color.color_on_primary_light),
                    getColor(R.color.color_background),
                    getDrawable(R.drawable.ic_star_24dp)!!,
                    getDrawable(R.drawable.ic_upvote_24dp)!!,
                    getDrawable(R.drawable.ic_downvote_24dp)!!,
                    getDrawable(R.drawable.ic_expand_more_black_14dp)!!,
                    object : SwipeCallback.Callback {
                        override fun extremeLeftAction(position: Int) = Unit

                        override fun leftAction(position: Int) = viewModel.castVote(position, 1)

                        override fun rightAction(position: Int) = viewModel.castVote(position, -1)

                        override fun extremeRightAction(position: Int) = Unit
                    }
            )).attachToRecyclerView(this)
        }
    }

    private fun getColor(@ColorRes color: Int): Int = ContextCompat.getColor(requireContext(), color)

    private fun getDrawable(@DrawableRes drawableRes: Int): Drawable? = ContextCompat.getDrawable(requireContext(), drawableRes)

    private fun expandCollapseComment(index: Int) = viewModel.toggleChildrenVisibility(index)
}