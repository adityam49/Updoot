package com.ducktapedapps.updoot.ui.subreddit

import android.app.Application
import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentSubredditBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.common.SwipeCallback
import com.ducktapedapps.updoot.utils.*
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.utils.Sorting.*
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import javax.inject.Inject

class SubredditFragment : Fragment() {
    @Inject
    lateinit var appContext: Application

    private lateinit var submissionsVM: SubmissionsVM
    private val args: SubredditFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (activity?.application as UpdootApplication).updootComponent.inject(this@SubredditFragment)
        submissionsVM = ViewModelProvider(this@SubredditFragment,
                SubmissionsVMFactory(args.rSubreddit ?: FRONTPAGE, appContext as UpdootApplication)
        ).get(SubmissionsVM::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.subreddit_screen_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.hot_item -> submissionsVM.changeSort(HOT)
            R.id.rising_item -> submissionsVM.changeSort(RISING)
            R.id.new_item -> submissionsVM.changeSort(NEW)
            R.id.best_item -> submissionsVM.changeSort(BEST)
            R.id.view_type_item -> submissionsVM.toggleUi()
            else -> return false
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSubredditBinding.inflate(inflater, container, false)
                .apply { lifecycleOwner = viewLifecycleOwner }

        val adapter = SubmissionsAdapter()

        adapter.submissionClickListener = object : SubmissionsAdapter.SubmissionClickListener {
            override fun onSubmissionClick(linkData: LinkData) = findNavController().navigate(SubredditFragmentDirections.actionGoToComments(linkData))
            override fun onThumbnailClick(imageView: View, linkData: LinkData) {
                val extra = FragmentNavigatorExtras(imageView as ImageView to imageView.transitionName)
                findNavController().navigate(
                        SubredditFragmentDirections.actionSubredditDestinationToImagePreviewDestination(linkData.thumbnail, linkData.preview!!.images[0].source.url),
                        extra
                )
            }

            override fun handleExpansion(index: Int) = submissionsVM.expandSelfText(index)
        }

        setUpVMWithViews(binding, adapter)
        setUpRecyclerView(binding, adapter)
        return binding.root
    }

    private fun setUpRecyclerView(binding: FragmentSubredditBinding, adapter: SubmissionsAdapter) {
        val linearLayoutManager = LinearLayoutManager(this@SubredditFragment.context)

        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = linearLayoutManager
            itemAnimator = SlideInUpAnimator(OvershootInterpolator(1f))

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
                        override fun extremeLeftAction(position: Int) = submissionsVM.toggleSave(position)

                        override fun leftAction(position: Int) = submissionsVM.castVote(position, 1)

                        override fun rightAction(position: Int) = submissionsVM.castVote(position, -1)

                        override fun extremeRightAction(position: Int) =
                                showMenuFor(args.rSubreddit,
                                        adapter.currentList[position],
                                        this@SubredditFragment.requireContext(),
                                        binding.recyclerView.findViewHolderForAdapterPosition(position)?.itemView,
                                        findNavController()
                                )
                    }
            )).attachToRecyclerView(this)
            addOnScrollListener(InfiniteScrollListener(linearLayoutManager, submissionsVM))
        }
    }

    private fun setUpVMWithViews(binding: FragmentSubredditBinding, adapter: SubmissionsAdapter) {
        binding.vm = submissionsVM
        binding.swipeToRefreshLayout.setOnRefreshListener { submissionsVM.reload() }
        activity?.let {
            val activityVM = ViewModelProvider(it).get(ActivityVM::class.java)
            activityVM.currentAccount.observe(viewLifecycleOwner, Observer { account: SingleLiveEvent<String?>? ->
                if ((account?.peekContent() == Constants.ANON_USER && account.contentIfNotHandled != null) || account?.contentIfNotHandled != null) {
                    reloadFragmentContent()
                    Toast.makeText(this.context, account.peekContent().toString() + " is logged in!", Toast.LENGTH_SHORT).show()
                }
            })
        }
        submissionsVM.apply {
            uiType.observe(viewLifecycleOwner) {
                adapter.itemUi = it
                binding.recyclerView.adapter = null
                binding.recyclerView.adapter = adapter
            }

            allSubmissions.observe(viewLifecycleOwner) { things: List<LinkData>? -> adapter.submitList(things) }

            toastMessage.observe(viewLifecycleOwner) { toastMessage: SingleLiveEvent<String?> ->
                val toast = toastMessage.contentIfNotHandled
                if (toast != null) Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
            }
            isLoading.observe(viewLifecycleOwner) { binding.swipeToRefreshLayout.isRefreshing = it }
        }
    }

    private fun reloadFragmentContent() = submissionsVM.reload()

    private companion object {
        const val TAG = "SubredditFragment"
    }
}