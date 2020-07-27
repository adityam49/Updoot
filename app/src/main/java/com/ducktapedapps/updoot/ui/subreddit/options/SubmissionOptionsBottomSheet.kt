package com.ducktapedapps.updoot.ui.subreddit.options

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.FragmentSubmissionOptionsBottomSheetBinding
import com.ducktapedapps.updoot.utils.Constants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import javax.inject.Inject

class SubmissionOptionsBottomSheet : BottomSheetDialogFragment() {
    companion object {
        private const val SUBMISSION_ID_KEY = "submission_id_key"
        fun newInstance(id: String) = SubmissionOptionsBottomSheet().apply {
            arguments = Bundle().apply { putString(SUBMISSION_ID_KEY, id) }
        }
    }

    @Inject
    lateinit var vmFactory: OptionsSheetVMFactory
    private val viewModel: OptionsSheetViewModel by lazy {
        ViewModelProvider(
                this,
                vmFactory.apply { setSubmissionId(requireArguments().getString(SUBMISSION_ID_KEY)!!) }
        ).get(OptionsSheetViewModel::class.java)
    }
    private val clipboardManager by lazy {
        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    private lateinit var binding: FragmentSubmissionOptionsBottomSheetBinding

    private val optionsAdapter = OptionsAdapter(::copyLink)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireContext().applicationContext as UpdootApplication).updootComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSubmissionOptionsBottomSheetBinding.inflate(inflater, container, false)
        setUpViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = optionsAdapter
            }
        }
    }

    private fun setUpViewModel() =
            viewModel.optionsList.observe(viewLifecycleOwner) { optionsAdapter.submitList(it) }

    private fun copyLink(link: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", Constants.BASE_URL + link))
        Toast.makeText(requireContext(), "Link copied !", Toast.LENGTH_SHORT).show()
        dismiss()
    }
}