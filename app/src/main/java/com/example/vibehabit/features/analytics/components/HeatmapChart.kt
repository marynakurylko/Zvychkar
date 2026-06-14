package com.example.vibehabit.features.analytics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import java.time.LocalDate

@Composable
fun HeatmapChart(heatmapData: Map<LocalDate, Int>) {
    val weeksToDisplay = 15
    val today = LocalDate.now()

    val currentDayOfWeek = today.dayOfWeek.value
    val daysToSubtract = (weeksToDisplay - 1) * 7 + (currentDayOfWeek - 1)
    val startDate = today.minusDays(daysToSubtract.toLong())

    val weekDaysLabels = listOf(
        stringResource(R.string.day_mon),
        stringResource(R.string.day_tue),
        stringResource(R.string.day_wed),
        stringResource(R.string.day_thu),
        stringResource(R.string.day_fri),
        stringResource(R.string.day_sat),
        stringResource(R.string.day_sun)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .width(28.dp)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (day in 0..6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = weekDaysLabels.getOrNull(day) ?: "",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        style = TextStyle(
                            lineHeight = 14.sp,
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both
                            ),
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (week in 0 until weeksToDisplay) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (day in 0..6) {
                        val cellDate = startDate.plusDays((week * 7 + day).toLong())
                        val isFuture = cellDate.isAfter(today)
                        val count = heatmapData[cellDate] ?: 0

                        val cellColor = when {
                            isFuture -> Color.Transparent
                            count == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            count == 1 -> Color(0xFF00606B)
                            count == 2 -> Color(0xFF00A2B8)
                            count >= 3 -> Color(0xFF00E5FF)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cellColor)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeatmapChartPreview() {
    val dummyData = mapOf(
        LocalDate.now() to 1,
        LocalDate.now().minusDays(1) to 3,
        LocalDate.now().minusDays(2) to 0,
        LocalDate.now().minusDays(3) to 2
    )
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            HeatmapChart(heatmapData = dummyData)
        }
    }
}