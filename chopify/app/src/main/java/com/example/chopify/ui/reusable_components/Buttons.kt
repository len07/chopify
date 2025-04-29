package com.example.chopify.ui.reusable_components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chopify.ui.theme.DarkGreen
import com.example.chopify.ui.theme.LightGreen

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    icon: Painter? = null,
    containerColor: Color = DarkGreen,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier
            .width(300.dp)
            .height(70.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(fontSize = 18.sp),
            )
            if (icon != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        painter = icon,
                        contentDescription = "Icon",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

// Not currently in use as sign in with google has been disabled
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    icon: Painter? = null,
    containerColor: Color = LightGreen,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier
            .width(300.dp)
            .height(70.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (icon != null) Arrangement.Start else Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    painter = icon,
                    contentDescription = "Icon",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = text,
                style = TextStyle(fontSize = 18.sp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
