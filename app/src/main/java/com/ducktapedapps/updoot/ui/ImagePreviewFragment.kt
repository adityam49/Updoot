package com.ducktapedapps.updoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.FragmentImagePreviewBinding


class ImagePreviewFragment : Fragment() {
    companion object {
        private const val IMAGE_URL_KEY = "image_url_key"
        private const val PLACEHOLDER_URL_KEY = "place_holder_key"
        fun newInstance(placeHolderUrl: String?, imageUrl: String) = ImagePreviewFragment().apply {
            arguments = Bundle().apply {
                putString(IMAGE_URL_KEY, imageUrl)
                putString(PLACEHOLDER_URL_KEY, placeHolderUrl)
            }
        }
    }

    private var _binding: FragmentImagePreviewBinding? = null
    private val binding: FragmentImagePreviewBinding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentImagePreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.apply {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.color_scrim)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.apply {
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.color_primary_variant)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide
                .with(this)
                .load(requireArguments().getString(IMAGE_URL_KEY))
                .thumbnail(
                        Glide.with(this)
                                .load(requireArguments().getString(PLACEHOLDER_URL_KEY))
                ).into(binding.imageView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}