package com.example.chopify.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.chopify.R
import com.example.chopify.models.User
import com.example.chopify.services.UserService
import com.example.chopify.ui.reusable_components.LoginField
import com.example.chopify.ui.reusable_components.PasswordField
import com.example.chopify.ui.reusable_components.PrimaryButton
import com.example.chopify.ui.theme.DarkGreen

@Composable
fun SignUpScreen(userService: UserService, onLoginSuccess: () -> Unit, onReturn: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var reTypePassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // back button
        IconButton(
            onClick = { onReturn() },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "back",
                tint = DarkGreen
            )
        }

        // page content
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painterResource(R.drawable.chopifylogo),
                contentDescription = "null",
                modifier = Modifier
                    .width(202.dp)
                    .height(126.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))
            LoginField(
                value = name,
                onChange = { name = it },
                placeholder = "Full Name"
            )
            Spacer(modifier = Modifier.height(10.dp))
            LoginField(
                value = email,
                onChange = { newText ->
                    email = newText.trim { it == ' ' }
                })
            Spacer(modifier = Modifier.height(10.dp))
            PasswordField(
                password = password,
                onPasswordChange = { newText ->
                    password = newText.trim { it == ' ' }
                },
                label = "Password"
            )
            Spacer(modifier = Modifier.height(10.dp))
            PasswordField(
                password = reTypePassword,
                onPasswordChange = { newText ->
                    reTypePassword = newText.trim { it == ' ' }
                },
                label = "Re-enter Password"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Spacer(modifier = Modifier.height(10.dp))
            PrimaryButton(
                text = if (isLoading) "Signing Up..." else "Sign Up",
                icon = painterResource(R.drawable.login_24dp),
                onClick = {
                    if (email == "" || name == "" || password == "" || reTypePassword == "") {
                        errorMessage = "Please fill out all fields"
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                        errorMessage = "Invalid email format"
                    } else if (password != reTypePassword) {
                        errorMessage = "Passwords do not match"
                    } else {
                        errorMessage = null
                        isLoading = true
                        val newUser =
                            User(userID = "", email = email, password = password, name = name)
                        userService.createUser(newUser,
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
                            },
                            onFailure = { exception ->
                                isLoading = false
                                errorMessage = "${exception.message}"
                            }
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
