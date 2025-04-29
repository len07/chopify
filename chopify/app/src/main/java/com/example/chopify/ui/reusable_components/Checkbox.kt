package com.example.chopify.ui.reusable_components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.chopify.ui.theme.DarkGreen

// Not currently in use as sign in with Google has been disabled
@Composable
fun Checkbox() {
    var isChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(280.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = DarkGreen
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("By clicking, I agree to the terms and conditions", color = Color(0xFF888888))
        }
    }
}
