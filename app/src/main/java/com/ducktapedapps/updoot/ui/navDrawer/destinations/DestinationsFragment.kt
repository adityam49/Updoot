package com.ducktapedapps.updoot.ui.navDrawer.destinations

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.databinding.FragmentDestinationsBinding
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.MainActivity
import com.ducktapedapps.updoot.ui.explore.ExploreFragment
import com.ducktapedapps.updoot.ui.login.LoginActivity
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountsAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class DestinationsFragment : Fragment() {
    private var _binding: FragmentDestinationsBinding? = null
    private val binding: FragmentDestinationsBinding
        get() = _binding!!

    private val navDrawerDestinationAdapter = NavDrawerDestinationAdapter(object : NavDrawerDestinationAdapter.ClickHandler {
        override fun openExplore() {
            requireActivity().supportFragmentManager
                    .beginTransaction()
                    .addToBackStack(null)
                    .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_bottom)
                    .replace(R.id.fragment_container, ExploreFragment())
                    .commit()
        }
    })

    private val accountsAdapter = AccountsAdapter(object : AccountsAdapter.AccountAction {

        override fun login() = startActivity(Intent(requireActivity(), LoginActivity::class.java))

        override fun switch(accountName: String) {
            activityVM.setCurrentAccount(accountName)
            (requireActivity() as? MainActivity)?.collapseBottomNavDrawer()
        }

        override fun logout(accountName: String) = activityVM.logout(accountName)

        override fun toggleEntryMenu() = activityVM.toggleAccountsMenuList()
    })


    private val activityVM by lazy {
        ViewModelProvider(requireActivity()).get(ActivityVM::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDestinationsBinding.inflate(inflater, container, false)
        activityVM.apply {
            navigationEntries.asLiveData().observe(viewLifecycleOwner) {
                navDrawerDestinationAdapter.submitList(it)
            }
            accounts.asLiveData().observe(viewLifecycleOwner) {
                accountsAdapter.submitList(it)
            }
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ConcatAdapter().apply {
                addAdapter(accountsAdapter)
                addAdapter(navDrawerDestinationAdapter)
            }
        }
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}