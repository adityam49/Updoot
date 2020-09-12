package com.ducktapedapps.updoot.ui.imagePreview

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.*
import com.bumptech.glide.Glide
import com.ducktapedapps.updoot.backgroundWork.ImageDownLoadWorker
import com.ducktapedapps.updoot.databinding.FragmentImagePreviewBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview


@ExperimentalCoroutinesApi
@FlowPreview
class ImagePreviewFragment : Fragment() {
    companion object {
        private const val IMAGE_DOWNLOAD_TAG = "image_download_tag"
        private const val IMAGE_URL_KEY = "image_url_key"
        private const val PLACEHOLDER_URL_KEY = "place_holder_key"
        private const val TAG = "ImagePreviewFragment"
        fun newInstance(placeHolderUrl: String?, imageUrl: String) = ImagePreviewFragment().apply {
            arguments = Bundle().apply {
                putString(IMAGE_URL_KEY, imageUrl)
                putString(PLACEHOLDER_URL_KEY, placeHolderUrl)
            }
        }
    }
    private val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) enqueueImageDownLoad()
                else Toast.makeText(requireContext(), "Saving images requires storage permission", Toast.LENGTH_LONG).show()
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

        binding.downloadButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                enqueueImageDownLoad()
            else checkPermissionAndDownload()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPermissionAndDownload() {
        val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        when (ContextCompat.checkSelfPermission(requireActivity(), writePermission)) {
            PackageManager.PERMISSION_GRANTED -> enqueueImageDownLoad()
            PackageManager.PERMISSION_DENIED -> {
                Toast.makeText(requireActivity(), "Storage permission are required for saving image!", Toast.LENGTH_SHORT).show()
                if (shouldShowRequestPermissionRationale(writePermission)) openSettings()
                else requestPermissionLauncher.launch(writePermission)
            }
            else -> Unit
        }
    }


    private fun enqueueImageDownLoad() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val imageUrlInput = workDataOf(ImageDownLoadWorker.URL_KEY to requireArguments().getString(IMAGE_URL_KEY)!!)
        val downloadRequest = OneTimeWorkRequestBuilder<ImageDownLoadWorker>()
                .setInputData(imageUrlInput)
                .setConstraints(constraints)
                .build()
        WorkManager.getInstance(requireContext()).enqueueUniqueWork(
                IMAGE_DOWNLOAD_TAG,
                ExistingWorkPolicy.REPLACE,
                downloadRequest
        )
    }

    private fun openSettings() =
            Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${requireActivity().packageName}")
            ).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.also { intent ->
                startActivity(intent)
            }
}