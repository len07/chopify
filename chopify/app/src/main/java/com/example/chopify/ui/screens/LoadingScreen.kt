package com.example.chopify.ui.screens

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chopify.R
import com.example.chopify.ui.theme.LettuceGreen

@Composable
fun LoadingScreen() {
    var progress by remember { mutableStateOf(0f) }
    val progressAnim by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 4000, easing = LinearOutSlowInEasing),
        label = "progressAnimation"
    )

    LaunchedEffect(Unit) {
        progress = 1f  // Animate to full width
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painterResource(R.drawable.chopifylogo),
                contentDescription = "null",
                modifier = Modifier
                    .width(202.dp)
                    .height(126.dp)
            )
            Spacer(modifier = Modifier.height(60.dp))
            LinearProgressIndicator(
                progress = { progressAnim },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp) // Thicker bar
                    .clip(RoundedCornerShape(6.dp)),
                color = LettuceGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Scanning...", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}
