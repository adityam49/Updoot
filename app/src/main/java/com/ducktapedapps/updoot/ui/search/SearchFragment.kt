package com.ducktapedapps.updoot.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ducktapedapps.updoot.ui.MainActivity
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchVM by viewModels<SearchVMImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            UpdootTheme {
                Surface(color = MaterialTheme.colors.background) {
                    SearchScreen(
                        goBack = { requireActivity().onBackPressed() },
                        openSubreddit = (requireActivity() as MainActivity)::openSubreddit,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}