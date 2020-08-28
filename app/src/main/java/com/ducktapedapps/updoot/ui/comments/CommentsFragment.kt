package com.ducktapedapps.updoot.ui.comments

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentCommentsBinding
import com.ducktapedapps.updoot.ui.common.SwipeCallback
import io.noties.markwon.Markwon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@FlowPreview
class CommentsFragment : Fragment() {
    companion object {
        private const val SUBREDDIT_KEY = "subreddit_key"
        private const val COMMENTS_KEY = "comments_key"
        fun newInstance(subreddit: String, commentsId: String) = CommentsFragment().apply {
            arguments = Bundle().apply {
                putString(SUBREDDIT_KEY, subreddit)
                putString(COMMENTS_KEY, commentsId)
            }
        }
    }

    @Inject
    @Named("Prefs")
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var commentsVMFactory: CommentsVMFactory
    private lateinit var viewModel: CommentsVM

    private var _binding: FragmentCommentsBinding? = null
    private val binding: FragmentCommentsBinding
        get() = _binding!!

    @Inject
    lateinit var markwon: Markwon

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity?.application as UpdootApplication).updootComponent.inject(this@CommentsFragment)
        with(requireArguments()) {
            setUpViewModel(getString(SUBREDDIT_KEY, null)!!, getString(COMMENTS_KEY, null)!!)
        }
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
        super.onViewCreated(view, savedInstanceState)
        val contentAdapter = ContentAdapter(markwon)
        val commentsAdapter = CommentsAdapter(
                ::expandCollapseComment,
                sharedPrefs.getBoolean(getString(R.string.comment_thread_indicator_count_key), true),
                sharedPrefs.getBoolean(getString(R.string.comment_thread_indicator_color_key), true)
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

    private fun setUpViewModel(name: String, id: String) {
        viewModel = ViewModelProvider(
                this@CommentsFragment,
                commentsVMFactory.apply { setSubredditAndId(name, id) }
        ).get(CommentsVM::class.java)
    }

    private fun observeData(submissionHeaderAdapter: SubmissionMetaDataAdapter, contentAdapter: ContentAdapter, commentsAdapter: CommentsAdapter) = viewModel.apply {
        allComments.observe(viewLifecycleOwner) { commentsAdapter.submitList(it) }
        isLoading.observe(viewLifecycleOwner) { binding.swipeToRefreshLayout.isRefreshing = it }
        submissionData.observe(viewLifecycleOwner) { submissionHeaderAdapter.linkData = it }
        content.observe(viewLifecycleOwner) { contentAdapter.content = it }
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

    private fun expandCollapseComment(index: Int) = viewModel.toggleChildrenVisibility(index)
}