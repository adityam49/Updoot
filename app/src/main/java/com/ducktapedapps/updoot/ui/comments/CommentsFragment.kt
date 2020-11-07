package com.ducktapedapps.updoot.ui.comments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.ui.VideoPreviewFragment
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.Image
import com.ducktapedapps.updoot.ui.comments.SubmissionContent.LinkState.*
import com.ducktapedapps.updoot.ui.imagePreview.ImagePreviewFragment
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import javax.inject.Inject

class CommentsFragment : Fragment() {
    companion object {
        private const val SUBREDDIT_KEY = "subreddit_key"
        private const val COMMENTS_KEY = "comments_key"
        fun newInstance(subreddit: String, commentsId: String) = CommentsFragment().apply {
            arguments = Bundle().apply {
                putString(SUBREDDIT_KEY, subreddit)
                putString(COMMENTS_KEY, commentsId)
            }
        }
    }

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @Inject
    lateinit var commentsVMFactory: CommentsVMFactory
    private val viewModel: CommentsVM by lazy {
        with(requireArguments()) {
            setUpViewModel(getString(SUBREDDIT_KEY, null)!!, getString(COMMENTS_KEY, null)!!)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity?.application as UpdootApplication).updootComponent.inject(this@CommentsFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.comment_screen_menu, menu)
    }

    @ExperimentalLazyDsl
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            ComposeView(requireContext()).apply {
                setContent {
                    UpdootTheme {
                        Surface(color = MaterialTheme.colors.background) {
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
            }


    private fun setUpViewModel(name: String, id: String): CommentsVM =
            ViewModelProvider(
                    this,
                    commentsVMFactory.apply { setSubredditAndId(name, id) }
            ).get(CommentsVM::class.java)


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