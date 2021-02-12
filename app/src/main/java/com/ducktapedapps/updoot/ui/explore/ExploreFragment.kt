package com.ducktapedapps.updoot.ui.explore

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
class ExploreFragment : Fragment() {
    private val viewModel: ExploreVM by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            UpdootTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ExploreScreen(
                        onClickSubreddit = (requireActivity() as MainActivity)::openSubreddit,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}