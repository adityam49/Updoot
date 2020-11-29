package com.ducktapedapps.updoot.ui.comments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.ui.VideoPreviewFragment
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.Image
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.*
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    @Inject
    lateinit var sharedPrefs: SharedPreferences
    private val viewModel: CommentsVM by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comment_screen_menu, menu)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            ComposeView(requireContext()).apply {
                setContent {
                    UpdootTheme {
                        CommentsScreen(
                                viewModel = viewModel,
                                openContent = { content ->
                                    when (content) {
                                        is Image -> openImage(
                                                lowResImage = content.data.imageData?.lowResUrl,
                                                highResImage = content.data.imageData?.highResUrl
                                                        ?: ""
                                        )
                                        is LoadedLink -> openLink(content.linkModel.url)
                                        is LoadingLink -> openLink(content.url)
                                        is NoMetaDataLink -> openLink(content.url)
                                        else -> Unit
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