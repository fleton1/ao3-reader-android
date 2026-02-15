package com.ao3reader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ao3reader.ui.screens.bookmarks.BookmarksScreen
import com.ao3reader.ui.screens.downloads.DownloadsScreen
import com.ao3reader.ui.screens.following.FollowingScreen
import com.ao3reader.ui.screens.home.HomeScreen
import com.ao3reader.ui.screens.reader.ReaderScreen
import com.ao3reader.ui.screens.search.SearchScreen
import com.ao3reader.ui.screens.workdetail.WorkDetailScreen

/**
 * Main navigation graph for the app.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Home screen
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToBookmarks = {
                    navController.navigate(Screen.Bookmarks.route)
                },
                onNavigateToDownloads = {
                    navController.navigate(Screen.Downloads.route)
                },
                onNavigateToFollowing = {
                    navController.navigate(Screen.Following.route)
                },
                onNavigateToWork = { workId ->
                    navController.navigate(Screen.WorkDetail.createRoute(workId))
                }
            )
        }

        // Search screen
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToWork = { workId ->
                    navController.navigate(Screen.WorkDetail.createRoute(workId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Bookmarks screen
        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                onNavigateToWork = { workId ->
                    navController.navigate(Screen.WorkDetail.createRoute(workId))
                },
                onNavigateToReader = { workId, chapterNumber ->
                    navController.navigate(Screen.Reader.createRoute(workId, chapterNumber))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Downloads screen
        composable(Screen.Downloads.route) {
            DownloadsScreen(
                onNavigateToWork = { workId ->
                    navController.navigate(Screen.WorkDetail.createRoute(workId))
                },
                onNavigateToReader = { workId, chapterNumber ->
                    navController.navigate(Screen.Reader.createRoute(workId, chapterNumber))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Following screen
        composable(Screen.Following.route) {
            FollowingScreen(
                onNavigateToWork = { workId ->
                    navController.navigate(Screen.WorkDetail.createRoute(workId))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Work detail screen
        composable(
            route = Screen.WorkDetail.route,
            arguments = Screen.WorkDetail.arguments
        ) { backStackEntry ->
            val workId = backStackEntry.arguments?.getString("workId") ?: return@composable
            WorkDetailScreen(
                workId = workId,
                onNavigateToReader = { chapterNumber ->
                    navController.navigate(Screen.Reader.createRoute(workId, chapterNumber))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Reader screen
        composable(
            route = Screen.Reader.route,
            arguments = Screen.Reader.arguments
        ) { backStackEntry ->
            val workId = backStackEntry.arguments?.getString("workId") ?: return@composable
            val chapterNumber = backStackEntry.arguments?.getInt("chapterNumber") ?: 1
            ReaderScreen(
                workId = workId,
                chapterNumber = chapterNumber,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChapter = { newChapterNumber ->
                    navController.navigate(Screen.Reader.createRoute(workId, newChapterNumber)) {
                        popUpTo(Screen.Reader.createRoute(workId, chapterNumber)) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}
