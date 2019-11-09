package com.ducktapedapps.updoot.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.ducktapedapps.updoot.R
import com.github.chrisbanes.photoview.PhotoView


const val TAG = "MediaPreviewFragment"

class MediaPreviewFragment : DialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.image_preview_fragment, container, false)
        val photoView: PhotoView = root.findViewById(R.id.imageView)
        arguments?.run {
            val args: MediaPreviewFragmentArgs = MediaPreviewFragmentArgs.fromBundle(this)
            Glide.with(photoView.context).load(args.mediaUrl).into(photoView)
        }
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, R.style.FullScreenDialog)
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
        super.onCreate(savedInstanceState)
    }
}