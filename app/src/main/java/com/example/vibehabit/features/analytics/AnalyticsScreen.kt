package com.example.vibehabit.features.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import com.example.vibehabit.features.analytics.components.HeatmapChart
import com.example.vibehabit.core.ui.UiState
import com.example.vibehabit.features.dashboard.DashboardViewModel
import com.example.vibehabit.shared_viewmodels.HabitsViewModel
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: DashboardViewModel) {
    val habitsState by viewModel.habitsState.collectAsState()
    val habits = (habitsState as? UiState.Success)?.data ?: emptyList()
    val heatmapData by viewModel.heatmapStats.collectAsState()

    val totalCompletedTasks = habits.sumOf { it.completedDates.size }
    val bestGlobalStreak = habits.maxOfOrNull { it.bestStreak } ?: 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.analytics_title), fontWeight = FontWeight.Bold, fontSize = 20.sp, maxLines = 1, modifier = Modifier.basicMarquee()) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stat_total_completed),
                    value = totalCompletedTasks.toString(),
                    icon = Icons.Filled.CheckCircle,
                    color = Color(0xFF00FF7F)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = stringResource(R.string.stat_max_streak),
                    value = stringResource(R.string.streak_format, bestGlobalStreak),
                    icon = Icons.Filled.LocalFireDepartment,
                    color = Color(0xFFFF9800)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.activity_history),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                HeatmapChart(heatmapData = heatmapData)
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, color: Color) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
        Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(text = title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}