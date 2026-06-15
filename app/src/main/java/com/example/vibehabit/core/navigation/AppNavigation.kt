package com.example.vibehabit.core.navigation

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.vibehabit.features.auth.AuthViewModel
import com.example.vibehabit.features.dashboard.DashboardViewModel
import com.example.vibehabit.features.settings.SettingsScreen
import com.example.vibehabit.features.habit_details.HabitDetailsScreen
import com.example.vibehabit.features.settings.ProfileViewModel
import kotlinx.serialization.Serializable

@Serializable object SignInRoute
@Serializable object OnboardingRoute
@Serializable object DashboardRoute
@Serializable object CreateHabitRoute
@Serializable object CalendarRoute
@Serializable object AnalyticsRoute
@Serializable object SettingsRoute
@Serializable data class EditHabitRoute(val habitId: String)
@Serializable data class HabitDetailsRoute(val habitId: String)

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destination = navBackStackEntry?.destination

    val isOnboardingCompleted by profileViewModel.isOnboardingCompleted.collectAsState()
    val authState by authViewModel.authState.collectAsState()

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

    var wasBottomBarVisible by rememberSaveable { mutableStateOf(false) }
    val isCurrentDestinationVisible = destination?.let {
        it.hasRoute<DashboardRoute>() || it.hasRoute<CalendarRoute>() ||
                it.hasRoute<AnalyticsRoute>() || it.hasRoute<SettingsRoute>()
    }
    if (isCurrentDestinationVisible != null) {
        wasBottomBarVisible = isCurrentDestinationVisible
    }
    val showBottomBar = isCurrentDestinationVisible ?: wasBottomBarVisible

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    NavigationBar(
                        modifier = Modifier.height(80.dp),
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.nav_home_desc)) },
                            label = { Text(text = stringResource(R.string.nav_habits_label), maxLines = 1, modifier = Modifier.basicMarquee()) },
                            selected = destination?.hasRoute<DashboardRoute>() == true,
                            onClick = {
                                if (destination?.hasRoute<DashboardRoute>() == false) {
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
                            label = { Text(text = stringResource(R.string.nav_calendar_label), maxLines = 1, modifier = Modifier.basicMarquee()) },
                            selected = destination?.hasRoute<CalendarRoute>() == true,
                            onClick = {
                                if (destination?.hasRoute<CalendarRoute>() == false) navController.navigate(CalendarRoute)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )

                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.PieChart, contentDescription = stringResource(R.string.nav_analytics_desc)) },
                            label = { Text(text = stringResource(R.string.nav_analytics_label), maxLines = 1, modifier = Modifier.basicMarquee()) },
                            selected = destination?.hasRoute<AnalyticsRoute>() == true,
                            onClick = {
                                if (destination?.hasRoute<AnalyticsRoute>() == false) navController.navigate(AnalyticsRoute)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )

                        NavigationBarItem(
                            icon = { Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_settings_desc)) },
                            label = { Text(text = stringResource(R.string.nav_settings_label), maxLines = 1, modifier = Modifier.basicMarquee()) },
                            selected = destination?.hasRoute<SettingsRoute>() == true,
                            onClick = {
                                if (destination?.hasRoute<SettingsRoute>() == false) navController.navigate(SettingsRoute)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        )
                    }
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
                SignInScreen(viewModel = authViewModel)

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
                        profileViewModel.completeOnboarding()
                        navController.navigate(DashboardRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    }
                )
            }

            composable<DashboardRoute> {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel,
                    onAddHabitClick = { navController.navigate(CreateHabitRoute) },
                    onHabitClick = { habitId -> navController.navigate(HabitDetailsRoute(habitId)) }
                )
            }

            composable<CreateHabitRoute> {
                CreateHabitScreen(
                    habitToEdit = null,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable<CalendarRoute> {
                CalendarScreen(viewModel = dashboardViewModel)
            }

            composable<AnalyticsRoute> {
                AnalyticsScreen(viewModel = dashboardViewModel)
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    dashboardViewModel = dashboardViewModel,
                    authViewModel = authViewModel,
                    profileViewModel = profileViewModel
                )
            }

            composable<EditHabitRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<EditHabitRoute>()

                val habitsState by dashboardViewModel.habitsState.collectAsState()
                val habits = (habitsState as? UiState.Success)?.data ?: emptyList()
                val habit = habits.find { it.id == args.habitId }

                if (habit != null) {
                    CreateHabitScreen(
                        habitToEdit = habit,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }

            composable<HabitDetailsRoute> { backStackEntry ->
                val args = backStackEntry.toRoute<HabitDetailsRoute>()

                HabitDetailsScreen(
                    habitId = args.habitId,
                    viewModel = dashboardViewModel,
                    onBackClick = { navController.popBackStack() },
                    onEditClick = { navController.navigate(EditHabitRoute(args.habitId)) }
                )
            }
        }
    }
}
