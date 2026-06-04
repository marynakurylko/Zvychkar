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

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: HabitsViewModel = viewModel()

    // Слідкуємо за тим, на якому екрані ми зараз знаходимося
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Головний каркас з нижньою панеллю
    Scaffold(
        bottomBar = {
            // Показуємо панель тільки на головних екранах
            if (currentRoute in listOf("dashboard", "calendar", "settings")) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    // Вкладка "Головна"
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                        label = { Text("Звички") },
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
                    // Вкладка "Календар" (Поки що пуста, просто заглушка маршруту)
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.DateRange, contentDescription = "Calendar") },
                        label = { Text("Календар") },
                        selected = currentRoute == "calendar",
                        onClick = {
                            if (currentRoute != "calendar") navController.navigate("calendar")
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    // Вкладка "Налаштування"
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Налаштування") },
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
        // Наш роутер тепер враховує висоту нижньої панелі (innerPadding)
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding) // Важливо! Щоб контент не ховався за панеллю
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
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { name, colorHex ->
                        viewModel.addHabit(name, colorHex)
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
                route = "habit_details/{habitId}", // Вказуємо, що чекаємо параметр
                arguments = listOf(navArgument("habitId") { type = NavType.IntType }) // Кажемо, що це число (Int)
            ) { backStackEntry ->
                // Витягуємо ID з роутера
                val habitId = backStackEntry.arguments?.getInt("habitId") ?: return@composable

                HabitDetailsScreen(
                    habitId = habitId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}