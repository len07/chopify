package com.example.chopify.services

import android.util.Log
import com.example.chopify.models.User
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

// Firebase Auth services
class LoginService(private val fb: FirebaseService) {

    // Firebase Auth Login
    fun loginUser(
        email: String,
        password: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = User(userID = firebaseUser.uid, email = firebaseUser.email ?: "")
                    Log.d("LoginService", "Login successful! userId=${user.userID}")
                    onSuccess(user)
                } else {
                    Log.e("LoginService", "Login successful, but user is null")
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoginService", "Login failed: ${exception.message}")
                when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        onFailure(Exception("Incorrect password. Please try again."))
                    }

                    is FirebaseAuthInvalidUserException -> {
                        onFailure(Exception("No account found with this email address."))
                    }

                    else -> {
                        onFailure(Exception("Login failed. Please check your credentials and try again."))
                    }
                }
            }
    }

    // Firebase Auth Logout
    fun logoutUser(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        try {
            fb.auth.signOut()
            Log.d("LoginService", "Logout successful!")
            onSuccess()
        } catch (exception: Exception) {
            Log.e("LoginService", "Logout failed: ${exception.message}")
            onFailure(exception)
        }
    }
}