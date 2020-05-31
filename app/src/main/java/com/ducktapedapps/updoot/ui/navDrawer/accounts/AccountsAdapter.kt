package com.ducktapedapps.updoot.ui.navDrawer.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.databinding.AccountItemBinding
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountModel.SystemModel
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountModel.UserModel
import com.ducktapedapps.updoot.utils.Constants

class AccountsAdapter(private val action: AccountAction) : ListAdapter<AccountModel, AccountsAdapter.NavDrawerItemViewHolder>(CALLBACK) {

    interface AccountAction {
        fun login()
        fun switch(accountName: String)
        fun logout(accountName: String)
    }

    inner class NavDrawerItemViewHolder(val binding: AccountItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(account: AccountModel) = when (account) {
            is UserModel -> bindUserAccount(account)
            is SystemModel -> bindSystemAccount(account)
        }

        private fun bindUserAccount(account: UserModel) =
                binding.apply {
                    textView.text = account.name
                    with(imageView) {
                        Glide
                                .with(this)
                                .load(account.userIcon)
                                .apply(RequestOptions.circleCropTransform())
                                .into(this)
                    }
                    logout.apply {
                        visibility = View.VISIBLE
                        setOnClickListener { action.logout(account.name) }
                    }
                    root.setOnClickListener { action.switch(account.name) }
                }

        private fun bindSystemAccount(account: SystemModel) =
                binding.apply {
                    textView.text = account.name
                    imageView.setImageDrawable(ContextCompat.getDrawable(imageView.context, account.icon))
                    logout.visibility = View.GONE
                    root.setOnClickListener {
                        when (account.name) {
                            Constants.ADD_ACCOUNT -> action.login()
                            else -> action.switch(account.name)
                        }
                    }
                }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NavDrawerItemViewHolder(AccountItemBinding.inflate(LayoutInflater.from(parent.context)))

    override fun onBindViewHolder(holder: NavDrawerItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private companion object CALLBACK : DiffUtil.ItemCallback<AccountModel>() {
        override fun areItemsTheSame(oldItem: AccountModel, newItem: AccountModel) = oldItem == newItem

        override fun areContentsTheSame(oldItem: AccountModel, newItem: AccountModel) = true
    }
}