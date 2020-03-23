package com.ducktapedapps.updoot.ui.subreddit

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentSubredditBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.utils.*
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import javax.inject.Inject


private const val TAG = "SubredditFragment"

class SubredditFragment : Fragment() {
    @Inject
    lateinit var appContext: Application

    private lateinit var submissionsVM: SubmissionsVM
    private val args: SubredditFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as UpdootApplication).updootComponent.inject(this@SubredditFragment)
        submissionsVM = ViewModelProvider(this@SubredditFragment,
                SubmissionsVMFactory(args.rSubreddit ?: FRONTPAGE, appContext as UpdootApplication)
        ).get(SubmissionsVM::class.java)
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

        binding.apply {
            subredditQas.sortButton.setOnClickListener { showMenuFor(requireContext(), it, submissionsVM) }
            subredditQas.viewModeButton.setOnClickListener { submissionsVM.toggleUi() }
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
            itemAnimator = CustomItemAnimator()

            ItemTouchHelper(SwipeUtils(activity, object : SwipeUtils.SwipeActionCallback {
                override fun performSlightLeftSwipeAction(adapterPosition: Int) = submissionsVM.castVote(adapterPosition, -1)


                override fun performSlightRightSwipeAction(adapterPosition: Int) = submissionsVM.castVote(adapterPosition, 1)


                override fun performLeftSwipeAction(adapterPosition: Int) =
                        showMenuFor(args.rSubreddit,
                                adapter.currentList[adapterPosition],
                                this@SubredditFragment.requireContext(),
                                binding.recyclerView.findViewHolderForAdapterPosition(adapterPosition)?.itemView,
                                findNavController())

                override fun performRightSwipeAction(adapterPosition: Int) {
                    submissionsVM.toggleSave(adapterPosition)
                }
            })).attachToRecyclerView(this)

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
            uiType.observe(viewLifecycleOwner, Observer { it: SubmissionUiType ->
                adapter.itemUi = it
                binding.recyclerView.adapter = null
                binding.recyclerView.adapter = adapter
            })

            allSubmissions.observe(viewLifecycleOwner, Observer { things: List<LinkData>? -> adapter.submitList(things) })

            toastMessage.observe(viewLifecycleOwner, Observer { toastMessage: SingleLiveEvent<String?> ->
                val toast = toastMessage.contentIfNotHandled
                if (toast != null) Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
            })

            isLoading.observe(viewLifecycleOwner, Observer {
                Log.i(TAG, "new loading value  $it")
                //Hack for motionLayout visibility
                if (binding.root is MotionLayout) {
                    val layout = binding.progressBar.parent as MotionLayout
                    val setToVisibility = if (it) View.VISIBLE else View.GONE
                    for (constraintId in layout.constraintSetIds) {
                        layout.getConstraintSet(constraintId)?.setVisibility(R.id.progressBar, setToVisibility)
                    }
                }
            })
        }
    }

    private fun reloadFragmentContent() = submissionsVM.reload()
}

@BindingAdapter("subreddit_header_icon")
fun bindIcon(view: ImageView, url: String?) =
        Glide.with(view)
                .load(url)
                .placeholder(R.drawable.ic_explore_black_24dp)
                .apply(RequestOptions.circleCropTransform())
                .override(128, 128)
                .into(view)

@BindingAdapter("onlineCount", "subscriberCount")
fun bindUserCount(view: TextView, onlineCount: Long, subscriberCount: Long) {
    view.text = String.format("%s Online / %s Subscribers", getCompactCountAsString(onlineCount), getCompactCountAsString(subscriberCount))
}

