package com.example.mystudytimerapp.ui.navigation

sealed class Screen(val route: String) {
    data object TaskList : Screen("task_list")

    data object Timer : Screen("timer/{taskId}") {
        fun createRoute(taskId: Int): String {
            return "timer/$taskId"
        }
    }

    data object History : Screen("history")
}
