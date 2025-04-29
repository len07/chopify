package com.example.chopify.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.chopify.MainActivity
import com.example.chopify.R
import com.example.chopify.services.LoginService
import com.example.chopify.ui.reusable_components.LoginField
import com.example.chopify.ui.reusable_components.PasswordField
import com.example.chopify.ui.reusable_components.PrimaryButton
import com.example.chopify.ui.theme.DarkGreen

@Composable
fun LoginScreen(
    context: MainActivity,
    loginService: LoginService,
    onSignUp: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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
            value = email,
            onChange = { newText ->
                email = newText.trim { it == ' ' } })

        Spacer(modifier = Modifier.height(10.dp))

        PasswordField(password = password, onPasswordChange = { newText ->
            password = newText.trim { it == ' ' }}, label = "Password")

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = if (isLoading) "Logging In..." else "Log In",
            icon = painterResource(R.drawable.login_24dp),
            onClick = {
                if (email == "" || password == "") {
                    errorMessage = "Please fill out all fields"
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
                    errorMessage = "Invalid email format"
                } else {
                    errorMessage = null
                    isLoading = true
                    loginService.loginUser(email, password,
                        onSuccess = {
                            isLoading = false
                            onLoginSuccess()
                        },
                        onFailure = { exception ->
                            isLoading = false
                            errorMessage = "${exception.message}"
                        })
                }
            })

        // Continue with Google disabled
//        SecondaryButton(text = "Continue with Google", icon = painterResource(R.drawable.google),
//            onClick = {
//                context.startActivity(Intent(context, GoogleSignInActivity::class.java))
//                isLoading = false
//                onLoginSuccess()
//            }
//        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Don't have an account? Sign up here.",
            modifier = Modifier.clickable { onSignUp() },
            color = DarkGreen,
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(10.dp))
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}