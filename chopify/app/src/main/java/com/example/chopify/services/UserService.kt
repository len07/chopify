package com.example.chopify.services

import android.util.Log
import com.example.chopify.models.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser

// CRUD for "users" in Firestore and users in Firebase Auth
class UserService(private val fb: FirebaseService) {

    fun createUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        fb.auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser? = fb.auth.currentUser
                    val userId = firebaseUser?.uid ?: return@addOnCompleteListener
                    val userData = hashMapOf(
                        "userID" to userId,
                        "email" to user.email,
                        "name" to user.name
                    )

                    fb.db.collection("users").document(userId).set(userData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e ->
                            firebaseUser.delete()
                                .addOnCompleteListener { onFailure(e) }
                        }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        onFailure(Exception("Email is already registered"))
                    } else {
                        onFailure(exception ?: Exception("Signup failed"))
                    }
                }
            }
    }

    fun getUser(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        fb.db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Log.d("UserService", "User found for userID=$userId")
                } else {
                    Log.e("UserService", "User not found in Firestore")
                }
                onSuccess(user)
            }
            .addOnFailureListener { exception ->
                Log.e("UserService", "Failed to get user userID=${userId}: ${exception.message}")
                onFailure(exception)
            }
    }

    fun updateName(
        userId: String,
        newName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "name" to newName
        )

        fb.db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Log.d("UserService", "User name updated successfully: userID=$userId")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "UserService",
                    "Failed to update name for userID=$userId: ${exception.message}"
                )
                onFailure(exception)
            }
    }

    fun updatePassword(
        userId: String,
        currentPassword: String,
        newPassword: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val currentUser = fb.auth.currentUser

        if (currentUser == null) {
            onFailure(Exception("No authenticated user found"))
            return
        }

        if (currentUser.uid != userId) {
            onFailure(Exception("User ID doesn't match."))
            return
        }

        val credential = EmailAuthProvider.getCredential(currentUser.email ?: "", currentPassword)

        currentUser.reauthenticate(credential)
            .addOnSuccessListener {
                currentUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        Log.d("UserService", "Password updated successfully for userID=$userId")
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "UserService",
                            "Failed to update password for userID=$userId: ${exception.message}"
                        )
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        onFailure(Exception("Current password is incorrect"))
                    }

                    else -> {
                        Log.e(
                            "UserService",
                            "Failed to update for userID=$userId: ${exception.message}"
                        )
                        onFailure(exception)
                    }
                }
            }
    }

    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        fb.db.collection("users").document(userId).delete()
            .addOnSuccessListener {
                Log.d("UserService", "User deleted successfully: userID=$userId")
                val currentUser = fb.auth.currentUser
                if (currentUser != null && currentUser.uid == userId) {
                    currentUser.delete()
                        .addOnSuccessListener {
                            Log.d(
                                "UserService",
                                "User deleted successfully: userID=$userId"
                            )
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "UserService",
                                "Failed delete for userID=$userId: ${exception.message}"
                            )
                            onFailure(exception)
                        }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserService", "Failed delete for userID=$userId: ${exception.message}")
                onFailure(exception)
            }
    }
}