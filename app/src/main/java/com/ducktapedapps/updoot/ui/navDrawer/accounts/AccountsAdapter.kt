package com.ducktapedapps.updoot.ui.navDrawer.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ducktapedapps.updoot.databinding.CurrentAccountItemBinding
import com.ducktapedapps.updoot.databinding.NonCurrentAccountItemBinding
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountModel.*
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountsAdapter.AccountVH.CurrentAccountVH
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountsAdapter.AccountVH.NonCurrentAccountVH

class AccountsAdapter(private val actions: AccountAction) : ListAdapter<AccountModel, AccountsAdapter.AccountVH>(CALLBACK) {

    interface AccountAction {
        fun login()
        fun switch(accountName: String)
        fun logout(accountName: String)
        fun toggleEntryMenu()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            if (viewType == CURRENT_VH)
                CurrentAccountVH(CurrentAccountItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else
                NonCurrentAccountVH(NonCurrentAccountItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun getItemViewType(position: Int) = with(getItem(position)) {
        when (this) {
            is AnonymousAccount -> if (this.isCurrent) CURRENT_VH else NON_CURRENT_VH
            is AddAccount -> NON_CURRENT_VH
            is UserModel -> if (this.isCurrentAccount) CURRENT_VH else NON_CURRENT_VH
        }
    }


    override fun onBindViewHolder(holder: AccountVH, position: Int) {
        when (holder) {
            is CurrentAccountVH -> holder.bind(getItem(position), actions)
            is NonCurrentAccountVH -> holder.bind(getItem(position), actions)
        }
    }

    sealed class AccountVH(view: View) : ViewHolder(view) {
        class CurrentAccountVH(val binding: CurrentAccountItemBinding) : AccountVH(binding.root) {
            fun bind(account: AccountModel, actions: AccountAction) = binding.apply {
                chevron.setOnClickListener { actions.toggleEntryMenu() }
                logoutButton.apply {
                    if (account is UserModel) {
                        visibility = View.VISIBLE
                        setOnClickListener { actions.logout(account.name) }
                    } else visibility = View.GONE
                }
                userNameText.text = account.name
                with(userIcon) {
                    when (account) {
                        is UserModel -> Glide
                                .with(this)
                                .load(account.userIcon)
                                .apply(RequestOptions.circleCropTransform())
                                .into(this)

                        //TODO : glide shows white drawable icons on white background for light theme
                        is AnonymousAccount -> setImageDrawable(ContextCompat.getDrawable(context, account.icon))
                        is AddAccount -> setImageDrawable(ContextCompat.getDrawable(context, account.icon))
                    }
                }
            }

        }

        class NonCurrentAccountVH(val binding: NonCurrentAccountItemBinding) : AccountVH(binding.root) {
            fun bind(account: AccountModel, actions: AccountAction) = binding.apply {
                userNameText.text = account.name
                root.setOnClickListener { if (account is AddAccount) actions.login() else actions.switch(account.name) }
                with(userIcon) {
                    when (account) {
                        is UserModel ->
                            Glide
                                    .with(this)
                                    .load(account.userIcon)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(this)
                        is AnonymousAccount -> setImageDrawable(ContextCompat.getDrawable(context, account.icon))
                        is AddAccount -> setImageDrawable(ContextCompat.getDrawable(context, account.icon))
                    }
                }
                logoutButton.apply {
                    if (account is UserModel) {
                        setOnClickListener { actions.logout(account.name) }
                        visibility = View.VISIBLE
                    } else visibility = View.GONE
                }
            }
        }

    }

    private companion object {
        val CALLBACK = object : DiffUtil.ItemCallback<AccountModel>() {
            override fun areItemsTheSame(oldItem: AccountModel, newItem: AccountModel) = oldItem == newItem

            override fun areContentsTheSame(oldItem: AccountModel, newItem: AccountModel) = true
        }
        const val CURRENT_VH = 1
        const val NON_CURRENT_VH = 2
    }
}