package com.ducktapedapps.updoot.home

import android.util.Log
import android.widget.Toast
import androidx.compose.material.*
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.AuthEvent.*
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.Event.ToastEvent
import com.ducktapedapps.navigation.NavigationDirections.*
import com.ducktapedapps.updoot.ActivityVM
import com.ducktapedapps.updoot.accounts.AccountsMenu
import com.ducktapedapps.updoot.comments.CommentsScreen
import com.ducktapedapps.updoot.login.LoginScreen
import com.ducktapedapps.updoot.settings.SettingsScreen
import com.ducktapedapps.updoot.subreddit.SubredditScreen
import com.ducktapedapps.updoot.user.UserInfoScreen
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow

private const val TAG = "HomeScreen"

@Composable
fun HomeScreen(activityViewModel: ActivityVM) {
    val navController = rememberNavController()

    val context = LocalContext.current
    LaunchedEffect(key1 = Unit) {
        activityViewModel
            .eventChannel
            .receiveAsFlow()
            .collect { event ->
                when (event) {
                    is ScreenNavigationEvent -> {
                        Log.d(TAG, "screen nav event collected -> ${event.data.route}")
                        navController.navigate(route = event.data.route)
                    }
                    is ToastEvent -> Toast.makeText(context, event.content, Toast.LENGTH_SHORT)
                        .show()
                    AccountSwitched -> navController.popBackStack(
                        SubredditScreenNavigation.destination,
                        false
                    )
                    LoggedOut -> navController.popBackStack(
                        SubredditScreenNavigation.destination,
                        false
                    )
                    NewAccountAdded -> navController.popBackStack()
                }
            }
    }
    val publishEvent = { event: Event -> activityViewModel.sendEvent(event) }


    Surface(color = MaterialTheme.colors.background) {
        Scaffold(
            bottomBar = {
                BottomNavigation {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    UpdootBottomNavigationItem.getItems().forEach { bottomNavItem ->
                        BottomNavigationItem(
                            icon = { Icon(bottomNavItem.icon, bottomNavItem.icon.name) },
                            selected = currentDestination?.hierarchy?.any { it.route == bottomNavItem.destination } == true,
                            onClick = {
                                navController.navigate(bottomNavItem.destination) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = false
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = SubredditScreenNavigation.destination
            ) {
                //replace with bottomSheet
                accountsScreen(publishEvent)

                subredditScreenComposable(publishEvent)
                commentsScreenComposable(publishEvent)
                userScreenComposable(publishEvent)
                settingScreenComposable(publishEvent)
                loginScreenComposable(publishEvent)
            }
        }
    }
}

private fun NavGraphBuilder.accountsScreen(publishEvent: (Event) -> Unit) {
    composable(
        route = AccountSelectionNavigation.destination,
        arguments = AccountSelectionNavigation.args
    ) { AccountsMenu(publishEvent = publishEvent) }
}


private fun NavGraphBuilder.loginScreenComposable(publishEvent: (Event) -> Unit) {
    composable(
        route = LoginScreenNavigation.destination,
        arguments = LoginScreenNavigation.args,
    ) {
        LoginScreen(publishEvent)
    }
}

private fun NavGraphBuilder.userScreenComposable(publishEvent: (Event) -> Unit) {
    composable(
        route = UserScreenNavigation.destination,
        arguments = UserScreenNavigation.args
    ) {
        val userName = it.arguments?.getString(UserScreenNavigation.USERNAME_KEY)!!
        UserInfoScreen(userName = userName, publishEvent = publishEvent)
    }
}

private fun NavGraphBuilder.commentsScreenComposable(publishEvent: (Event) -> Unit) {
    composable(
        route = CommentScreenNavigation.destination,
        arguments = CommentScreenNavigation.args
    ) {
        val subredditId = it.arguments?.getString(CommentScreenNavigation.SUBREDDIT_ID_KEY)!!
        val postId = it.arguments?.getString(CommentScreenNavigation.POST_ID_KEY)!!
        CommentsScreen(
            subredditId = subredditId,
            postId = postId,
            publishEvent = publishEvent
        )
    }
}

private fun NavGraphBuilder.subredditScreenComposable(publishEvent: (Event) -> Unit) {
    composable(
        route = SubredditScreenNavigation.destination,
        arguments = SubredditScreenNavigation.args
    ) {
        val subreddit = it.arguments?.getString(SubredditScreenNavigation.SUBREDDIT_NAME_KEY) ?: "#"
        SubredditScreen(publishEvent = publishEvent, subredditName = subreddit)
    }

}

private fun NavGraphBuilder.settingScreenComposable(publishEvent: (Event) -> Unit) {
    composable(
        route = SettingsScreenNavigation.destination,
        arguments = SettingsScreenNavigation.args
    ) { SettingsScreen() }
}

sealed class UpdootBottomNavigationItem(val icon: ImageVector, val destination: String) {
    object Posts : UpdootBottomNavigationItem(Outlined.Home, SubredditScreenNavigation.destination)
    object Accounts :
        UpdootBottomNavigationItem(Outlined.AccountCircle, AccountSelectionNavigation.destination)

    object Settings :
        UpdootBottomNavigationItem(Outlined.Settings, SettingsScreenNavigation.destination)

    companion object {
        fun getItems() = listOf(Posts, Accounts, Settings)
    }
}
