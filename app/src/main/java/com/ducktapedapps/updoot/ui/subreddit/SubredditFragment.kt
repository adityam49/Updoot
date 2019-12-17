package com.ducktapedapps.updoot.ui.subreddit

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentSubredditBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.MediaPreviewFragmentDirections
import com.ducktapedapps.updoot.utils.CustomItemAnimator
import com.ducktapedapps.updoot.utils.InfiniteScrollListener
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.SwipeUtils
import javax.inject.Inject

class SubredditFragment : Fragment() {
    @Inject
    lateinit var appContext: Application

    private lateinit var binding: FragmentSubredditBinding
    private lateinit var submissionsVM: SubmissionsVM
    private lateinit var adapter: SubmissionsAdapter
    private lateinit var navController: NavController
    private val args: SubredditFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as UpdootApplication).updootComponent.inject(this@SubredditFragment)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        navController = findNavController()
        binding = FragmentSubredditBinding.inflate(inflater, container, false)
                .apply { lifecycleOwner = viewLifecycleOwner }
        setUpViewModel()
        setUpRecyclerView()
        return binding.root
    }

    private fun setUpRecyclerView() {
        val recyclerView = binding.recyclerView
        val linearLayoutManager = LinearLayoutManager(this@SubredditFragment.context)
        adapter = SubmissionsAdapter(ClickHandler())

        recyclerView.adapter = adapter
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.itemAnimator = CustomItemAnimator()

        ItemTouchHelper(SwipeUtils(activity, object : SwipeUtils.SwipeActionCallback {
            override fun performSlightLeftSwipeAction(adapterPosition: Int) {
                submissionsVM.castVote(adapterPosition, -1)
            }

            override fun performSlightRightSwipeAction(adapterPosition: Int) {
                submissionsVM.castVote(adapterPosition, 1)
            }

            override fun performLeftSwipeAction(adapterPosition: Int) {
                val data = adapter.currentList[adapterPosition]
                if (submissionsVM.subreddit != data.subredditName) {
                    val action = SubredditFragmentDirections.actionGoToSubreddit().setRSubreddit(data.subredditName)
                    navController.navigate(action)
                }
            }

            override fun performRightSwipeAction(adapterPosition: Int) {
                submissionsVM.save(adapterPosition)
            }
        })).attachToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(InfiniteScrollListener(linearLayoutManager, submissionsVM))
        val swipeRefreshLayout = binding.swipeToRefreshLayout
        swipeRefreshLayout.setColorSchemeResources(
                R.color.DT_primaryColor,
                R.color.secondaryColor,
                R.color.secondaryDarkColor)
        swipeRefreshLayout.setOnRefreshListener { reloadFragmentContent() }
    }

    private fun setUpViewModel() {
        val subreddit = args.rSubreddit ?: ""
        submissionsVM = ViewModelProvider(this@SubredditFragment, SubmissionsVMFactory(appContext, subreddit)).get(SubmissionsVM::class.java)
        binding.submissionViewModel = submissionsVM

        activity?.let {
            val activityVM = ViewModelProvider(it).get(ActivityVM::class.java)
            activityVM.currentAccount.observe(viewLifecycleOwner, Observer { account: SingleLiveEvent<String?>? ->
                if (account?.contentIfNotHandled != null) {
                    reloadFragmentContent()
                    Toast.makeText(this.context, account.peekContent().toString() + " is logged in!", Toast.LENGTH_SHORT).show()
                }
            })
        }

        submissionsVM.allSubmissions.observe(viewLifecycleOwner, Observer { things: List<LinkData>? -> adapter.submitList(things) })
        submissionsVM.toastMessage.observe(viewLifecycleOwner, Observer { toastMessage: SingleLiveEvent<String?> ->
            val toast = toastMessage.contentIfNotHandled
            if (toast != null) Toast.makeText(this.context, toast, Toast.LENGTH_SHORT).show()
        })

    }

    private fun reloadFragmentContent() {
        submissionsVM.reload(null, null)
    }

    inner class ClickHandler {
        fun onClick(linkData: LinkData) {
            val action = SubredditFragmentDirections.actionGoToComments(linkData)
            navController.navigate(action)
        }

        fun handleImagePreview(data: LinkData) {
            navController.navigate(
                    MediaPreviewFragmentDirections.actionGlobalMediaPreviewFragment(
                            data.preview!!.images[0].source.url
                    )
            )
        }

        fun handleExpansion(index: Int) {
            submissionsVM.expandSelfText(index)
        }
    }


}