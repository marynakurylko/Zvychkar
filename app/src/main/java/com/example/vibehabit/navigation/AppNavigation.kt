package com.example.vibehabit.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vibehabit.screens.CreateHabitScreen
import com.example.vibehabit.screens.DashboardScreen
import com.example.vibehabit.screens.SettingsScreen
import com.example.vibehabit.viewmodels.HabitsViewModel
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.vibehabit.screens.CalendarScreen
import com.example.vibehabit.screens.HabitDetailsScreen
import androidx.compose.ui.res.stringResource
import com.example.vibehabit.R
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.vibehabit.screens.AnalyticsScreen
import com.example.vibehabit.screens.OnboardingScreen

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: HabitsViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    if (isOnboardingCompleted == null) return
    val startDestination = if (isOnboardingCompleted == true) "dashboard" else "onboarding"

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute != "onboarding" && currentRoute in listOf("dashboard", "calendar", "analytics", "settings")) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.nav_home_desc)) },
                        label = { Text(stringResource(R.string.nav_habits_label)) },
                        selected = currentRoute == "dashboard",
                        onClick = {
                            if (currentRoute != "dashboard") {
                                navController.navigate("dashboard") { popUpTo("dashboard") { inclusive = true } }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.nav_calendar_desc)) },
                        label = { Text(stringResource(R.string.nav_calendar_label)) },
                        selected = currentRoute == "calendar",
                        onClick = {
                            if (currentRoute != "calendar") navController.navigate("calendar")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.PieChart, contentDescription = stringResource(R.string.nav_analytics_desc)) },
                        label = { Text(stringResource(R.string.nav_analytics_label)) },
                        selected = currentRoute == "analytics",
                        onClick = {
                            if (currentRoute != "analytics") navController.navigate("analytics")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings_desc)) },
                        label = { Text(stringResource(R.string.nav_settings_label)) },
                        selected = currentRoute == "settings",
                        onClick = {
                            if (currentRoute != "settings") navController.navigate("settings")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("onboarding") {
                OnboardingScreen(
                    onFinish = {
                        // Відмічаємо в базі, що онбординг пройдено
                        viewModel.completeOnboarding()
                        // Переходимо на дашборд і чистимо історію, щоб не можна було повернутися назад
                        navController.navigate("dashboard") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    }
                )
            }

            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onAddHabitClick = { navController.navigate("create_habit") },
                    onHabitClick = { habitId -> navController.navigate("habit_details/$habitId") }
                )
            }

            composable("create_habit") {
                CreateHabitScreen(
                    habitToEdit = null,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { name, colorHex, iconName, frequency, targetDays, finalReminderTime ->
                        viewModel.addHabit(name, colorHex, iconName, targetDays, frequency, finalReminderTime)
                        navController.popBackStack()
                    }
                )
            }

            composable("calendar") {
                CalendarScreen(viewModel = viewModel)
            }

            composable("analytics") {
                AnalyticsScreen(viewModel = viewModel)
            }

            composable("settings") {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange
                )
            }

            composable(
                route = "edit_habit/{habitId}",
                arguments = listOf(navArgument("habitId") { type = NavType.IntType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getInt("habitId") ?: return@composable

                val habits by viewModel.habits.collectAsState()
                val habit = habits.find { it.id == habitId }

                if (habit != null) {
                    CreateHabitScreen(
                        habitToEdit = habit,
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { name, colorHex, iconName, frequency, targetDays, finalReminderTime ->
                            viewModel.updateHabit(habit.id, name, colorHex, iconName, targetDays, frequency, finalReminderTime)
                            navController.popBackStack()
                        }
                    )
                }
            }

            composable(
                route = "habit_details/{habitId}",
                arguments = listOf(navArgument("habitId") { type = NavType.IntType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getInt("habitId") ?: return@composable

                HabitDetailsScreen(
                    habitId = habitId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate("edit_habit/$habitId") }
                )
            }
        }
    }
}
