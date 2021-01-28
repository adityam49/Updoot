package com.ducktapedapps.updoot.ui.subreddit

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.ImageVariants
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.MainActivity
import com.ducktapedapps.updoot.ui.VideoPreviewFragment
import com.ducktapedapps.updoot.ui.comments.CommentsFragment
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import com.ducktapedapps.updoot.ui.subreddit.options.SubmissionOptionsBottomSheet
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import com.ducktapedapps.updoot.utils.Media.*
import dagger.hilt.android.AndroidEntryPoint

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
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
                            openComments = { subreddit, id -> openComments(subreddit, id) },
                            activityVM = activityVM,
                            addAccount = (requireActivity() as MainActivity)::openLoginScreen,
                            openSettings = (requireActivity() as MainActivity)::openSettings,
                            onExit = (requireActivity() as MainActivity)::showExitDialog,
                    )
                }
            }
        }
    }

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