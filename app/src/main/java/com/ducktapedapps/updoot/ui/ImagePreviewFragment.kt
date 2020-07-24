package com.ducktapedapps.updoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.FragmentImagePreviewBinding


class ImagePreviewFragment : DialogFragment() {
    private val args: ImagePreviewFragmentArgs by navArgs()
    private var _binding: FragmentImagePreviewBinding? = null
    private val binding: FragmentImagePreviewBinding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme() = R.style.FullScreenDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide
                .with(this)
                .load(args.mediaUrl)
                .thumbnail(
                        Glide.with(this)
                                .load(args.placeHolderMedia)
                ).into(binding.imageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}