package com.ducktapedapps.updoot.ui.navDrawer

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ducktapedapps.updoot.ui.navDrawer.destinations.DestinationsFragment
import com.ducktapedapps.updoot.ui.navDrawer.subscriptions.SubscriptionFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class NavDrawerPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment =
            if (position == 0) DestinationsFragment()
            else SubscriptionFragment()
}