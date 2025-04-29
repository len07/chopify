package com.example.chopify.ui.screens

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chopify.models.User
import com.example.chopify.models.UserPreferences
import com.example.chopify.services.LoginService
import com.example.chopify.services.NotificationService
import com.example.chopify.services.UserService
import com.example.chopify.ui.reusable_components.EmailAccountField
import com.example.chopify.ui.reusable_components.NameAccountField
import com.example.chopify.ui.reusable_components.PasswordAccountField
import com.example.chopify.ui.theme.DarkGreen
import com.example.chopify.ui.theme.LettuceGreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SettingScreen(
    loginService: LoginService,
    userService: UserService,
    navController: NavController,
    onLogoutSuccess: () -> Unit,
    userPreferences: UserPreferences,
    context: Context
) {
    val user = Firebase.auth.currentUser
    var currentAccountFields by remember { mutableStateOf(User()) }

    var isEditingName by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }

    var isEditingPassword by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var reTypePassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val notificationsEnabled by userPreferences.notificationsEnabledFlow.collectAsState(initial = true)
    var expandedDayPicker by remember { mutableStateOf(false) }
    val selectedExpiryDays by userPreferences.selectedDaysFlow.collectAsState(initial = 3)
    val coroutineScope = rememberCoroutineScope()

    var demoModeEnabled by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    var notifTitle by remember { mutableStateOf("") }
    var notifMessage by remember { mutableStateOf("") }
    var notifDate by remember { mutableStateOf(LocalDate.now()) }
    var notifTime by remember { mutableStateOf(LocalTime.now()) }

    LaunchedEffect(Unit) {
        if (user != null) {
            userService.getUser(
                userId = user.uid,
                onSuccess = { user ->
                    if (user != null) {
                        currentAccountFields = user
                    }
                },
                onFailure = { exception ->
                    Log.e(
                        "SettingScreen",
                        "Error fetching user: ${exception.message}"
                    )
                }
            )
        }
    }

    fun showNotificationConfirmation(context: Context) {
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())


        AlertDialog.Builder(context)
            .setTitle("Notification Scheduled")
            .setMessage(
                "Title: " + notifTitle +
                        "\nMessage: " + notifMessage +
                        "\nAt: " + notifDate.format(dateFormatter) + " " + notifTime.format(
                    timeFormatter
                )
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Settings",
                color = DarkGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Section(title = "Account") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Email", fontSize = 16.sp, modifier = Modifier.weight(1f))
                    EmailAccountField(value = currentAccountFields.email)
                }

                Spacer(modifier = Modifier.height(8.dp))

                NameAccountField(
                    currentName = currentAccountFields.name,
                    newName = newName,
                    isEditing = isEditingName,
                    onEditChange = { isEditingName = it },
                    onNewNameChange = { newName = it },
                    onSave = {
                        if (newName == "") {
                            nameError = "Name cannot be empty."
                        } else {
                            userService.updateName(
                                userId = user?.uid ?: "",
                                newName = newName,
                                onSuccess = {
                                    currentAccountFields =
                                        User(
                                            userID = currentAccountFields.userID,
                                            email = currentAccountFields.email,
                                            name = newName
                                        )
                                    newName = ""
                                    nameError = null
                                    isEditingName = false
                                },
                                onFailure = { exception ->
                                    nameError =
                                        "Failed to update name: ${exception.message}"
                                }
                            )
                        }
                    },
                    onCancel = {
                        newName = ""
                        nameError = null
                        isEditingName = false
                    }
                )

                if (nameError != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = nameError ?: "",
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                PasswordAccountField(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    reTypePassword = reTypePassword,
                    isEditing = isEditingPassword,
                    onEditChange = { isEditingPassword = it },
                    onCurrentPasswordChange = { currentPassword = it },
                    onNewPasswordChange = { newPassword = it },
                    onReTypePasswordChange = { reTypePassword = it },
                    onSave = {
                        if (newPassword == "" || reTypePassword == "") {
                            passwordError = "New password cannot be empty"
                        } else if (newPassword != reTypePassword) {
                            passwordError = "New passwords don't match"
                        } else {
                            userService.updatePassword(
                                userId = user?.uid ?: "",
                                currentPassword = currentPassword,
                                newPassword = newPassword,
                                onSuccess = {
                                    currentPassword = ""
                                    newPassword = ""
                                    reTypePassword = ""
                                    passwordError = null
                                    isEditingPassword = false
                                },
                                onFailure = { exception ->
                                    passwordError =
                                        "Failed to update name: ${exception.message}"
                                }
                            )
                        }
                    },
                    onCancel = {
                        currentPassword = ""
                        newPassword = ""
                        reTypePassword = ""
                        passwordError = null
                        isEditingPassword = false
                    }
                )

                if (passwordError != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = passwordError ?: "",
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Section(title = "Notifications") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Enable Notifications", fontSize = 16.sp)
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                coroutineScope.launch {
                                    userPreferences.saveNotificationsEnabled(it)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = LettuceGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    if (notificationsEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Notify me daily up to",
                                fontSize = 16.sp
                            )
                            Text(
                                text = "$selectedExpiryDays day${if (selectedExpiryDays > 1) "s" else ""}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = DarkGreen,
                                modifier = Modifier
                                    .clickable { expandedDayPicker = true }
                                    .padding(8.dp)
                                    .background(
                                        DarkGreen.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 4.dp
                                    )
                            )
                            Text(
                                text = "before expiration",
                                fontSize = 16.sp
                            )
                        }

                        DropdownMenu(
                            expanded = expandedDayPicker,
                            onDismissRequest = { expandedDayPicker = false },
                            offset = DpOffset(x = 150.dp, y = 0.dp),
                            modifier = Modifier
                                .width(100.dp)
                        ) {
                            (1..7).forEach { days ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "$days day${if (days > 1) "s" else ""}",
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    },
                                    onClick = {
                                        coroutineScope.launch {
                                            userPreferences.saveSelectedDays(days) // Save to DataStore
                                        }
                                        expandedDayPicker = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Section(title = "Developer") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Enable Demo Mode", fontSize = 16.sp)
                        Switch(
                            checked = demoModeEnabled,
                            onCheckedChange = { demoModeEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = LettuceGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }

                    if (demoModeEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(text = "Mock Notification", fontSize = 16.sp)

                        TextField(
                            value = notifTitle,
                            onValueChange = { notifTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = notifMessage,
                            onValueChange = { notifMessage = it },
                            label = { Text("Message") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { showDatePickerDialog = true },
                                colors = ButtonColors(
                                    containerColor = Color.Gray,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.Gray,
                                    disabledContentColor = Color.White
                                )
                            ) {
                                Text("Select Date")
                            }

                            Button(
                                onClick = { showTimePickerDialog = true },
                                colors = ButtonColors(
                                    containerColor = Color.Gray,
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.Gray,
                                    disabledContentColor = Color.White
                                )
                            ) {
                                Text("Select Time")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Schedule Notification Button
                        Button(
                            onClick = {
                                val zoneId = ZoneId.systemDefault()
                                val time = LocalDateTime.of(notifDate, notifTime).atZone(zoneId)
                                    .toInstant().toEpochMilli()
                                NotificationService.scheduleNotification(
                                    context,
                                    notifTitle,
                                    notifMessage,
                                    time
                                )
                                showNotificationConfirmation(context)
                                notifTitle = ""
                                notifMessage = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonColors(
                                containerColor = DarkGreen,
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.White
                            )
                        ) {
                            Text("Schedule Notification")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Log out",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        loginService.logoutUser(
                            onSuccess = {
                                onLogoutSuccess()
                            },
                            onFailure = { exception ->
                            }
                        )
                    }
                    .padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    if (showDatePickerDialog) {
        DatePickerDialog(
            LocalContext.current,
            { _, year, month, dayOfMonth ->
                notifDate = LocalDate.of(year, month + 1, dayOfMonth)
                showDatePickerDialog = false
            },
            notifDate.year,
            notifDate.monthValue - 1,
            notifDate.dayOfMonth
        ).show()
    }

    if (showTimePickerDialog) {
        TimePickerDialog(
            LocalContext.current,
            { _, hourOfDay, minute ->
                notifTime = LocalTime.of(hourOfDay, minute)
                showTimePickerDialog = false
            },
            notifTime.hour,
            notifTime.minute,
            true
        ).show()
    }
}

@Composable
fun Section(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        content()
        HorizontalDivider()
    }
}
