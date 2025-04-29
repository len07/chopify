package com.example.chopify.ui.reusable_components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chopify.ui.theme.DarkGreen

@Composable
fun LoginField(
    value: String, onChange: (String) -> Unit,
    placeholder: String = "Email Address"
) {
    val focusManager = LocalFocusManager.current

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
        value = value,
        singleLine = true,
        textStyle = TextStyle(fontSize = 17.sp),
        onValueChange = onChange,
        modifier = Modifier
            .width(300.dp)
            .height(50.dp)
            .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp)),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF888888)
            )
        },
        visualTransformation = VisualTransformation.None,
    )
}

@Composable
fun PasswordField(password: String, onPasswordChange: (String) -> Unit, label: String) {
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
        textStyle = TextStyle(fontSize = 17.sp),
        value = password,
        singleLine = true,
        onValueChange = onPasswordChange,
        modifier = Modifier
            .width(300.dp)
            .height(50.dp)
            .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp)),
        label = { Text(label, color = Color(0xFF888888)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = PasswordVisualTransformation(),
    )
}

@Composable
fun InputField(value: String, onChange: (String) -> Unit) {
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
        value = value,
        singleLine = true,
        textStyle = TextStyle(fontSize = 17.sp),
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .width(150.dp)
            .height(50.dp)
            .background(Color.White, shape = RoundedCornerShape(10.dp)),
        label = { Text("Item Name") },
    )
}

@Composable
fun EditableLabel(
    value: String,
    onChange: (String) -> Unit
) {
    TextField(
        value = value,
        singleLine = true,
        textStyle = TextStyle(fontSize = 17.sp),
        onValueChange = onChange,
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
        modifier = Modifier
            .fillMaxWidth()
            .width(150.dp)
            .height(50.dp)
            .background(Color.White, shape = RoundedCornerShape(10.dp)),
    )
}

@Composable
fun EmailAccountField(value: String) {
    TextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 16.sp,
            color = Color.Gray
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
            .width(225.dp)
            .height(50.dp)
            .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp))
    )
}

@Composable
fun NameAccountField(
    currentName: String,
    newName: String,
    isEditing: Boolean,
    onEditChange: (Boolean) -> Unit,
    onNewNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Current Name" else "Name",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(225.dp)
                    .height(50.dp)
                    .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentName,
                        fontSize = 16.sp,
                        color = if (isEditing) Color.Gray else Color.Black
                    )

                    if (!isEditing) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Name",
                            tint = Color.Gray,
                            modifier = Modifier
                                .clickable { onEditChange(true) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "New Name", fontSize = 16.sp, modifier = Modifier.weight(1f))

                TextField(
                    value = newName,
                    onValueChange = onNewNameChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .width(225.dp)
                        .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp)),
                    placeholder = { Text(text = "Enter new name", color = Color.Gray) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Save",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .background(DarkGreen, shape = RoundedCornerShape(8.dp))
                        .clickable { onSave() }
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Cancel",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .clickable { onCancel() }
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PasswordAccountField(
    currentPassword: String,
    newPassword: String,
    reTypePassword: String,
    isEditing: Boolean,
    onEditChange: (Boolean) -> Unit,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onReTypePasswordChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Current Password" else "Password",
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            if (isEditing) {
                // editable password field
                TextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .width(225.dp)
                        .height(50.dp)
                        .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp)),
                    placeholder = {
                        Text(text = "Enter current password", color = Color.Gray)
                    }
                )
            } else {
                // not editable password display
                Box(
                    modifier = Modifier
                        .width(225.dp)
                        .height(50.dp)
                        .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "********",
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Password",
                            tint = Color.Gray,
                            modifier = Modifier
                                .clickable { onEditChange(true) }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        if (isEditing) {
            Spacer(modifier = Modifier.height(8.dp))

            // New Password Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "New Password", fontSize = 16.sp, modifier = Modifier.weight(1f))

                TextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .width(225.dp)
                        .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp)),
                    placeholder = { Text(text = "Enter new password", color = Color.Gray) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Re-Type Password Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Re-Type Password", fontSize = 16.sp, modifier = Modifier.weight(1f))

                TextField(
                    value = reTypePassword,
                    onValueChange = onReTypePasswordChange,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .height(50.dp)
                        .width(225.dp)
                        .background(Color(0xFFECECEC), shape = RoundedCornerShape(10.dp)),
                    placeholder = { Text(text = "Re-type new password", color = Color.Gray) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Save",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .background(DarkGreen, shape = RoundedCornerShape(8.dp))
                        .clickable { onSave() }
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Cancel",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .clickable { onCancel() }
                        .padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
