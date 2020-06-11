package com.ducktapedapps.updoot.ui.subreddit.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.FragmentSubmissionOptionsBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SubmissionOptionsBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentSubmissionOptionsBottomSheetBinding
    private val args: SubmissionOptionsBottomSheetArgs by navArgs()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSubmissionOptionsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                val placeHolderOptions = mutableListOf<SubmissionOptionUiModel>().apply {
                    repeat(15) {
                        add(SubmissionOptionUiModel("Option $it", R.drawable.ic_explore_24dp))
                    }
                }
                adapter = OptionsAdapter().apply { submitList(placeHolderOptions) }
            }
        }
        Toast.makeText(requireContext(), "id found :${args.submissionsId}", Toast.LENGTH_SHORT).show()
    }
}