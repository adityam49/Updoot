package com.ducktapedapps.updoot.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
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
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
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