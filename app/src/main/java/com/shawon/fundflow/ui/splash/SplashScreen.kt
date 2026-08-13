package com.shawon.fundflow.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shawon.fundflow.R
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToBudgetSetup: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        viewModel.navigationEvent.collect { event ->
            when (event) {
                SplashNavigation.ToDashboard -> onNavigateToDashboard()
                SplashNavigation.ToOnboarding -> onNavigateToOnboarding()
                SplashNavigation.ToBudgetSetup -> onNavigateToBudgetSetup()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Flying Money Animation Background
        if (visible) {
            for (i in 0 until 18) {
                key(i) {
                    FlyingMoneyIcon()
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1200)) + scaleIn(initialScale = 0.8f, animationSpec = tween(1200))
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.main_logo),
                        contentDescription = "FundFlow Logo",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000, 500)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(1000, 500)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FundFlow",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )

                    Text(
                        text = "Smart Budget. Smarter Life.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Text(
            text = "Developed by\nShawon Talukder",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(if (visible) 0.8f else 0f),
            style = MaterialTheme.typography.labelLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FlyingMoneyIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "flying_money")
    
    val randomXBase = remember { Random.nextFloat() }
    val randomDelay = remember { Random.nextInt(0, 2000) }
    val randomDuration = remember { Random.nextInt(4000, 7000) }
    val icon = remember { listOf("💸", "💵", "💰", "🪙", "💹").random() }
    val randomSize = remember { Random.nextInt(20, 36).sp }

    val yOffset by infiniteTransition.animateFloat(
        initialValue = 1200f,
        targetValue = -300f,
        animationSpec = infiniteRepeatable(
            animation = tween(randomDuration, easing = LinearEasing, delayMillis = randomDelay),
            repeatMode = RepeatMode.Restart
        ),
        label = "y_offset"
    )

    val xSway by infiniteTransition.animateFloat(
        initialValue = -40f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x_sway"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = randomDuration
                0f at 0
                0.5f at (randomDuration / 3)
                0.5f at (2 * randomDuration / 3)
                0f at randomDuration
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(
                x = (randomXBase * 400 - 200).dp + xSway.dp,
                y = yOffset.dp
            )
            .alpha(alpha)
    ) {
        Text(icon, fontSize = randomSize)
    }
}
