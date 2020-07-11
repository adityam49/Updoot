package com.ducktapedapps.updoot.ui.subreddit

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentSubredditBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.LoginState
import com.ducktapedapps.updoot.ui.common.SwipeCallback
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.utils.InfiniteScrollListener
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import com.ducktapedapps.updoot.utils.SubmissionUiType
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import javax.inject.Inject

class SubredditFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: SubmissionsVMFactory
    private val args: SubredditFragmentArgs by navArgs()

    private lateinit var submissionsVM: SubmissionsVM
    private var _binding: FragmentSubredditBinding? = null
    private val binding get() = _binding!!
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        setUpViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity?.application as UpdootApplication).updootComponent.inject(this@SubredditFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.subreddit_screen_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.view_type_item -> submissionsVM.toggleUi()
            R.id.hot_item -> submissionsVM.changeSort(Hot)
            R.id.rising_item -> submissionsVM.changeSort(Rising)
            R.id.new_item -> submissionsVM.changeSort(New)
            R.id.best_item -> submissionsVM.changeSort(Best)
            R.id.top_hour_item -> submissionsVM.changeSort(TopHour)
            R.id.top_day_item -> submissionsVM.changeSort(TopDay)
            R.id.top_week_item -> submissionsVM.changeSort(TopWeek)
            R.id.top_month_item -> submissionsVM.changeSort(TopMonth)
            R.id.top_year_item -> submissionsVM.changeSort(TopYear)
            R.id.top_all_time_item -> submissionsVM.changeSort(TopAll)
            R.id.controversial_hour_item -> submissionsVM.changeSort(ControversialHour)
            R.id.controversial_day_item -> submissionsVM.changeSort(ControversialDay)
            R.id.controversial_week_item -> submissionsVM.changeSort(ControversialWeek)
            R.id.controversial_month_item -> submissionsVM.changeSort(ControversialMonth)
            R.id.controversial_year_item -> submissionsVM.changeSort(ControversialYear)
            R.id.controversial_all_time_item -> submissionsVM.changeSort(ControversialAll)
            R.id.search_item -> findNavController().navigate(SubredditFragmentDirections.actionSubredditDestinationToSearchOverlayDestination())
            else -> return false
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentSubredditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val submissionsAdapter = SubmissionsAdapter(::openComments, ::openOptions, ::openImage)
        setUpViews(submissionsAdapter)
        observeViewModel(submissionsAdapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpViews(submissionsAdapter: SubmissionsAdapter) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.apply {
            swipeToRefreshLayout.setOnRefreshListener { reloadFragmentContent() }
            recyclerView.apply {
                adapter = submissionsAdapter
                layoutManager = linearLayoutManager
                itemAnimator = SlideInUpAnimator()
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
                            override fun extremeLeftAction(position: Int) =
                                    Toast.makeText(requireContext(), if (isLoggedIn) "can save" else "please login", Toast.LENGTH_SHORT).show()

                            override fun leftAction(position: Int) =
                                    Toast.makeText(requireContext(), if (isLoggedIn) "can upvote" else "please login", Toast.LENGTH_SHORT).show()

                            override fun rightAction(position: Int) =
                                    Toast.makeText(requireContext(), if (isLoggedIn) "can downvote" else "please login", Toast.LENGTH_SHORT).show()

                            override fun extremeRightAction(position: Int) = openSubreddit(submissionsAdapter.currentList[position].subredditName)
                        }
                )).attachToRecyclerView(this)
                addOnScrollListener(InfiniteScrollListener(linearLayoutManager, submissionsVM))
            }
        }
    }

    private fun getColor(@ColorRes color: Int): Int = ContextCompat.getColor(requireContext(), color)

    private fun getDrawable(@DrawableRes drawableRes: Int): Drawable? = ContextCompat.getDrawable(requireContext(), drawableRes)

    private fun observeViewModel(submissionsAdapter: SubmissionsAdapter) {
        ViewModelProvider(requireActivity()).get(ActivityVM::class.java).apply {
            shouldReload.observe(viewLifecycleOwner) { shouldReload ->
                if (shouldReload.contentIfNotHandled == true) {
                    Toast.makeText(requireContext(), resources.getString(R.string.reloading), Toast.LENGTH_SHORT).show()
                    reloadFragmentContent()
                }
            }
            loginState.observe(viewLifecycleOwner) {
                isLoggedIn = when (it) {
                    LoginState.LoggedOut -> false
                    is LoginState.LoggedIn -> true
                }
            }
        }
        submissionsVM.apply {
            postViewType.observe(viewLifecycleOwner) { postViewType: SubmissionUiType? ->
                postViewType?.let {
                    submissionsAdapter.itemUi = it
                    binding.recyclerView.apply {
                        adapter = null
                        adapter = submissionsAdapter
                    }
                }
            }

            allSubmissions.observe(viewLifecycleOwner) { things: List<LinkData> -> submissionsAdapter.submitList(things) }

            toastMessage.observe(viewLifecycleOwner) { toastMessage: SingleLiveEvent<String?> ->
                val toast = toastMessage.contentIfNotHandled
                if (toast != null) Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
            }
            isLoading.observe(viewLifecycleOwner) { binding.swipeToRefreshLayout.isRefreshing = it }
        }
    }

    private fun setUpViewModel() {
        submissionsVM = ViewModelProvider(
                this@SubredditFragment,
                viewModelFactory.apply { setSubreddit(args.subreddit) }
        ).get(SubmissionsVM::class.java)
    }

    private fun reloadFragmentContent() = submissionsVM.reload()

    private fun openComments(subreddit: String, id: String) = findNavController().navigate(SubredditFragmentDirections.actionGoToComments(subreddit, id))

    private fun openOptions(submissionId: String) = findNavController().navigate(SubredditFragmentDirections.actionSubredditDestinationToSubmissionOptionsBottomSheet(submissionId))

    private fun openImage(lowResImage: String, highResImage: String) = findNavController().navigate(
            SubredditFragmentDirections.actionSubredditDestinationToImagePreviewDestination(lowResImage, highResImage)
    )

    private fun openSubreddit(targetSubreddit: String) = if (args.subreddit != targetSubreddit) {
        findNavController().navigate(
                SubredditFragmentDirections
                        .actionGoToSubreddit()
                        .setSubreddit(targetSubreddit)
        )
        Toast.makeText(requireContext(), targetSubreddit, Toast.LENGTH_SHORT).show()
    } else Toast.makeText(requireContext(), "You are already in $targetSubreddit", Toast.LENGTH_SHORT).show()

    private companion object {
        const val TAG = "SubredditFragment"
    }
}