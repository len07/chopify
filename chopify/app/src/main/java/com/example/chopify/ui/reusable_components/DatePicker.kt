package com.example.chopify.ui.reusable_components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.chopify.ui.theme.DarkGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDocked(expiryDate: String? = null): String {
    var showDatePicker by remember { mutableStateOf(false) }
    var newExpiryDate by remember { mutableStateOf(expiryDate ?: "") }
    val initialDateMillis = remember {
        expiryDate?.takeIf { it.isNotBlank() }?.let {
            val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            formatter.timeZone = TimeZone.getDefault()
            formatter.parse(it)?.time
        }
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    // convert milliseconds to local date
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            val updatedMillis = it + 24 * 60 * 60 * 1000
            newExpiryDate = convertMillisToDate(updatedMillis)
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedLabelColor = DarkGreen,
                cursorColor = DarkGreen,
                selectionColors = TextSelectionColors(
                    handleColor = DarkGreen,
                    backgroundColor = DarkGreen.copy(alpha = 0.4f)
                )
            ),
            value = newExpiryDate,
            onValueChange = {},
            label = { Text("Expiry Date") },
            textStyle = TextStyle(fontSize = 17.sp),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White, shape = RoundedCornerShape(10.dp)),
        )

        if (showDatePicker) {
            Popup(
                alignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable { showDatePicker = false }
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .height(395.dp)
                            .shadow(elevation = 4.dp)
                            .background(Color(0xFFCFE0D5), shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        DatePicker(
                            state = datePickerState,
                            title = null,
                            headline = null,
                            showModeToggle = false,
                            colors = DatePickerColors(
                                selectedDayContainerColor = DarkGreen,
                                containerColor = Color.White,
                                titleContentColor = DarkGreen,
                                headlineContentColor = DarkGreen,
                                weekdayContentColor = DarkGreen,
                                subheadContentColor = DarkGreen,
                                navigationContentColor = DarkGreen,
                                yearContentColor = DarkGreen,
                                disabledYearContentColor = DarkGreen,
                                currentYearContentColor = DarkGreen,
                                selectedYearContentColor = Color.White,
                                disabledSelectedYearContentColor = Color.Gray,
                                selectedYearContainerColor = DarkGreen,
                                disabledSelectedYearContainerColor = DarkGreen,
                                dayContentColor = DarkGreen,
                                disabledDayContentColor = Color.Gray,
                                selectedDayContentColor = Color.White,
                                disabledSelectedDayContentColor = Color.Gray,
                                disabledSelectedDayContainerColor = Color.Gray,
                                todayContentColor = Color.Gray,
                                todayDateBorderColor = DarkGreen,
                                dayInSelectionRangeContainerColor = DarkGreen,
                                dayInSelectionRangeContentColor = DarkGreen,
                                dividerColor = DarkGreen,
                                dateTextFieldColors = TextFieldColors(
                                    unfocusedContainerColor = Color.White,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Gray,
                                    disabledTextColor = Color.LightGray,
                                    errorTextColor = Color.Red,
                                    focusedContainerColor = Color.LightGray,
                                    disabledContainerColor = Color.LightGray,
                                    errorContainerColor = Color.Red.copy(alpha = 0.1f),
                                    cursorColor = Color.Black,
                                    errorCursorColor = Color.Red,
                                    textSelectionColors = TextSelectionColors(
                                        handleColor = Color.Blue,
                                        backgroundColor = Color.Blue.copy(alpha = 0.3f)
                                    ),
                                    focusedIndicatorColor = DarkGreen,
                                    unfocusedIndicatorColor = Color.Gray,
                                    disabledIndicatorColor = Color.Gray,
                                    errorIndicatorColor = Color.Red,
                                    focusedLeadingIconColor = Color.Green,
                                    unfocusedLeadingIconColor = Color.Gray,
                                    disabledLeadingIconColor = Color.LightGray,
                                    errorLeadingIconColor = Color.Red,
                                    focusedTrailingIconColor = DarkGreen,
                                    unfocusedTrailingIconColor = Color.Gray,
                                    disabledTrailingIconColor = Color.LightGray,
                                    errorTrailingIconColor = Color.Red,
                                    focusedLabelColor = Color.Black,
                                    unfocusedLabelColor = Color.Gray,
                                    disabledLabelColor = Color.LightGray,
                                    errorLabelColor = Color.Red,
                                    focusedPlaceholderColor = Color.Gray,
                                    unfocusedPlaceholderColor = Color.LightGray,
                                    disabledPlaceholderColor = Color.LightGray,
                                    errorPlaceholderColor = Color.Red,
                                    focusedSupportingTextColor = DarkGreen,
                                    unfocusedSupportingTextColor = Color.Gray,
                                    disabledSupportingTextColor = Color.LightGray,
                                    errorSupportingTextColor = Color.Red,
                                    focusedPrefixColor = DarkGreen,
                                    unfocusedPrefixColor = Color.Gray,
                                    disabledPrefixColor = Color.LightGray,
                                    errorPrefixColor = Color.Red,
                                    focusedSuffixColor = Color.Green,
                                    unfocusedSuffixColor = Color.Gray,
                                    disabledSuffixColor = Color.LightGray,
                                    errorSuffixColor = Color.Red
                                )
                            )
                        )
                        IconButton(
                            onClick = {
                                showDatePicker = false
                            },
                            colors = IconButtonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black,
                                disabledContainerColor = Color.White,
                                disabledContentColor = Color.Black
                            ),
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Icon(
                                imageVector = Icons.Sharp.Check,
                                contentDescription = "confirm",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    return newExpiryDate
}

fun convertMillisToDate(millis: Long): String {
    val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    dateFormat.timeZone = TimeZone.getDefault()
    return dateFormat.format(Date(millis))
}