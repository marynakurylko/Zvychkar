package com.example.vibehabit.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeonProgressRing(
    progress: Float, // Від 0.0 до 1.0
    color: Color,
    modifier: Modifier = Modifier,
    centerText: String = "",
    subtitle: String = ""
) {
    // Плавна анімація заповнення (1.5 секунди)
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(240.dp) // Великий розмір для екрана деталей
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val glowWidth = 32.dp.toPx() // Ширина "світіння"
            val sizePx = size.minDimension
            val radius = (sizePx - glowWidth) / 2f

            // 1. Темний фоновий трек
            drawArc(
                color = Color.DarkGray.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Внутрішнє високотехнологічне пунктирне кільце (Tech Dashes)
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = radius * 0.70f, // Менший радіус, щоб бути всередині
                style = Stroke(
                    width = 2.dp.toPx(),
                    // Створюємо пунктир: 10px лінія, 20px пропуск
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f)
                )
            )

            // Зсуваємо початок на -90 градусів (щоб заповнення йшло зверху, як у годиннику)
            val startAngle = -90f
            val sweepAngle = 360 * animatedProgress

            if (animatedProgress > 0f) {
                // 3. Неонове світіння (Glow) - товсте і напівпрозоре
                drawArc(
                    color = color.copy(alpha = 0.35f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = glowWidth, cap = StrokeCap.Round)
                )

                // 4. Основна яскрава лінія (Ядро неону)
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Текст всередині кільця
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerText,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}