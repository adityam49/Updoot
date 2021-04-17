package com.ducktapedapps.updoot.subreddit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.ducktapedapps.updoot.ActivityVM
import com.ducktapedapps.updoot.MainActivity
import com.ducktapedapps.updoot.subreddit.PostMedia.*
import com.ducktapedapps.updoot.theme.UpdootTheme
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
    private val subredditVM: SubredditVM by viewModels<SubredditVMImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            UpdootTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    with((requireActivity() as MainActivity)) {
                        SubredditScreen(
                            viewModel = subredditVM,
                            activityVM = activityVM,
                            openMedia = {
                                when (it) {
                                    is TextMedia -> Unit
                                    is ImageMedia -> openImage(it)
                                    is LinkMedia -> openLink(it.url)
                                    is VideoMedia -> openVideo(it.url)
                                    NoMedia -> Unit
                                }
                            },
                            openComments = ::openComments,
                            openUser = ::openUser,
                            openSubreddit = ::openSubreddit,
                        )
                    }
                }
            }
        }
    }
}