package com.example.chopify

import android.text.TextUtils
import android.util.Log
import com.example.chopify.models.User
import com.example.chopify.services.FirebaseService
import com.example.chopify.services.UserService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserServiceTest {
    private lateinit var userService: UserService
    private lateinit var mockFirebaseService: FirebaseService
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockFirebaseUser: FirebaseUser

    @BeforeEach
    fun setUp() {
        mockAuth = mockk()
        mockFirestore = mockk()
        mockFirebaseService = mockk()
        mockFirebaseUser = mockk()
        userService = UserService(mockFirebaseService)
        every { mockFirebaseService.auth } returns mockAuth
        every { mockFirebaseService.db } returns mockFirestore
        every { mockAuth.currentUser } returns mockFirebaseUser

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns false
    }

    @Test
    fun testCreateUserSuccess() {
        val testUser = User("", "testuser1@test.com", "testuser1", "Test User 1")
        val testUid = "testuser1"
        every { mockFirebaseUser.uid } returns testUid

        val mockAuthResultTask = mockk<Task<AuthResult>>()
        every {
            mockAuth.createUserWithEmailAndPassword(
                testUser.email, testUser.password
            )
        } returns mockAuthResultTask
        every { mockAuthResultTask.isSuccessful } returns true
        every { mockAuthResultTask.exception } returns null

        val mockAuthResult: AuthResult = mockk()
        every { mockAuthResultTask.result } returns mockAuthResult
        every { mockAuthResult.user } returns mockFirebaseUser
        every { mockAuthResultTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(mockAuthResultTask)
            mockAuthResultTask
        }

        val mockSetTask = mockk<Task<Void>>()
        every { mockFirestore.collection("users").document(testUid).set(any()) } returns mockSetTask
        every { mockSetTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockSetTask
        }
        every { mockSetTask.addOnFailureListener(any()) } returns mockSetTask

        var onSuccessCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }
        userService.createUser(testUser, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockAuth.createUserWithEmailAndPassword(testUser.email, testUser.password) }
        verify { mockFirestore.collection("users").document(testUid).set(any()) }
    }

    @Test
    fun testCreateUserAlreadyRegistered() {
        val testUser = User("", "testuser1@test.com", "testuser1", "Test User 1")
        val testUid = "testuser1"
        every { mockFirebaseUser.uid } returns testUid

        val mockAuthResultTask = mockk<Task<AuthResult>>()
        every {
            mockAuth.createUserWithEmailAndPassword(
                testUser.email, testUser.password
            )
        } returns mockAuthResultTask
        every { mockAuthResultTask.isSuccessful } returns false
        val exception = FirebaseAuthUserCollisionException("test", "Email is already registered")
        every { mockAuthResultTask.exception } returns exception

        val mockAuthResult1: AuthResult = mockk()
        every { mockAuthResultTask.result } returns mockAuthResult1
        every { mockAuthResult1.user } returns mockFirebaseUser
        every { mockAuthResultTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<AuthResult>>().onComplete(mockAuthResultTask)
            mockAuthResultTask
        }

        val mockSetTask = mockk<Task<Void>>()
        every {
            mockFirestore.collection("users").document(testUid).set(any())
        } returns mockSetTask
        every { mockSetTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockSetTask
        }
        every { mockSetTask.addOnFailureListener(any()) } returns mockSetTask

        var onSuccessCalled = false
        var onFailureCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit = { exception ->
            onFailureCalled = true
            assertTrue(exception.message?.contains("Email is already registered") == true)
        }
        userService.createUser(testUser, onSuccess, onFailure)

        assertFalse(onSuccessCalled)
        assertTrue(onFailureCalled)
    }

    @Test
    fun testGetUserSuccess() {
        val testUser = User("testUser1", "testuser1@test.com", "testuser1", "Test User 1")
        val mockDocumentSnapshot = mockk<DocumentSnapshot>()
        every { mockDocumentSnapshot.toObject(User::class.java) } returns testUser

        val mockGetTask = mockk<Task<DocumentSnapshot>>()
        every {
            mockFirestore.collection("users").document(testUser.userID).get()
        } returns mockGetTask
        every { mockGetTask.isSuccessful } returns true
        every { mockGetTask.result } returns mockDocumentSnapshot
        every { mockGetTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocumentSnapshot)
            mockGetTask
        }
        every { mockGetTask.addOnFailureListener(any()) } returns mockGetTask

        var onSuccessCalled = false
        val onSuccess: (User?) -> Unit = { user ->
            assertNotNull(user)
            assertEquals(testUser, user)
            onSuccessCalled = true
        }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }
        userService.getUser(testUser.userID, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockFirestore.collection("users").document(testUser.userID).get() }
    }

    @Test
    fun testGetUserFailure() {
        val testUserId = "notUserId"
        val mockGetTask = mockk<Task<DocumentSnapshot>>()

        every {
            mockFirestore.collection("users").document(testUserId).get()
        } returns mockGetTask
        every { mockGetTask.addOnSuccessListener(any()) } returns mockGetTask
        val exception = Exception("User not found")
        every { mockGetTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockGetTask
        }
        var onSuccessCalled = false
        val onSuccess: (User?) -> Unit = { user ->
            onSuccessCalled = true
            fail("Expected failure but got success with user: $user")
        }
        var onFailureCalled = false
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("User not found", e.message)
        }
        userService.getUser(testUserId, onSuccess, onFailure)

        assertFalse(onSuccessCalled)
        assertTrue(onFailureCalled)
        verify { mockFirestore.collection("users").document(testUserId).get() }
    }

    @Test
    fun testUpdateNameSuccess() {
        val testUserId = "testuser1"
        val newName = "Updated User Name"

        val mockUpdateTask = mockk<Task<Void>>()
        every {
            mockFirestore.collection("users").document(testUserId)
                .update(any<Map<String, Any>>())
        } returns mockUpdateTask

        every { mockUpdateTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockUpdateTask
        }
        every { mockUpdateTask.addOnFailureListener(any()) } returns mockUpdateTask

        var onSuccessCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        userService.updateName(testUserId, newName, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify {
            mockFirestore.collection("users").document(testUserId)
                .update(match { updates ->
                    updates["name"] == newName
                })
        }
    }

    @Test
    fun testUpdateNameFailure() {
        val testUserId = "testuser1"
        val newName = "Updated User Name"

        val mockUpdateTask = mockk<Task<Void>>()
        every {
            mockFirestore.collection("users").document(testUserId)
                .update(any<Map<String, Any>>())
        } returns mockUpdateTask

        val exception = Exception("Update name failed")
        every { mockUpdateTask.addOnSuccessListener(any()) } returns mockUpdateTask
        every { mockUpdateTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockUpdateTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Update name failed", e.message)
        }

        userService.updateName(testUserId, newName, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify {
            mockFirestore.collection("users").document(testUserId)
                .update(any<Map<String, Any>>())
        }
    }

    @Test
    fun testUpdatePasswordSuccess() {
        val testUserId = "testuser1"
        val currentPassword = "currentpassword"
        val newPassword = "newpassword"

        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns "user@example.com"

        val mockAuthCredential = mockk<com.google.firebase.auth.AuthCredential>()
        mockkStatic(com.google.firebase.auth.EmailAuthProvider::class)
        every {
            com.google.firebase.auth.EmailAuthProvider.getCredential(any(), currentPassword)
        } returns mockAuthCredential

        val mockReauthTask = mockk<Task<Void>>()
        every { mockFirebaseUser.reauthenticate(mockAuthCredential) } returns mockReauthTask
        every { mockReauthTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockReauthTask
        }
        every { mockReauthTask.addOnFailureListener(any()) } returns mockReauthTask

        val mockUpdatePwdTask = mockk<Task<Void>>()
        every { mockFirebaseUser.updatePassword(newPassword) } returns mockUpdatePwdTask
        every { mockUpdatePwdTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockUpdatePwdTask
        }
        every { mockUpdatePwdTask.addOnFailureListener(any()) } returns mockUpdatePwdTask

        var onSuccessCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        userService.updatePassword(testUserId, currentPassword, newPassword, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockFirebaseUser.reauthenticate(mockAuthCredential) }
        verify { mockFirebaseUser.updatePassword(newPassword) }
    }

    @Test
    fun testUpdatePasswordNoUser() {
        val testUserId = "testuser1"
        val currentPassword = "currentpassword"
        val newPassword = "newpassword"

        every { mockAuth.currentUser } returns null

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("No authenticated user found", e.message)
        }

        userService.updatePassword(testUserId, currentPassword, newPassword, onSuccess, onFailure)

        assertTrue(onFailureCalled)
    }

    @Test
    fun testUpdatePasswordUserIdMismatch() {
        val testUserId = "testuser1"
        val currentPassword = "currentpassword"
        val newPassword = "newpassword"

        every { mockFirebaseUser.uid } returns "differentUserId"

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("User ID doesn't match.", e.message)
        }

        userService.updatePassword(testUserId, currentPassword, newPassword, onSuccess, onFailure)

        assertTrue(onFailureCalled)
    }

    @Test
    fun testUpdatePasswordReauthFailure() {
        val testUserId = "testuser1"
        val currentPassword = "currentpassword"
        val newPassword = "newpassword"

        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns "user@example.com"

        val mockAuthCredential = mockk<com.google.firebase.auth.AuthCredential>()
        mockkStatic(com.google.firebase.auth.EmailAuthProvider::class)
        every {
            com.google.firebase.auth.EmailAuthProvider.getCredential(any(), currentPassword)
        } returns mockAuthCredential

        val mockReauthTask = mockk<Task<Void>>()
        every { mockFirebaseUser.reauthenticate(mockAuthCredential) } returns mockReauthTask

        val exception = com.google.firebase.auth.FirebaseAuthInvalidCredentialsException(
            "incorrect-password",
            "Password is invalid"
        )
        every { mockReauthTask.addOnSuccessListener(any()) } returns mockReauthTask
        every { mockReauthTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockReauthTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Current password is incorrect", e.message)
        }

        userService.updatePassword(testUserId, currentPassword, newPassword, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockFirebaseUser.reauthenticate(mockAuthCredential) }
    }

    @Test
    fun testUpdatePasswordUpdateFailure() {
        val testUserId = "testuser1"
        val currentPassword = "currentpassword"
        val newPassword = "newpassword"

        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns "user@example.com"

        val mockAuthCredential = mockk<com.google.firebase.auth.AuthCredential>()
        mockkStatic(com.google.firebase.auth.EmailAuthProvider::class)
        every {
            com.google.firebase.auth.EmailAuthProvider.getCredential(any(), currentPassword)
        } returns mockAuthCredential

        val mockReauthTask = mockk<Task<Void>>()
        every { mockFirebaseUser.reauthenticate(mockAuthCredential) } returns mockReauthTask
        every { mockReauthTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockReauthTask
        }
        every { mockReauthTask.addOnFailureListener(any()) } returns mockReauthTask

        val mockUpdatePwdTask = mockk<Task<Void>>()
        every { mockFirebaseUser.updatePassword(newPassword) } returns mockUpdatePwdTask

        val exception = Exception("Password update failed")
        every { mockUpdatePwdTask.addOnSuccessListener(any()) } returns mockUpdatePwdTask
        every { mockUpdatePwdTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockUpdatePwdTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Password update failed", e.message)
        }

        userService.updatePassword(testUserId, currentPassword, newPassword, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockFirebaseUser.reauthenticate(mockAuthCredential) }
        verify { mockFirebaseUser.updatePassword(newPassword) }
    }

    @Test
    fun testUpdatePasswordGeneralReauthFailure() {
        val testUserId = "testuser1"
        val currentPassword = "currentpassword"
        val newPassword = "newpassword"

        every { mockFirebaseUser.uid } returns testUserId
        every { mockFirebaseUser.email } returns "user@example.com"

        val mockAuthCredential = mockk<com.google.firebase.auth.AuthCredential>()
        mockkStatic(com.google.firebase.auth.EmailAuthProvider::class)
        every {
            com.google.firebase.auth.EmailAuthProvider.getCredential(any(), currentPassword)
        } returns mockAuthCredential

        val mockReauthTask = mockk<Task<Void>>()
        every { mockFirebaseUser.reauthenticate(mockAuthCredential) } returns mockReauthTask

        val exception = Exception("General reauthentication failure")
        every { mockReauthTask.addOnSuccessListener(any()) } returns mockReauthTask
        every { mockReauthTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockReauthTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals(exception, e)
        }

        userService.updatePassword(testUserId, currentPassword, newPassword, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockFirebaseUser.reauthenticate(mockAuthCredential) }
    }

    @Test
    fun testDeleteUserSuccess() {
        val testUserId = "testuser1"
        every { mockFirebaseUser.uid } returns testUserId

        val mockDeleteTask = mockk<Task<Void>>()
        every {
            mockFirestore.collection("users").document(testUserId).delete()
        } returns mockDeleteTask
        every { mockDeleteTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockDeleteTask
        }
        every { mockDeleteTask.addOnFailureListener(any()) } returns mockDeleteTask

        val mockAuthDeleteTask = mockk<Task<Void>>()
        every { mockFirebaseUser.delete() } returns mockAuthDeleteTask
        every { mockAuthDeleteTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockAuthDeleteTask
        }
        every { mockAuthDeleteTask.addOnFailureListener(any()) } returns mockAuthDeleteTask

        var onSuccessCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        userService.deleteUser(testUserId, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockFirestore.collection("users").document(testUserId).delete() }
        verify { mockFirebaseUser.delete() }
    }

    @Test
    fun testDeleteUserFirestoreFailure() {
        val testUserId = "testuser1"

        val mockDeleteTask = mockk<Task<Void>>()
        every {
            mockFirestore.collection("users").document(testUserId).delete()
        } returns mockDeleteTask

        val exception = Exception("Delete failed")
        every { mockDeleteTask.addOnSuccessListener(any()) } returns mockDeleteTask
        every { mockDeleteTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockDeleteTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Delete failed", e.message)
        }

        userService.deleteUser(testUserId, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockFirestore.collection("users").document(testUserId).delete() }
    }

    @Test
    fun testDeleteUserAuthFailure() {
        val testUserId = "testuser1"
        every { mockFirebaseUser.uid } returns testUserId

        val mockDeleteTask = mockk<Task<Void>>()
        every {
            mockFirestore.collection("users").document(testUserId).delete()
        } returns mockDeleteTask
        every { mockDeleteTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockDeleteTask
        }
        every { mockDeleteTask.addOnFailureListener(any()) } returns mockDeleteTask

        val mockAuthDeleteTask = mockk<Task<Void>>()
        every { mockFirebaseUser.delete() } returns mockAuthDeleteTask
        every { mockAuthDeleteTask.addOnSuccessListener(any()) } returns mockAuthDeleteTask

        val exception = Exception("Auth delete failed")
        every { mockAuthDeleteTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockAuthDeleteTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Auth delete failed", e.message)
        }

        userService.deleteUser(testUserId, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockFirestore.collection("users").document(testUserId).delete() }
        verify { mockFirebaseUser.delete() }
    }
}
