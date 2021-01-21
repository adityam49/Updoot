package com.ducktapedapps.updoot.ui.navDrawer


import androidx.compose.animation.Crossfade
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import com.ducktapedapps.updoot.ui.ActivityVM
import com.ducktapedapps.updoot.ui.explore.ExploreScreen
import com.ducktapedapps.updoot.ui.navDrawer.NavigationDestination.*
import com.ducktapedapps.updoot.ui.search.SearchScreen

@Composable
fun NavDrawerScreen(
        goBack: () -> Unit,
        activityVM: ActivityVM,
        onLogin: () -> Unit,
        onRemoveAccount: (accountName: String) -> Unit,
        onToggleAccountMenu: () -> Unit,
        onSwitchAccount: (accountName: String) -> Unit,
        onExit: () -> Unit,
        openSubreddit: (subredditName: String) -> Unit
) {
    Card {
        Crossfade(current = activityVM.currentNavDrawerScreen) {
            when (it.value) {
                NavigationMenu -> NavigationMenuScreen(
                        viewModel = activityVM,
                        onLogin = onLogin,
                        onRemoveAccount = onRemoveAccount,
                        onToggleAccountMenu = onToggleAccountMenu,
                        onSwitchAccount = onSwitchAccount,
                        onExplore = { activityVM.drawerNavigateTo(Explore) },
                        onSearch = { activityVM.drawerNavigateTo(Search) },
                        onExit = onExit
                )
                Search -> SearchScreen(
                        openSubreddit = openSubreddit,
                        subredditDAO = activityVM.subredditDAO,
                        redditClient = activityVM.redditClient,
                        goBack = goBack,
                )
                Explore -> ExploreScreen(
                        redditClient = activityVM.redditClient,
                        subredditDAO = activityVM.subredditDAO,
                        onClickSubreddit = openSubreddit
                )
                CreatePost,
                Inbox,
                History -> throw RuntimeException("Not implemented")
                Exit -> onExit()
            }
        }
    }
}
