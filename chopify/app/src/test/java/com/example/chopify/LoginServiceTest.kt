package com.example.chopify

import android.text.TextUtils
import android.util.Log
import com.example.chopify.models.User
import com.example.chopify.services.FirebaseService
import com.example.chopify.services.LoginService
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoginServiceTest {
    private lateinit var loginService: LoginService
    private lateinit var mockFirebaseService: FirebaseService
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser

    @BeforeEach
    fun setUp() {
        mockAuth = mockk()
        mockFirebaseService = mockk()
        mockFirebaseUser = mockk()
        loginService = LoginService(mockFirebaseService)
        every { mockFirebaseService.auth } returns mockAuth

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns false
    }

    @Test
    fun testLoginUserSuccess() {
        val email = "testuser1@test.com"
        val password = "testuser1password"
        val testUid = "testuser1"

        every { mockFirebaseUser.uid } returns testUid
        every { mockFirebaseUser.email } returns email

        val mockAuthResultTask = mockk<Task<AuthResult>>()
        val mockAuthResult: AuthResult = mockk()
        every { mockAuthResult.user } returns mockFirebaseUser

        every { mockAuth.signInWithEmailAndPassword(email, password) } returns mockAuthResultTask
        every { mockAuthResultTask.isSuccessful } returns true
        every { mockAuthResultTask.result } returns mockAuthResult
        every { mockAuthResultTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<AuthResult>>().onSuccess(mockAuthResult)
            mockAuthResultTask
        }
        every { mockAuthResultTask.addOnFailureListener(any()) } returns mockAuthResultTask

        var onSuccessCalled = false
        val onSuccess: (User?) -> Unit = { user ->
            assertNotNull(user)
            assertEquals(testUid, user?.userID)
            assertEquals(email, user?.email)
            onSuccessCalled = true
        }
        val onFailure: (Exception) -> Unit = { fail("Unexpected failure: ${it.message}") }

        loginService.loginUser(email, password, onSuccess, onFailure)
        assertTrue(onSuccessCalled)
    }

    @Test
    fun testLoginUserIncorrectPassword() {
        val email = "testuser1@test.com"
        val password = "notTestUser1Password"
        val mockAuthResultTask = mockk<Task<AuthResult>>()
        every { mockAuth.signInWithEmailAndPassword(email, password) } returns mockAuthResultTask
        every { mockAuthResultTask.isSuccessful } returns false
        val exception = FirebaseAuthInvalidCredentialsException("test", "Incorrect password")
        every { mockAuthResultTask.exception } returns exception
        every { mockAuthResultTask.addOnSuccessListener(any()) } returns mockAuthResultTask
        every { mockAuthResultTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockAuthResultTask
        }

        var onFailureCalled = false
        val onSuccess: (User?) -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Incorrect password. Please try again.", e.message)
        }

        loginService.loginUser(email, password, onSuccess, onFailure)
        assertTrue(onFailureCalled)
    }

    @Test
    fun testLoginUserNoAccountFound() {
        val email = "testuser1@test.com"
        val password = "testuser1password"
        val mockAuthResultTask = mockk<Task<AuthResult>>()
        every { mockAuth.signInWithEmailAndPassword(email, password) } returns mockAuthResultTask
        every { mockAuthResultTask.isSuccessful } returns false
        val exception = FirebaseAuthInvalidUserException("test", "User not found")
        every { mockAuthResultTask.exception } returns exception
        every { mockAuthResultTask.addOnSuccessListener(any()) } returns mockAuthResultTask
        every { mockAuthResultTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockAuthResultTask
        }

        var onFailureCalled = false
        val onSuccess: (User?) -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("No account found with this email address.", e.message)
        }
        loginService.loginUser(email, password, onSuccess, onFailure)

        assertTrue(onFailureCalled)
    }

    @Test
    fun testLogoutUserSuccess() {
        every { mockAuth.signOut() } just Runs
        var onSuccessCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit = { fail("Exception: ${it.message}") }
        loginService.logoutUser(onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockAuth.signOut() }
    }

    @Test
    fun testLogoutUserFail() {
        every { mockAuth.signOut() } throws Exception("Logout failed")
        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Logout failed", e.message)
        }
        loginService.logoutUser(onSuccess, onFailure)

        assertTrue(onFailureCalled)
    }
}
