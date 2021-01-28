package com.ducktapedapps.updoot.ui.comments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.FragmentCommentsBinding
import com.ducktapedapps.updoot.ui.VideoPreviewFragment
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.*
import com.ducktapedapps.updoot.ui.common.SwipeCallback
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentsFragment : Fragment() {
    companion object {
        const val SUBREDDIT_KEY = "subreddit_key"
        const val COMMENTS_KEY = "comments_key"
        fun newInstance(subreddit: String, commentsId: String) = CommentsFragment().apply {
            arguments = Bundle().apply {
                putString(SUBREDDIT_KEY, subreddit)
                putString(COMMENTS_KEY, commentsId)
            }
        }
    }

    private val viewModel: CommentsVM by viewModels()
    private var _binding: FragmentCommentsBinding? = null
    private val binding: FragmentCommentsBinding
        get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comment_screen_menu, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val contentAdapter = ContentAdapter(object : ContentAdapter.ClickHandler {
            override fun onClick(content: SubmissionContent) {
                when (content) {
                    is Image -> openImage(content.data.lowResUrl, content.data.highResUrl)
                    is Video -> openVideo(content.data.url)
                    is LinkState.LoadedLink -> openLink(content.linkModel.url)
                    is LinkState.LoadingLink -> openLink(content.url)
                    is LinkState.NoMetaDataLink -> openLink(content.url)
                    is SelfText -> Unit
                    is JustTitle -> Unit
                }
            }
        })
        val commentsAdapter = CommentsAdapter(
                viewModel::toggleChildrenVisibility,
                viewModel::loadMoreComment,
                singleThreadMode = true,
                singleThreadColorMode = true
        )
        val submissionHeaderAdapter = SubmissionMetaDataAdapter()
        setUpRecyclerView(submissionHeaderAdapter, contentAdapter, commentsAdapter)
        observeData(submissionHeaderAdapter, contentAdapter, commentsAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }

    private fun observeData(submissionHeaderAdapter: SubmissionMetaDataAdapter, contentAdapter: ContentAdapter, commentsAdapter: CommentsAdapter) = viewModel.apply {
        isLoading.observe(viewLifecycleOwner, { binding.swipeToRefreshLayout.isRefreshing = it })
        submissionData.asLiveData().observe(viewLifecycleOwner, { submissionHeaderAdapter.linkData = it })
        content.asLiveData().observe(viewLifecycleOwner, { contentAdapter.submitList(listOf(it)) })
        allComments.asLiveData().observe(viewLifecycleOwner) {
            commentsAdapter.submitList(it)
        }
    }

    private fun setUpRecyclerView(submissionHeaderAdapter: SubmissionMetaDataAdapter, contentAdapter: ContentAdapter, commentsAdapter: CommentsAdapter) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ConcatAdapter(submissionHeaderAdapter, contentAdapter, commentsAdapter)
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
                        override fun onExtremeLeftSwipe(swipedThingData: String?) = Unit
                        override fun onLeftSwipe(swipedThingData: String?) = viewModel.castVote(-1, -1)
                        override fun onRightSwipe(swipedThingData: String?) = viewModel.castVote(1, -1)
                        override fun onExtremeRightSwipe(swipedThingData: String?) = Unit
                    }
            )).attachToRecyclerView(this)
        }
    }

    private fun getColor(@ColorRes color: Int): Int = ContextCompat.getColor(requireContext(), color)

    private fun getDrawable(@DrawableRes drawableRes: Int): Drawable? = ContextCompat.getDrawable(requireContext(), drawableRes)


    private fun openLink(link: String) = startActivity(Intent().apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse(link)
    })

    private fun openImage(lowResImage: String?, highResImage: String) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).add(R.id.fragment_container, ImagePreviewFragment.newInstance(lowResImage, highResImage)).commit()
    }

    private fun openVideo(videoUrl: String) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).add(R.id.fragment_container, VideoPreviewFragment.newInstance(videoUrl)).commit()
    }
}