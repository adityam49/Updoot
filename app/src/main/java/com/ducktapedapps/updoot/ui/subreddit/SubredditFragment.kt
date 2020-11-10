package com.ducktapedapps.updoot.ui.subreddit

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.data.local.model.ImageVariants
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.User.LoggedIn
import com.ducktapedapps.updoot.ui.User.LoggedOut
import com.ducktapedapps.updoot.ui.VideoPreviewFragment
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import com.ducktapedapps.updoot.ui.subreddit.SubredditSorting.*
import com.ducktapedapps.updoot.ui.subreddit.options.SubmissionOptionsBottomSheet
import com.ducktapedapps.updoot.ui.subreddit.options.SubredditScreen
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import com.ducktapedapps.updoot.utils.Media.*
import com.ducktapedapps.updoot.utils.SingleLiveEvent
import javax.inject.Inject

class SubredditFragment : Fragment() {
    companion object {
        private const val SUBREDDIT_KEY = "subreddit_key"
        fun newInstance(subreddit: String) = SubredditFragment().apply {
            arguments = Bundle().apply { putString(SUBREDDIT_KEY, subreddit) }
        }
    }

    @Inject
    lateinit var viewModelFactory: SubmissionsVMFactory

    private val activityVM by lazy { ViewModelProvider(requireActivity()).get(ActivityVM::class.java) }
    private val submissionsVM by lazy {
        ViewModelProvider(
                this@SubredditFragment,
                viewModelFactory.apply { setSubreddit(requireArguments().getString(SUBREDDIT_KEY)!!) }
        ).get(SubmissionsVM::class.java)
    }
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
            R.id.side_bar_item -> Unit
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

    @ExperimentalLazyDsl
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            ComposeView(requireContext()).apply {
                setContent {
                    UpdootTheme {
                        SubredditScreen(
                                viewModel = submissionsVM,
                                openOptions = { id: String -> openOptions(id) },
                                openMedia = { media ->
                                    when (media) {
                                        is SelfText -> Unit
                                        is Image -> openImage(media.imageData)
                                        is Video -> openVideo(media.url)
                                        is Link -> openLink(media.url)
                                        JustTitle -> Unit
                                    }
                                },
                                openComments = { subreddit, id -> openComments(subreddit, id) }
                        )
                    }
                }
            }


    private fun observeViewModel() {
        activityVM.apply {
            shouldReload.asLiveData().observe(viewLifecycleOwner, { shouldReload ->
                if (shouldReload.contentIfNotHandled == true) {
                    Toast.makeText(requireContext(), resources.getString(R.string.reloading), Toast.LENGTH_SHORT).show()
                    reloadFragmentContent()
                }
            })
            user.asLiveData().observe(viewLifecycleOwner, {
                isLoggedIn = when (it) {
                    is LoggedOut -> false
                    is LoggedIn -> true
                }
            })
        }
        submissionsVM.apply {
            toastMessage.asLiveData().observe(viewLifecycleOwner, { toastMessage: SingleLiveEvent<String?> ->
                val toast = toastMessage.contentIfNotHandled
                if (toast != null) Toast.makeText(requireContext(), toast, Toast.LENGTH_SHORT).show()
            }
            )

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

    private fun openImage(preview: ImageVariants?) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).add(R.id.fragment_container, ImagePreviewFragment.newInstance(preview?.lowResUrl, preview?.highResUrl!!)).commit()
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
}