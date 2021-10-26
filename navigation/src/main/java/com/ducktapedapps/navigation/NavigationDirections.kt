package com.ducktapedapps.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class NavigationDirections {

    object AccountSelectionNavigation : NavigationDirections() {
        const val destination = "accounts"

        val args = emptyList<NamedNavArgument>()

        fun open() = object : ScreenNavigationCommand {

            override val arguments = args

            override val route: String = destination
        }
    }

    object SubredditScreenNavigation : NavigationDirections() {
        const val SUBREDDIT_NAME_KEY = "subreddit_name"
        const val destination = "subreddit/{$SUBREDDIT_NAME_KEY}"

        val args = listOf(
            navArgument(SUBREDDIT_NAME_KEY) {
                type = NavType.StringType
                nullable = true
            }
        )

        fun open(subredditName: String) = object : ScreenNavigationCommand {

            override val arguments = args

            override val route: String = "subreddit/$subredditName"
        }
    }

    object CommentScreenNavigation : NavigationDirections() {
        const val SUBREDDIT_ID_KEY = "subreddit_name"
        const val POST_ID_KEY = "post_id_key"
        const val destination = "comments/{$SUBREDDIT_ID_KEY}/{$POST_ID_KEY}"

        val args = listOf(
            navArgument(SUBREDDIT_ID_KEY) {
                type = NavType.StringType
                nullable = false
            },
            navArgument(POST_ID_KEY) {
                type = NavType.StringType
                nullable = false
            }
        )

        fun open(subredditID: String, postID: String) = object : ScreenNavigationCommand {

            override val arguments = args

            override val route: String = "comments/$subredditID/$postID"
        }
    }

    object UserScreenNavigation : NavigationDirections() {
        const val USERNAME_KEY = "username_key"
        const val destination = "user/{$USERNAME_KEY}"

        val args = listOf(
            navArgument(USERNAME_KEY) {
                type = NavType.StringType
                nullable = false
            }
        )

        fun open(userName: String) = object : ScreenNavigationCommand {

            override val arguments = args

            override val route: String = "user/$userName"
        }
    }

    object LoginScreenNavigation : NavigationDirections() {
        const val destination = "login"
        val args = emptyList<NamedNavArgument>()

        fun open() = object : ScreenNavigationCommand {

            override val arguments = args

            override val route: String = destination

        }
    }

    object SettingsScreenNavigation : NavigationDirections() {
        const val destination = "settings"
        val args = emptyList<NamedNavArgument>()
        fun open() = object : ScreenNavigationCommand {

            override val arguments = args

            override val route: String = destination
        }
    }

}