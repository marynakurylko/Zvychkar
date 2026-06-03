package com.example.vibehabit.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vibehabit.screens.CreateHabitScreen
import com.example.vibehabit.screens.DashboardScreen
import com.example.vibehabit.viewmodels.HabitsViewModel

@Composable
fun AppNavigation() {
    // Це наш "роутер", який запам'ятовує поточний екран
    val navController = rememberNavController()

    // Створюємо спільну ViewModel тут, на найвищому рівні,
    // щоб дані не втрачалися при переході між екранами
    val viewModel: HabitsViewModel = viewModel()

    // NavHost — це контейнер, в якому змінюються екрани
    NavHost(navController = navController, startDestination = "dashboard") {

        // Маршрут 1: Головний екран
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onAddHabitClick = {
                    // Переходимо на екран створення
                    navController.navigate("create_habit")
                }
            )
        }

        // Маршрут 2: Екран створення звички
        composable("create_habit") {
            CreateHabitScreen(
                onBackClick = {
                    // Повертаємося назад (видаляємо екран зі стеку)
                    navController.popBackStack()
                },
                onSaveClick = { name, colorHex ->
                    viewModel.addHabit(name, colorHex)
                    navController.popBackStack()
                }
            )
        }
    }
}

