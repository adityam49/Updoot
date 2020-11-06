package com.ducktapedapps.updoot.ui.user

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.ui.theme.UpdootTheme
import javax.inject.Inject

class UserFragment : Fragment() {
    companion object {
        private const val USERNAME_KEY = "username_key"
        fun newInstance(userName: String) = UserFragment().apply {
            arguments = Bundle().apply {
                putString(USERNAME_KEY, userName)
            }
        }
    }

    private val userName: String
        get() = requireArguments().getString(USERNAME_KEY, null)!!

    @Inject
    lateinit var viewModelFactory: UserVMFactory

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity?.application as UpdootApplication).updootComponent.inject(this)
    }

    private val viewModel by lazy {
        ViewModelProvider(
                this,
                viewModelFactory.apply { forUser(userName) }
        ).get(UserViewModel::class.java)
    }

    @ExperimentalLazyDsl
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            ComposeView(requireContext()).apply {
                setContent {
                    UpdootTheme {
                        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                            UserInfoScreen(viewModel = viewModel)
                        }
                    }
                }
            }
}