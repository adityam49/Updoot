package com.ducktapedapps.updoot.ui.navDrawer.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import  androidx.recyclerview.widget.ListAdapter
import com.ducktapedapps.updoot.databinding.AccountItemBinding
import com.ducktapedapps.updoot.utils.Constants

class AccountsAdapter(private val action: AccountAction) : ListAdapter<AccountModel, AccountsAdapter.NavDrawerItemViewHolder>(CALLBACK) {

    interface AccountAction {
        fun login()
        fun switch(accountName: String)
        fun logout(accountName: String)
    }

    inner class NavDrawerItemViewHolder(val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: AccountModel) = binding.apply {
            imageView.setImageDrawable(ContextCompat.getDrawable(this.imageView.context, account.icon))
            textView.text = account.name
            when (account.name) {
                Constants.ANON_USER -> {
                    logout.visibility = View.GONE
                    root.setOnClickListener { action.switch(account.name) }
                    imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, account.icon))
                }

                Constants.ADD_ACCOUNT -> {
                    logout.visibility = View.GONE
                    root.setOnClickListener { action.login() }
                }

                else -> {
                    logout.apply {
                        visibility = View.VISIBLE
                        setOnClickListener { action.logout(account.name) }
                    }
                    root.setOnClickListener { action.switch(account.name) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavDrawerItemViewHolder(AccountItemBinding.inflate(LayoutInflater.from(parent.context)))

    override fun onBindViewHolder(holder: NavDrawerItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object CALLBACK : DiffUtil.ItemCallback<AccountModel>() {
        override fun areItemsTheSame(oldItem: AccountModel, newItem: AccountModel) = oldItem == newItem

        override fun areContentsTheSame(oldItem: AccountModel, newItem: AccountModel) = true
    }
}