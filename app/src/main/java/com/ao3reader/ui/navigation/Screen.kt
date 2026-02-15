package com.ao3reader.ui.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Sealed class representing all navigation destinations in the app.
 */
sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList()
) {
    // Main navigation destinations
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Bookmarks : Screen("bookmarks")
    data object Downloads : Screen("downloads")
    data object Following : Screen("following")

    // Detail screens
    data object WorkDetail : Screen(
        route = "work/{workId}",
        arguments = listOf(
            navArgument("workId") { type = NavType.StringType }
        )
    ) {
        fun createRoute(workId: String) = "work/$workId"
    }

    data object Reader : Screen(
        route = "reader/{workId}/{chapterNumber}",
        arguments = listOf(
            navArgument("workId") { type = NavType.StringType },
            navArgument("chapterNumber") { type = NavType.IntType }
        )
    ) {
        fun createRoute(workId: String, chapterNumber: Int) = "reader/$workId/$chapterNumber"
    }

    // Settings and other screens
    data object Settings : Screen("settings")
}
