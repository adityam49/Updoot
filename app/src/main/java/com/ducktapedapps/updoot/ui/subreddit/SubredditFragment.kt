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
    private val subredditVM: ISubredditVM by viewModels<SubredditVMImpl>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                UpdootTheme {
                    with((requireActivity() as MainActivity)) {
                        SubredditScreen(
                                viewModel = subredditVM,
                                activityVM = activityVM,
                                openMedia = { media ->
                                    when (media) {
                                        is PostMedia.TextMedia -> Unit
                                        is PostMedia.ImageMedia -> openImage(media)
                                        is PostMedia.LinkMedia -> openLink(media.url)
                                        is PostMedia.VideoMedia -> openVideo(media.url)
                                        PostMedia.NoMedia -> Unit
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