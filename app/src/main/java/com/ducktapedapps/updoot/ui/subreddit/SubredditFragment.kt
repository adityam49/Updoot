package com.ducktapedapps.updoot.ui.subreddit

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.view.*
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentSubredditBinding
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.model.Subreddit
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.LoginState.LoggedIn
import com.ducktapedapps.updoot.ui.LoginState.LoggedOut
import com.ducktapedapps.updoot.ui.common.SwipeCallback
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.utils.*
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT
import com.ducktapedapps.updoot.utils.SubmissionUiType.LARGE
import io.noties.markwon.Markwon
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubredditFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: SubmissionsVMFactory

    @Inject
    lateinit var markwon: Markwon

    private val args: SubredditFragmentArgs by navArgs()

    private val activityVM by lazy { ViewModelProvider(requireActivity()).get(ActivityVM::class.java) }
    private val submissionsVM by lazy {
        ViewModelProvider(
                this@SubredditFragment,
                viewModelFactory.apply { setSubreddit(args.subreddit) }
        ).get(SubmissionsVM::class.java)
    }
    private var _binding: FragmentSubredditBinding? = null
    private val binding get() = _binding!!
    private var isLoggedIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
            R.id.side_bar_item -> binding.root.openDrawer(GravityCompat.END)
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
        val submissionsAdapter = SubmissionsAdapter(object : SubmissionsAdapter.SubmissionClickHandler {
            override fun actionOpenComments(linkDataId: String, commentId: String) = openComments(linkDataId, commentId)

            override fun actionOpenOption(linkDataId: String) = openOptions(linkDataId)

            override fun actionOpenImage(lowResUrl: String, highResUrl: String) = openImage(lowResUrl, highResUrl)

            override fun actionOpenLink(link: String) = openLink(link)
        })
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
            sideBar.apply {
                viewTypeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
                    if (isChecked)
                        when (checkedId) {
                            R.id.view_type_card_button -> submissionsVM.setPostViewType(LARGE)
                            R.id.view_type_list_button -> submissionsVM.setPostViewType(COMPACT)
                        }
                }
                controlsBackground.setOnClickListener { expandControls() }
                sideBarInfo.setOnClickListener { expandInfo() }
            }
            root.apply {
                setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                addDrawerListener(object : DrawerLayout.DrawerListener {
                    override fun onDrawerClosed(drawerView: View) =
                            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                    override fun onDrawerOpened(drawerView: View) {
                        setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        setSideBarContent()
                    }

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                        activityVM.setBottomNavDrawerVisibilityRatio(slideOffset)
                        binding.swipeToRefreshLayout
                    }

                    override fun onDrawerStateChanged(newState: Int) = Unit
                })
            }
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
        activityVM.apply {
            shouldReload.observe(viewLifecycleOwner) { shouldReload ->
                if (shouldReload.contentIfNotHandled == true) {
                    Toast.makeText(requireContext(), resources.getString(R.string.reloading), Toast.LENGTH_SHORT).show()
                    reloadFragmentContent()
                }
            }
            loginState.observe(viewLifecycleOwner) {
                isLoggedIn = when (it) {
                    is LoggedOut -> false
                    is LoggedIn -> true
                }
            }
        }
        submissionsVM.apply {
            postViewType.observe(viewLifecycleOwner) { postViewType: SubmissionUiType? ->
                postViewType?.let {
                    submissionsAdapter.itemUi = it
                    binding.apply {
                        recyclerView.apply {
                            adapter = null
                            adapter = submissionsAdapter
                        }
                        sideBar.viewTypeGroup.check(
                                when (it) {
                                    COMPACT -> R.id.view_type_list_button
                                    LARGE -> R.id.view_type_card_button
                                }
                        )
                    }
                }
            }

            allSubmissions.observe(viewLifecycleOwner) { things: List<LinkData> -> submissionsAdapter.submitList(things) }

            toastMessage.observe(viewLifecycleOwner) { toastMessage: SingleLiveEvent<String?> ->
                val toast = toastMessage.contentIfNotHandled
                if (toast != null) Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
            }
            isLoading.observe(viewLifecycleOwner) { binding.swipeToRefreshLayout.isRefreshing = it }

            subredditInfo.observe(viewLifecycleOwner) { subreddit -> subreddit?.let { loadSubredditIconAndTitle(it) } }
        }
    }

    private fun reloadFragmentContent() = submissionsVM.reload()

    private fun openComments(subreddit: String, id: String) = findNavController().navigate(SubredditFragmentDirections.actionGoToComments(subreddit, id))

    private fun openOptions(submissionId: String) = findNavController().navigate(SubredditFragmentDirections.actionSubredditDestinationToSubmissionOptionsBottomSheet(submissionId))

    private fun openLink(link: String) = startActivity(Intent().apply {
        action = ACTION_VIEW
        data = Uri.parse(link)
    })

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

    private fun expandInfo() = updateConstraints(R.layout.side_bar_info)

    private fun expandControls() = updateConstraints(R.layout.side_bar_controls)

    private fun updateConstraints(@LayoutRes id: Int) = ConstraintSet().apply {
        clone(requireContext(), id)
        applyTo(binding.sideBar.root)
        TransitionManager.beginDelayedTransition(binding.sideBar.root)
    }

    private fun loadSubredditIconAndTitle(subreddit: Subreddit) {
        binding.apply {
            val placeHolder = ContextCompat.getDrawable(requireContext(), R.drawable.ic_subreddit_default_24dp)?.apply {
                setTint(ContextCompat.getColor(requireContext(), R.color.color_on_surface))
            }
            Glide.with(this@SubredditFragment)
                    .load(subreddit.community_icon)
                    .placeholder(placeHolder)
                    .error(placeHolder)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.sideBar.subredditIcon)
            binding.sideBar.subredditHeader.text = Truss()
                    .pushSpan(AbsoluteSizeSpan(18, true))
                    .append(subreddit.display_name)
                    .popSpan()
                    .pushSpan(AbsoluteSizeSpan(12, true))
                    .append("\n\n${getCompactCountAsString(subreddit.subscribers)} Users / ${getCompactCountAsString(subreddit.active_user_count)} Active now")
                    .build()
        }
    }

    private fun setSideBarContent() {
        submissionsVM.subredditInfo.observe(viewLifecycleOwner) {
            it?.let {
                binding.apply {
                    if (binding.sideBar.sideBarInfo.text.isNullOrEmpty()) {
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                            val spannedText = markwon.toMarkdown(it.description.replace("(#+)".toRegex(), "$1 "))
                            withContext(Dispatchers.Main) {
                                binding.sideBar.sideBarInfo.apply {
                                    text = spannedText
                                    movementMethod = LinkMovementMethod.getInstance()
                                    delay(50)
                                    expandControls()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}