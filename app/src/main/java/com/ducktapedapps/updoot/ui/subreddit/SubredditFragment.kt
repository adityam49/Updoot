package com.ducktapedapps.updoot.ui.subreddit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.MainActivity
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
                    with((requireActivity() as MainActivity)) {
                        SubredditScreen(
                                viewModel = submissionsVM,
                                activityVM = activityVM,
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
                                openUser = ::openUser,
                                openSubreddit = ::openSubreddit,
                        )
                    }
                }
            }
        }
    }
}