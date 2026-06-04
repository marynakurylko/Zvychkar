package com.example.vibehabit.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
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

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: HabitsViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in listOf("dashboard", "calendar", "settings")) {
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
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
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
                    onSaveClick = { name, colorHex, iconName, targetDays, frequency ->
                        viewModel.addHabit(name, colorHex, iconName, targetDays, frequency)
                        navController.popBackStack()
                    }
                )
            }

            composable("calendar") {
                CalendarScreen(viewModel = viewModel)
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
                        onSaveClick = { name, colorHex, iconName, targetDays, frequency ->
                            // Corrected argument order to fix type mismatch
                            viewModel.updateHabit(habit.id, name, colorHex, iconName, targetDays, frequency)
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