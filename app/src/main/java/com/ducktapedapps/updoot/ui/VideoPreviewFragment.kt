package com.ducktapedapps.updoot.ui

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentVideoPreviewBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import javax.inject.Inject

class VideoPreviewFragment : DialogFragment() {
    private val args: VideoPreviewFragmentArgs by navArgs()
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
        return binding.root
    }

    override fun getTheme() = R.style.FullScreenDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            playerView.apply {
                player = exoPlayer
            }
        }
    }

    private fun getMediaSource(): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(this.context, resources.getString(R.string.app_name))
        return DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(args.videoUrl))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.playerView.player = null
        _binding = null
        exoPlayer.stop()
    }
}


