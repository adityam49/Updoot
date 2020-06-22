package com.ducktapedapps.updoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.ImagePreviewFragmentBinding


class MediaPreviewFragment : DialogFragment() {
    private val TAG = "MediaPreviewFragment"

    private val args: MediaPreviewFragmentArgs by navArgs()
    private lateinit var binding: ImagePreviewFragmentBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ImagePreviewFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme() = R.style.FullScreenDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide
                .with(binding.imageView.context)
                .load(args.mediaUrl)
                .thumbnail(
                        Glide.with(binding.imageView.context)
                                .load(args.placeHolderMedia)
                ).into(binding.imageView)
    }
}