package com.ducktapedapps.navigation

import androidx.navigation.NamedNavArgument

interface ScreenNavigationCommand {
    val arguments: List<NamedNavArgument>

    val route: String
}