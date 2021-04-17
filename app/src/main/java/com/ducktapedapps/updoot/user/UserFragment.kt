package com.ducktapedapps.updoot.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ducktapedapps.updoot.theme.UpdootTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserFragment : Fragment() {
    companion object {
        const val USERNAME_KEY = "username_key"
        fun newInstance(userName: String) = UserFragment().apply {
            arguments = Bundle().apply {
                putString(USERNAME_KEY, userName)
            }
        }
    }

    private val viewModel: UserViewModel by viewModels<UserViewModelImpl>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setContent {
            UpdootTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background,
                    content = { UserInfoScreen(viewModel = viewModel) }
                )
            }
        }
    }
}