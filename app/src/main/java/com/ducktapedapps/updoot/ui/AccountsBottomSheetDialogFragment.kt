package com.ducktapedapps.updoot.ui

import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.databinding.AccountsModalBottomSheetBinding
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.UserManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*
import javax.inject.Inject

class AccountsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var userManager: UserManager

    private lateinit var viewModel: ActivityVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as UpdootApplication).updootComponent.inject(this@AccountsBottomSheetDialogFragment)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = AccountsModalBottomSheetBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(ActivityVM::class.java)

        val allAccounts: MutableList<String> = ArrayList()
        for (account in accountManager.accounts) {
            allAccounts.add(account.name)
        }
        allAccounts.add("Add Account")

        val accountsAdapter = ArrayAdapter(inflater.context, R.layout.modal_sheet_account_item, R.id.bottom_sheet_userName, allAccounts)
        binding.accountsLV.apply {
            adapter = accountsAdapter
            onItemClickListener = OnItemClickListener { _, _, position, _ ->
                when {
                    adapter.getItem(position) == "Add Account" -> {
                        val intent = Intent(requireActivity(), LoginActivity::class.java)
                        startActivityForResult(intent, Constants.ACCOUNT_LOGIN_REQUEST_CODE)
                    }
                    adapter.getItem(position) == Constants.ANON_USER -> {
                        userManager.setCurrentUser(Constants.ANON_USER, null)
                        viewModel.setCurrentAccount(Constants.ANON_USER)
                    }
                    else -> {
                        userManager.setCurrentUser(accountsAdapter.getItem(position), null)
                        viewModel.setCurrentAccount(accountsAdapter.getItem(position))
                    }
                }
                this@AccountsBottomSheetDialogFragment.dismiss()
            }
        }
        return binding.root
    }
}