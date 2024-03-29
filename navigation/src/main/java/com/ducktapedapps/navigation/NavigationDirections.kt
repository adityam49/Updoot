package com.ducktapedapps.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class NavigationDirections {
    object AccountSelectionNavigation : NavigationDirections() {
        const val destination = "bottomSheet/accounts"

        val args = emptyList<NamedNavArgument>()

        fun open() = object : NavigationCommand {

            override val arguments = args

            override val route: String = destination
        }
    }

    object SearchNavigation : NavigationDirections() {
        const val destination = "search"

        val args = emptyList<NamedNavArgument>()

        fun open() = object : NavigationCommand {

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
                defaultValue = "#"
            }
        )

        fun open(subredditName: String) = object : NavigationCommand {

            override val arguments = args

            override val route: String = "subreddit/$subredditName"
        }
    }

    object SubredditOptionsNavigation : NavigationDirections() {
        const val SUBREDDIT_NAME_KEY = "subreddit_name"
        const val destination = "subreddit/options/{$SUBREDDIT_NAME_KEY}"

        val args = listOf(
            navArgument(SUBREDDIT_NAME_KEY) {
                type = NavType.StringType
                defaultValue = "#"
            }
        )

        fun open(subredditName: String) = object : NavigationCommand {

            override val arguments = args

            override val route: String = "subreddit/options/$subredditName"
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

        fun open(subredditID: String, postID: String) = object : NavigationCommand {

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

        fun open(userName: String) = object : NavigationCommand {

            override val arguments = args

            override val route: String = "user/$userName"
        }
    }

    object LoginScreenNavigation : NavigationDirections() {
        const val destination = "login"
        val args = emptyList<NamedNavArgument>()

        fun open() = object : NavigationCommand {

            override val arguments = args

            override val route: String = destination

        }
    }

    object SettingsScreenNavigation : NavigationDirections() {
        const val destination = "settings"
        val args = emptyList<NamedNavArgument>()
        fun open() = object : NavigationCommand {

            override val arguments = args

            override val route: String = destination
        }
    }

    object VideoScreenNavigation : NavigationDirections() {
        const val URL_KEY = "URL_KEY"
        const val destination = "video/{$URL_KEY}"
        val args = listOf(
            navArgument(URL_KEY) {
                type = NavType.StringType
                nullable = false
            }
        )

        fun open(url: String) = object : NavigationCommand {

            override val arguments = args

            override val route: String =
                "video/${URLEncoder.encode(url, StandardCharsets.UTF_8.toString())}"
        }

    }

    object ImageScreenNavigation : NavigationDirections() {
        const val URL_KEY = "URL_KEY"
        const val destination = "image/{$URL_KEY}"
        val args = listOf(
            navArgument(URL_KEY) {
                type = NavType.StringType
                nullable = false
            }
        )

        fun open(url: String) = object : NavigationCommand {

            override val arguments = args

            override val route: String =
                "image/${URLEncoder.encode(url, StandardCharsets.UTF_8.toString())}"
        }

    }
}