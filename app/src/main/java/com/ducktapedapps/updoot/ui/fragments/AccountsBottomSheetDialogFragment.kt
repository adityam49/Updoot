package com.ducktapedapps.updoot.ui.fragments

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*
import javax.inject.Inject

class AccountsBottomSheetDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var accountManager: AccountManager

    private var accountChangeListener: BottomSheetListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity?.application as UpdootApplication).updootComponent.inject(this@AccountsBottomSheetDialogFragment)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.accounts_modal_bottom_sheet, container, false)
        val allAccounts: MutableList<String> = ArrayList()
        for (account in accountManager.accounts) {
            allAccounts.add(account.name)
        }
        allAccounts.add("Add Account")
        val accountLV = view.findViewById<ListView>(R.id.accountsLV)
        val adapter = ArrayAdapter(inflater.context, R.layout.modal_sheet_account_item, R.id.bottom_sheet_userName, allAccounts)
        accountLV.adapter = adapter
        accountLV.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, _: View?, position: Int, _: Long ->
            accountChangeListener?.onButtonClicked(adapter.getItem(position))
            dismiss()
        }
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        accountChangeListener = try {
            context as BottomSheetListener
        } catch (e: ClassCastException) {
            throw ClassCastException(context.toString() + "has not implemented BottomSheetListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        accountChangeListener = null
    }

    interface BottomSheetListener {
        fun onButtonClicked(text: String?)
    }
}