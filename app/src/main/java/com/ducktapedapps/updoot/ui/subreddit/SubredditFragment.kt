package com.ducktapedapps.updoot.ui.subreddit

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannedString
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.databinding.FragmentSubredditBinding
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.VideoPreviewFragment
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.common.InfiniteScrollListener
import com.ducktapedapps.updoot.ui.common.ScrollPositionListener
import com.ducktapedapps.updoot.ui.common.SwipeCallback
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.ui.subreddit.options.SubmissionOptionsBottomSheet
import com.ducktapedapps.updoot.utils.SubmissionUiType.COMPACT
import com.ducktapedapps.updoot.utils.SubmissionUiType.LARGE
import com.ducktapedapps.updoot.utils.Truss
import com.ducktapedapps.updoot.utils.getCompactCountAsString
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.Markwon
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SubredditFragment : Fragment() {
    companion object {
        const val SUBREDDIT_KEY = "subreddit_key"
        fun newInstance(subreddit: String) = SubredditFragment().apply {
            arguments = Bundle().apply { putString(SUBREDDIT_KEY, subreddit) }
        }
    }

    private val activityVM: ActivityVM by activityViewModels()
    private val submissionsVM: SubmissionsVM by viewModels()

    @Inject
    lateinit var markwon: Markwon


    private var _binding: FragmentSubredditBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSubredditBinding.inflate(inflater, container, false)
        val submissionsAdapter = SubmissionsAdapter(object : SubmissionsAdapter.SubmissionClickHandler {
            override fun actionOpenComments(linkDataId: String, commentId: String) = openComments(linkDataId, commentId)

            override fun actionOpenOption(linkDataId: String) = openOptions(linkDataId)

            override fun actionOpenImage(lowResUrl: String, highResUrl: String) = openImage(lowResUrl, highResUrl)

            override fun actionOpenLink(link: String) = openLink(link)

            override fun actionOpenVideo(videoUrl: String) = openVideo(videoUrl)
        }).apply { stateRestorationPolicy = PREVENT_WHEN_EMPTY }
        observeViewModel(submissionsAdapter)
        setUpViews(submissionsAdapter)
        return binding.root
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
                    override fun onDrawerClosed(drawerView: View) {
                        setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }

                    override fun onDrawerOpened(drawerView: View) {
                        setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        setSideBarContent()
                    }

                    override fun onDrawerSlide(drawerView: View, slideOffset: Float) = Unit

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
                            override fun onExtremeLeftSwipe(swipedThingData: String?) = openSubreddit(swipedThingData!!)
                            override fun onLeftSwipe(swipedThingData: String?) = submissionsVM.downVote(swipedThingData!!)
                            override fun onRightSwipe(swipedThingData: String?) = submissionsVM.upVote(swipedThingData!!)
                            override fun onExtremeRightSwipe(swipedThingData: String?) = submissionsVM.save(swipedThingData!!)
                        }
                )).attachToRecyclerView(this)
                addOnScrollListener(InfiniteScrollListener(linearLayoutManager, submissionsVM))
                addOnScrollListener(ScrollPositionListener(linearLayoutManager, submissionsVM))
            }
        }
    }

    private fun getColor(@ColorRes color: Int): Int = ContextCompat.getColor(requireContext(), color)

    private fun getDrawable(@DrawableRes drawableRes: Int): Drawable? = ContextCompat.getDrawable(requireContext(), drawableRes)

    private fun observeViewModel(submissionsAdapter: SubmissionsAdapter) {
        activityVM.apply {
            shouldReload.filter { it }
                    .onEach {
                        submissionsVM.reload()
                    }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
        submissionsVM.apply {
            postViewType.onEach {
                submissionsAdapter.itemUi = it
                binding.apply {
                    recyclerView.adapter = null
                    recyclerView.adapter = submissionsAdapter
                    recyclerView.scrollToPosition(lastScrollPosition)
                    sideBar.viewTypeGroup.check(
                            when (it) {
                                COMPACT -> R.id.view_type_list_button
                                LARGE -> R.id.view_type_card_button
                            }
                    )
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)
            feedPages.onEach {
                submissionsAdapter.submitList(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
            toastMessage.onEach {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }.launchIn(viewLifecycleOwner.lifecycleScope)
            isLoading.onEach {
                binding.swipeToRefreshLayout.isRefreshing = it
            }.launchIn(viewLifecycleOwner.lifecycleScope)
            subredditInfo.filterNotNull().onEach {
                loadSubredditIconAndTitle(it)
            }.launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    private fun reloadFragmentContent() = submissionsVM.reload()

    private fun openComments(subreddit: String, id: String) {
        requireActivity().supportFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.fragment_container, CommentsFragment.newInstance(subreddit, id))
                .commit()
    }

    private fun openOptions(submissionId: String) {
        SubmissionOptionsBottomSheet.newInstance(submissionId).show(requireActivity().supportFragmentManager, null)
    }

    private fun openLink(link: String) = startActivity(Intent().apply {
        action = ACTION_VIEW
        data = Uri.parse(link)
    })

    private fun openImage(lowResImage: String, highResImage: String) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).add(R.id.fragment_container, ImagePreviewFragment.newInstance(lowResImage, highResImage)).commit()
    }

    private fun openVideo(videoUrl: String) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).add(R.id.fragment_container, VideoPreviewFragment.newInstance(videoUrl)).commit()
    }

    private fun openSubreddit(targetSubreddit: String) {
        if (targetSubreddit != requireArguments().getString(SUBREDDIT_KEY))
            requireActivity()
                    .supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                    .replace(R.id.fragment_container, newInstance(targetSubreddit))
                    .commit()
    }

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
                    .append("\n\n${getCompactCountAsString(subreddit.subscribers ?: 0)} Users / ${getCompactCountAsString(subreddit.active_user_count ?: 0)} Active now")
                    .build()
        }
    }

    private fun setSideBarContent() {
        submissionsVM.subredditInfo.onEach {
            it?.let {
                binding.apply {
                    if (binding.sideBar.sideBarInfo.text.isNullOrEmpty()) {
                        withContext(Dispatchers.Default) {
                            val spannedText = markwon.toMarkdown(it.description?.replace("(#+)".toRegex(), "$1 ")
                                    ?: "")
                            withContext(Dispatchers.Main) {
                                binding.sideBar.sideBarInfo.apply {
                                    text = spannedText
                                    movementMethod = LinkMovementMethod.getInstance()
                                    delay(50)
                                    expandControls()
                                }
                                binding.sideBar.sideBarInfo.text = SpannedString(binding.sideBar.sideBarInfo.text)
                            }
                        }
                    }
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }
}