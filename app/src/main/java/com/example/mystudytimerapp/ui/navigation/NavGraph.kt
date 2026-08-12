package com.example.mystudytimerapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mystudytimerapp.StudyTimerApplication
import com.example.mystudytimerapp.ui.screens.HistoryScreen
import com.example.mystudytimerapp.ui.screens.TaskListScreen
import com.example.mystudytimerapp.ui.screens.TimerScreen
import com.example.mystudytimerapp.ui.viewmodel.TaskListViewModel
import com.example.mystudytimerapp.ui.viewmodel.TimerViewModel
import com.example.mystudytimerapp.ui.viewmodel.ViewModelFactory

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current.applicationContext as StudyTimerApplication
    val repository = context.repository
    val factory = ViewModelFactory(repository)

    NavHost(
        navController = navController,
        startDestination = Screen.TaskList.route
    ) {
        composable(Screen.TaskList.route) {
            val taskListViewModel: TaskListViewModel = viewModel(factory = factory)
            TaskListScreen(
                viewModel = taskListViewModel,
                onNavigateToTimer = { taskId ->
                    navController.navigate(Screen.Timer.createRoute(taskId))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(
            route = Screen.Timer.route,
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            val timerViewModel: TimerViewModel = viewModel(factory = factory)
            TimerScreen(
                taskId = taskId,
                viewModel = timerViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.History.route) {
            val taskListViewModel: TaskListViewModel = viewModel(factory = factory)
            HistoryScreen(
                viewModel = taskListViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
