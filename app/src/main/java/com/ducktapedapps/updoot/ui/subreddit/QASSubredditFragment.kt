package com.ducktapedapps.updoot.ui.subreddit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.databinding.QasSubredditContentsBinding
import com.ducktapedapps.updoot.utils.showMenuFor

class QASSubredditFragment : Fragment() {
    private lateinit var qasSubredditVM: QASSubredditVM

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: QasSubredditContentsBinding = QasSubredditContentsBinding.inflate(inflater, container, false)
                .apply { lifecycleOwner = viewLifecycleOwner }

        activity?.let {
            qasSubredditVM = ViewModelProvider(it).get(QASSubredditVM::class.java)
            binding.qasSubredditVM = qasSubredditVM

        }

        binding.sortButton.setOnClickListener {
            showMenuFor(requireContext(), it, qasSubredditVM)
        }

        binding.viewModeButton.setOnClickListener {
            qasSubredditVM.toggleUi()
        }
        return binding.root
    }

}