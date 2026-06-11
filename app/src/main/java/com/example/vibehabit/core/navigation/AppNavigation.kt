package com.example.vibehabit.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.vibehabit.R
import com.example.vibehabit.core.ui.UiState
import com.example.vibehabit.features.auth.AuthState
import com.example.vibehabit.features.auth.SignInScreen
import com.example.vibehabit.features.onboarding.OnboardingScreen
import com.example.vibehabit.features.dashboard.DashboardScreen
import com.example.vibehabit.features.create_habit.CreateHabitScreen
import com.example.vibehabit.features.calendar.CalendarScreen
import com.example.vibehabit.features.analytics.AnalyticsScreen
import com.example.vibehabit.features.settings.SettingsScreen
import com.example.vibehabit.features.habit_details.HabitDetailsScreen
import com.example.vibehabit.shared_viewmodels.HabitsViewModel
import kotlinx.serialization.Serializable

// 1. ВИЗНАЧАЄМО ТИПІЗОВАНІ МАРШРУТИ ЗАМІСТЬ СТРІНГОВИХ КОНСТАНТ
@Serializable object SignInRoute
@Serializable object OnboardingRoute
@Serializable object DashboardRoute
@Serializable object CreateHabitRoute
@Serializable object CalendarRoute
@Serializable object AnalyticsRoute
@Serializable object SettingsRoute
@Serializable data class EditHabitRoute(val habitId: Int)
@Serializable data class HabitDetailsRoute(val habitId: Int)

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val viewModel: HabitsViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destination = navBackStackEntry?.destination

    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated && destination?.hasRoute<SignInRoute>() == false) {
            navController.navigate(SignInRoute) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    if (isOnboardingCompleted == null) return

    val startDestination: Any = when {
        authState is AuthState.Unauthenticated -> SignInRoute
        isOnboardingCompleted == false -> OnboardingRoute
        else -> DashboardRoute
    }

    // Перевіряємо за допомогою Type Safety, чи треба показувати BottomBar
    val showBottomBar = destination?.run {
        hasRoute<DashboardRoute>() || hasRoute<CalendarRoute>() || hasRoute<AnalyticsRoute>() || hasRoute<SettingsRoute>()
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar && destination != null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.nav_home_desc)) },
                        label = { Text(stringResource(R.string.nav_habits_label)) },
                        selected = destination.hasRoute<DashboardRoute>(),
                        onClick = {
                            if (!destination.hasRoute<DashboardRoute>()) {
                                navController.navigate(DashboardRoute) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
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
                        selected = destination.hasRoute<CalendarRoute>(),
                        onClick = {
                            if (!destination.hasRoute<CalendarRoute>()) navController.navigate(CalendarRoute)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.PieChart, contentDescription = stringResource(R.string.nav_analytics_desc)) },
                        label = { Text(stringResource(R.string.nav_analytics_label)) },
                        selected = destination.hasRoute<AnalyticsRoute>(),
                        onClick = {
                            if (!destination.hasRoute<AnalyticsRoute>()) navController.navigate(AnalyticsRoute)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings_desc)) },
                        label = { Text(stringResource(R.string.nav_settings_label)) },
                        selected = destination.hasRoute<SettingsRoute>(),
                        onClick = {
                            if (!destination.hasRoute<SettingsRoute>()) navController.navigate(SettingsRoute)
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
            composable<SignInRoute> {
                SignInScreen(viewModel = viewModel)

                LaunchedEffect(authState) {
                    if (authState is AuthState.Authenticated) {
                        val dest = if (isOnboardingCompleted == true) DashboardRoute else OnboardingRoute
                        navController.navigate(dest) {
                            popUpTo(SignInRoute) { inclusive = true }
                        }
                    }
                }
            }

            composable<OnboardingRoute> {
                OnboardingScreen(
                    onFinish = {
                        viewModel.completeOnboarding()
                        navController.navigate(DashboardRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    }
                )
            }

            composable<DashboardRoute> {
                DashboardScreen(
                    viewModel = viewModel,
                    onAddHabitClick = { navController.navigate(CreateHabitRoute) },
                    onHabitClick = { habitId -> navController.navigate(HabitDetailsRoute(habitId)) }
                )
            }

            composable<CreateHabitRoute> {
                CreateHabitScreen(
                    habitToEdit = null,
                    onBackClick = { navController.popBackStack() },
                    onSaveClick = { name, colorHex, iconName, frequency, targetDays, finalReminderTime ->
                        viewModel.addHabit(name, colorHex, iconName, targetDays, frequency, finalReminderTime)
                    },
                    viewModel = viewModel
                )
            }

            composable<CalendarRoute> {
                CalendarScreen(viewModel = viewModel)
            }

            composable<AnalyticsRoute> {
                AnalyticsScreen(viewModel = viewModel)
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    habitsViewModel = viewModel
                )
            }

            composable<EditHabitRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<EditHabitRoute>()

                val habitsState by viewModel.habitsState.collectAsState()
                val habits = (habitsState as? UiState.Success)?.data ?: emptyList()
                val habit = habits.find { it.id == args.habitId }

                if (habit != null) {
                    CreateHabitScreen(
                        habitToEdit = habit,
                        onBackClick = { navController.popBackStack() },
                        onSaveClick = { name, colorHex, iconName, frequency, targetDays, finalReminderTime ->
                            viewModel.updateHabit(habit.id, name, colorHex, iconName, targetDays, frequency, finalReminderTime)
                        },
                        viewModel = viewModel
                    )
                }
            }

            composable<HabitDetailsRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<HabitDetailsRoute>()

                HabitDetailsScreen(
                    habitId = args.habitId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(EditHabitRoute(args.habitId)) }
                )
            }
        }
    }
}
