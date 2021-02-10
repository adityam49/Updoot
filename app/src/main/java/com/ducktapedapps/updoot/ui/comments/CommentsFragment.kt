package com.ducktapedapps.updoot.ui.comments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.VideoPreviewFragment
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import com.ducktapedapps.updoot.ui.subreddit.PostMedia.*
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentsFragment : Fragment() {
    companion object {
        const val SUBREDDIT_KEY = "subreddit_key"
        const val COMMENTS_KEY = "comments_key"
        fun newInstance(subreddit: String, commentsId: String) = CommentsFragment().apply {
            arguments = Bundle().apply {
                putString(SUBREDDIT_KEY, subreddit)
                putString(COMMENTS_KEY, commentsId)
            }
        }
    }

    private val viewModel: ICommentsVM by viewModels<CommentsVMImpl>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            ComposeView(requireContext()).apply {
                setContent {
                    UpdootTheme {
                        CommentsScreen(
                                viewModel = viewModel,
                                openContent = { content ->
                                    when (content) {
                                        is TextMedia -> Unit
                                        is ImageMedia -> openImage(
                                                lowResImage = content.url,
                                                highResImage = content.url
                                        )
                                        is LinkMedia -> openLink(content.url)
                                        is VideoMedia -> openVideo(content.url)
                                        NoMedia -> Unit
                                    }
                                })
                    }
                }
            }


    private fun openLink(link: String) = startActivity(Intent().apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse(link)
    })

    private fun openImage(lowResImage: String?, highResImage: String) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).add(R.id.fragment_container, ImagePreviewFragment.newInstance(lowResImage, highResImage)).commit()
    }

    private fun openVideo(videoUrl: String) {
        requireActivity().supportFragmentManager.beginTransaction().addToBackStack(null).add(R.id.fragment_container, VideoPreviewFragment.newInstance(videoUrl)).commit()
    }
}