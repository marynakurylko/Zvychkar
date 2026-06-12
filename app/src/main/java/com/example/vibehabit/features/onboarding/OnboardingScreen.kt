package com.example.vibehabit.features.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibehabit.R
import kotlinx.coroutines.launch

// Дані для кожної сторінки
data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_1),
            description = stringResource(R.string.onboarding_desc_1),
            icon = Icons.Filled.Bolt,
            color = Color(0xFF00E5FF) // Cyan
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_2),
            description = stringResource(R.string.onboarding_desc_2),
            icon = Icons.Filled.LocalFireDepartment,
            color = Color(0xFFFF9800) // Orange
        ),
        OnboardingPage(
            title = stringResource(R.string.onboarding_title_3),
            description = stringResource(R.string.onboarding_desc_3),
            icon = Icons.Filled.NotificationsActive,
            color = Color(0xFF9D4EDD) // Purple
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onFinish()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onFinish) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { position ->
                val page = pages[position]
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(page.color.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = page.icon,
                            contentDescription = null,
                            tint = page.color,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = page.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = page.description,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // (Dots)
            Row(
                modifier = Modifier.padding(vertical = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pages.size) { iteration ->
                    val isSelected = pagerState.currentPage == iteration
                    val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp)

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            val isLastPage = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    if (isLastPage) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onFinish()
                        }
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isLastPage) stringResource(R.string.onboarding_start) else stringResource(R.string.onboarding_next),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
