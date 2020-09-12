package com.ducktapedapps.updoot.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentVideoPreviewBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class VideoPreviewFragment : Fragment() {

    companion object {
        private const val KEY_VIDEO_URL = "key_video_url"
        fun newInstance(videoUrl: String) = VideoPreviewFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_VIDEO_URL, videoUrl)
            }
        }
    }

    private val playBackListener = object : EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                STATE_BUFFERING -> {
                    binding.progressCircular.visibility = VISIBLE
                }
                STATE_ENDED -> binding.playerView.showController()
                STATE_READY -> {
                    binding.apply {
                        playerView.visibility = VISIBLE
                        progressCircular.visibility = GONE
                    }
                }
                else -> Unit
            }
        }
    }

    private var _binding: FragmentVideoPreviewBinding? = null
    private val binding: FragmentVideoPreviewBinding
        get() = _binding!!

    @Inject
    lateinit var exoPlayer: ExoPlayer

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity?.application as UpdootApplication).updootComponent.inject(this)
        exoPlayer.apply {
            playWhenReady = true
            prepare(getMediaSource())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVideoPreviewBinding.inflate(inflater, container, false)
        exoPlayer.addListener(playBackListener)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            scrimView.setOnClickListener { }
            playerView.apply {
                player = exoPlayer
            }
        }
    }

    private fun getMediaSource(): MediaSource {
        val url = requireArguments().getString(KEY_VIDEO_URL) ?: ""
        Toast.makeText(requireContext(), "video : $url", Toast.LENGTH_LONG).show()
        val dataSourceFactory = DefaultDataSourceFactory(this.context, resources.getString(R.string.app_name))
        val uri = Uri.parse(url)
        return when {
            uri.authority?.contains("redd") == true -> DashMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(uri)
            uri.authority?.contains("imgur") == true -> {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri)
            }
            else -> {
                Toast.makeText(requireContext(), "${uri.authority} not supported yet!", Toast.LENGTH_SHORT).show()
                throw Exception("unknown url! $uri")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.apply {
            ContextCompat.getColor(requireContext(), R.color.color_scrim).let {
                statusBarColor = it
                navigationBarColor = it
            }
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.apply {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.color_status_bar)
            navigationBarColor = ContextCompat.getColor(requireContext(), R.color.nav_bar_color)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.playerView.player = null
        exoPlayer.removeListener(playBackListener)
        _binding = null
        exoPlayer.stop()
    }
}


