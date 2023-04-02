package com.ducktapedapps.updoot.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons.Outlined
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.plusAssign
import com.ducktapedapps.navigation.Event
import com.ducktapedapps.navigation.Event.AuthEvent.*
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.NavigationDirections.*
import com.ducktapedapps.updoot.ActivityVM
import com.ducktapedapps.updoot.accounts.AccountsMenu
import com.ducktapedapps.updoot.comments.CommentsScreen
import com.ducktapedapps.updoot.imagePreview.ImagePreviewScreen
import com.ducktapedapps.updoot.login.LoginScreen
import com.ducktapedapps.updoot.search.SearchScreen
import com.ducktapedapps.updoot.settings.SettingsScreen
import com.ducktapedapps.updoot.subreddit.SubredditScreen
import com.ducktapedapps.updoot.subreddit.options.SubredditOptions
import com.ducktapedapps.updoot.subscriptions.SubscriptionsScreen
import com.ducktapedapps.updoot.user.UserInfoScreen
import com.ducktapedapps.updoot.utils.Constants.FRONTPAGE
import com.ducktapedapps.updoot.video.VideoScreen
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialNavigationApi
@Composable
fun HomeScreen(
    activityViewModel: ActivityVM,
    publishEvent: (Event) -> Unit
) {
    val navController = rememberNavController()
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    navController.navigatorProvider += bottomSheetNavigator

    LaunchedEffect(key1 = Unit) {
        activityViewModel
            .eventChannel
            .receiveAsFlow()
            .collect { event ->
                when (event) {
                    is ScreenNavigationEvent -> {
                        Timber.d( "screen nav event collected -> ${event.data.route}")
                        navController.navigate(route = event.data.route)
                    }
                    AccountSwitched -> navController.popBackStack(
                        SubredditScreenNavigation.destination,
                        false
                    )
                    LoggedOut -> navController.popBackStack(
                        SubredditScreenNavigation.destination,
                        false
                    )
                    NewAccountAdded -> navController.popBackStack()
                    else -> publishEvent(event)
                }
            }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        ModalBottomSheetLayout(bottomSheetNavigator) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        UpdootBottomNavigationItem.getItems().forEach { bottomNavItem ->
                            NavigationBarItem(
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
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    NavHost(
                        navController = navController,
                        startDestination = SubredditScreenNavigation.destination
                    ) {
                        accountsSwitcherBottomSheet(publishEvent)
                        subscriptionsBottomSheet(publishEvent)

                        searchScreenComposable(publishEvent)
                        subredditOptions(publishEvent)
                        subredditScreenComposable(publishEvent)
                        commentsScreenComposable(publishEvent)
                        userScreenComposable(publishEvent)
                        settingScreenComposable(publishEvent)
                        loginScreenComposable(publishEvent)
                        videoScreenComposable(publishEvent)
                        imageScreenComposable(publishEvent)
                    }
                }
            }
        }
    }
}
@ExperimentalMaterialNavigationApi
private fun NavGraphBuilder.subredditOptions(publishEvent: (Event) -> Unit) {
    bottomSheet(
        route = SubredditOptionsNavigation.destination,
        arguments = SubredditOptionsNavigation.args
    ) {
        val subredditName =
            it.arguments?.getString(SubredditScreenNavigation.SUBREDDIT_NAME_KEY) ?: FRONTPAGE
        SubredditOptions(subredditName = subredditName)
    }
}

@ExperimentalMaterialNavigationApi
private fun NavGraphBuilder.subscriptionsBottomSheet(publishEvent: (Event) -> Unit) {
    bottomSheet(
        route = SubscriptionsNavigation.destination,
        arguments = SubscriptionsNavigation.args
    ) { SubscriptionsScreen(publishEvent = publishEvent) }
}

@ExperimentalMaterialNavigationApi
private fun NavGraphBuilder.accountsSwitcherBottomSheet(publishEvent: (Event) -> Unit) {
    bottomSheet(
        route = AccountSelectionNavigation.destination,
        arguments = AccountSelectionNavigation.args
    ) { AccountsMenu(publishEvent = publishEvent) }
}

private fun NavGraphBuilder.searchScreenComposable(publishEvent: (Event) -> Unit) {
    composable(
        route = SearchNavigation.destination,
        arguments = SearchNavigation.args
    ) { SearchScreen(publishEvent) }
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

private fun NavGraphBuilder.videoScreenComposable(publishEvent: (Event) -> Unit){
    composable(
        route = VideoScreenNavigation.destination,
        arguments = VideoScreenNavigation.args
    ){
        val url = it.arguments?.getString(VideoScreenNavigation.URL_KEY) ?: "#"
        VideoScreen(publishEvent = publishEvent, videoUrl = url)
    }
}
private fun NavGraphBuilder.imageScreenComposable(publishEvent: (Event) -> Unit){
    composable(
        route = ImageScreenNavigation.destination,
        arguments = ImageScreenNavigation.args
    ){
        val url = it.arguments?.getString(ImageScreenNavigation.URL_KEY) ?: "#"
        ImagePreviewScreen(publishEvent = publishEvent, imageUrl = url)
    }
}
sealed class UpdootBottomNavigationItem(val icon: ImageVector, val destination: String) {
    object Posts : UpdootBottomNavigationItem(Outlined.Home, SubredditScreenNavigation.destination)
    object Accounts :
        UpdootBottomNavigationItem(Outlined.AccountCircle, AccountSelectionNavigation.destination)

    object Search : UpdootBottomNavigationItem(Outlined.Search, SearchNavigation.destination)
    object Subscriptions :
        UpdootBottomNavigationItem(Outlined.Menu, SubscriptionsNavigation.destination)

    object Settings :
        UpdootBottomNavigationItem(Outlined.Settings, SettingsScreenNavigation.destination)

    companion object {
        fun getItems() = listOf(Posts, Subscriptions, Search, Settings, Accounts)
    }
}
