package com.example.vibehabit.features.dashboard.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R

@Composable
fun DailySummaryCard(
    dateText: String,
    completedCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val targetProgress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "DailyProgress"
    )

    val accentColor = if (completedCount == totalCount && totalCount > 0) {
        Color(0xFF00FF7F)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.today_label),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = stringResource(R.string.progress_format, completedCount, totalCount),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            ) {
                val trackWidth = size.width
                val progressWidth = trackWidth * animatedProgress
                val strokeW = 8.dp.toPx()
                val glowW = 20.dp.toPx()
                val yOffset = size.height / 2

                drawLine(
                    color = Color.Black.copy(alpha = 0.3f),
                    start = Offset(0f, yOffset),
                    end = Offset(trackWidth, yOffset),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )

                if (animatedProgress > 0f) {
                    drawLine(
                        color = accentColor.copy(alpha = 0.4f),
                        start = Offset(0f, yOffset),
                        end = Offset(progressWidth, yOffset),
                        strokeWidth = glowW,
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = accentColor,
                        start = Offset(0f, yOffset),
                        end = Offset(progressWidth, yOffset),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DailySummaryCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DailySummaryCard(
                dateText = "12 Червня",
                completedCount = 3,
                totalCount = 5
            )
        }
    }
}