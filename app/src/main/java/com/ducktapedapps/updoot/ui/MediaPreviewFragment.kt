package com.ducktapedapps.updoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.ducktapedapps.updoot.databinding.ImagePreviewFragmentBinding


const val TAG = "MediaPreviewFragment"

class MediaPreviewFragment : Fragment() {

    private val args: MediaPreviewFragmentArgs by navArgs()
    private lateinit var binding: ImagePreviewFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ImagePreviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageView.transitionName = args.placeHolderMedia
        Glide
                .with(binding.imageView.context)
                .load(args.mediaUrl)
                .thumbnail(
                        Glide.with(binding.imageView.context)
                                .load(args.placeHolderMedia)
                ).into(binding.imageView)
    }
}