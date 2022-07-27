package com.ducktapedapps.updoot

import androidx.fragment.app.Fragment

class VideoPreviewFragment : Fragment() {

//    companion object {
//        private const val KEY_VIDEO_URL = "key_video_url"
//        fun newInstance(videoUrl: String) = VideoPreviewFragment().apply {
//            arguments = Bundle().apply {
//                putString(KEY_VIDEO_URL, videoUrl)
//            }
//        }
//    }
//
//    private val playBackListener = object : Listener {
//        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
//            when (playbackState) {
//                STATE_BUFFERING -> {
//                    binding.progressCircular.visibility = VISIBLE
//                }
//                STATE_ENDED -> binding.playerView.showController()
//                STATE_READY -> {
//                    binding.apply {
//                        playerView.visibility = VISIBLE
//                        progressCircular.visibility = GONE
//                    }
//                }
//                else -> Unit
//            }
//        }
//    }
//
//
//    @Inject
//    lateinit var exoPlayer: ExoPlayer
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        exoPlayer.apply {
//            playWhenReady = true
//            setMediaSource(getMediaSource())
//            prepare()
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentVideoPreviewBinding.inflate(inflater, container, false)
//        exoPlayer.addListener(playBackListener)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.apply {
//            scrimView.setOnClickListener { }
//            playerView.apply {
//                player = exoPlayer
//            }
//        }
//    }
//
//    private fun getMediaSource(): MediaSource {
//        val dataSourceFactory =
//            DefaultDataSourceFactory(requireContext(), resources.getString(R.string.app_name))
//        val uri = Uri.parse(requireArguments().getString(KEY_VIDEO_URL))
//        return when {
//
//            uri.authority?.contains("redd") == true -> DashMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(MediaItem.fromUri(uri))
//            uri.authority?.contains("imgur") == true -> {
//                ProgressiveMediaSource.Factory(dataSourceFactory)
//                    .createMediaSource(MediaItem.fromUri(uri))
//            }
//            else -> {
//                Toast.makeText(
//                    requireContext(),
//                    "${uri.authority} not supported yet!",
//                    Toast.LENGTH_SHORT
//                ).show()
//                throw Exception("unknown url! $uri")
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding.playerView.player = null
//        exoPlayer.removeListener(playBackListener)
//        _binding = null
//        exoPlayer.stop()
//    }
}


