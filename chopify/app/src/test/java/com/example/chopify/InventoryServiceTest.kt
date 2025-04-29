package com.example.chopify

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.example.chopify.models.InventoryItem
import com.example.chopify.models.UserPreferences
import com.example.chopify.services.FirebaseService
import com.example.chopify.services.InventoryService
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InventoryServiceTest {
    private lateinit var inventoryService: InventoryService
    private lateinit var mockFirebaseService: FirebaseService
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockDocumentReference: DocumentReference
    private lateinit var mockCollectionReference: CollectionReference
    private lateinit var mockUserPreferences: UserPreferences
    private lateinit var mockContext: Context

    @BeforeEach
    fun setUp() {
        mockFirestore = mockk()
        mockFirebaseService = mockk()
        mockDocumentReference = mockk()
        mockCollectionReference = mockk()
        mockUserPreferences = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        inventoryService = InventoryService(mockFirebaseService)
        every { mockFirebaseService.db } returns mockFirestore

        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } returns false
    }

    @Test
    fun testCreateItemSuccess() {
        val testUserId = "testuser1"
        val testInventoryId = "testinventory1"
        val testItem = InventoryItem(name = "Test Item", quantity = 5)

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
        } returns mockCollectionReference

        val mockAddTask = mockk<Task<DocumentReference>>()
        every { mockCollectionReference.add(testItem) } returns mockAddTask
        every { mockAddTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentReference>>().onSuccess(mockDocumentReference)
            mockAddTask
        }
        every { mockAddTask.addOnFailureListener(any()) } returns mockAddTask
        every { mockDocumentReference.id } returns testInventoryId

        val mockSetTask = mockk<Task<Void>>()
        every { mockCollectionReference.document(testInventoryId) } returns mockDocumentReference
        every { mockDocumentReference.set(any<InventoryItem>()) } returns mockSetTask
        every { mockSetTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockSetTask
        }
        every { mockSetTask.addOnFailureListener(any()) } returns mockSetTask

        var onSuccessCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        inventoryService.createItem(
            testUserId, testItem, onSuccess, onFailure,
            userPreferences = mockUserPreferences,
            context = mockContext
        )

        assertTrue(onSuccessCalled)
        verify { mockCollectionReference.add(testItem) }
        verify { mockDocumentReference.set(any<InventoryItem>()) }
        assertEquals(testInventoryId, testItem.inventoryID)
    }

    @Test
    fun testCreateItemFailure() {
        val testUserId = "testuser1"
        val testItem = InventoryItem(name = "Test Item", quantity = 5)

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
        } returns mockCollectionReference

        val mockAddTask = mockk<Task<DocumentReference>>()
        every { mockCollectionReference.add(testItem) } returns mockAddTask

        val exception = Exception("Failed to create item")
        every { mockAddTask.addOnSuccessListener(any()) } returns mockAddTask
        every { mockAddTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockAddTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Failed to create item", e.message)
        }

        inventoryService.createItem(
            testUserId, testItem, onSuccess, onFailure,
            userPreferences = mockUserPreferences,
            context = mockContext
        )

        assertTrue(onFailureCalled)
        verify { mockCollectionReference.add(testItem) }
    }

    @Test
    fun testGetAllItemsSuccess() {
        val testUserId = "testuser1"
        val testItems = listOf(
            InventoryItem(inventoryID = "item1", name = "Item 1", quantity = 2),
            InventoryItem(inventoryID = "item2", name = "Item 2", quantity = 5)
        )

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
        } returns mockCollectionReference

        val mockGetTask = mockk<Task<QuerySnapshot>>()
        every { mockCollectionReference.get() } returns mockGetTask

        val mockQuerySnapshot = mockk<QuerySnapshot>()
        val mockDocumentSnapshots = testItems.map { item ->
            mockk<DocumentSnapshot>().apply {
                every { toObject(InventoryItem::class.java) } returns item
            }
        }
        every { mockQuerySnapshot.documents } returns mockDocumentSnapshots

        every { mockGetTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<QuerySnapshot>>().onSuccess(mockQuerySnapshot)
            mockGetTask
        }
        every { mockGetTask.addOnFailureListener(any()) } returns mockGetTask

        var onSuccessCalled = false
        val onSuccess: (List<InventoryItem>) -> Unit = { items ->
            onSuccessCalled = true
            assertEquals(testItems.size, items.size)
            assertEquals(testItems[0].inventoryID, items[0].inventoryID)
            assertEquals(testItems[1].inventoryID, items[1].inventoryID)
        }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        inventoryService.getAllItems(testUserId, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockCollectionReference.get() }
    }

    @Test
    fun testGetAllItemsFailure() {
        val testUserId = "testuser1"

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
        } returns mockCollectionReference

        val mockGetTask = mockk<Task<QuerySnapshot>>()
        every { mockCollectionReference.get() } returns mockGetTask

        val exception = Exception("Failed to get items")
        every { mockGetTask.addOnSuccessListener(any()) } returns mockGetTask
        every { mockGetTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockGetTask
        }

        var onFailureCalled = false
        val onSuccess: (List<InventoryItem>) -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Failed to get items", e.message)
        }

        inventoryService.getAllItems(testUserId, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockCollectionReference.get() }
    }

    @Test
    fun testGetItemSuccess() {
        val testUserId = "testuser1"
        val testInventoryId = "testinventory1"
        val testItem =
            InventoryItem(inventoryID = testInventoryId, name = "Test Item", quantity = 5)

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
                .document(testInventoryId)
        } returns mockDocumentReference

        val mockGetTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocumentReference.get() } returns mockGetTask

        val mockDocumentSnapshot = mockk<DocumentSnapshot>()
        every { mockDocumentSnapshot.toObject(InventoryItem::class.java) } returns testItem

        every { mockGetTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<DocumentSnapshot>>().onSuccess(mockDocumentSnapshot)
            mockGetTask
        }
        every { mockGetTask.addOnFailureListener(any()) } returns mockGetTask

        var onSuccessCalled = false
        val onSuccess: (InventoryItem?) -> Unit = { item ->
            onSuccessCalled = true
            assertNotNull(item)
            assertEquals(testItem.inventoryID, item?.inventoryID)
            assertEquals(testItem.name, item?.name)
            assertEquals(testItem.quantity, item?.quantity)
        }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        inventoryService.getItem(testUserId, testInventoryId, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockDocumentReference.get() }
    }

    @Test
    fun testGetItemFailure() {
        val testUserId = "testuser1"
        val testInventoryId = "testinventory1"

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
                .document(testInventoryId)
        } returns mockDocumentReference

        val mockGetTask = mockk<Task<DocumentSnapshot>>()
        every { mockDocumentReference.get() } returns mockGetTask

        val exception = Exception("Failed to get item")
        every { mockGetTask.addOnSuccessListener(any()) } returns mockGetTask
        every { mockGetTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockGetTask
        }

        var onFailureCalled = false
        val onSuccess: (InventoryItem?) -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Failed to get item", e.message)
        }

        inventoryService.getItem(testUserId, testInventoryId, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockDocumentReference.get() }
    }

    @Test
    fun testUpdateItemSuccess() {
        val testUserId = "testuser1"
        val testInventoryId = "testinventory1"
        val updatedItem =
            InventoryItem(inventoryID = testInventoryId, name = "Updated Item", quantity = 10)

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
                .document(testInventoryId)
        } returns mockDocumentReference

        val mockSetTask = mockk<Task<Void>>()
        every { mockDocumentReference.set(updatedItem, SetOptions.merge()) } returns mockSetTask
        every { mockSetTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockSetTask
        }
        every { mockSetTask.addOnFailureListener(any()) } returns mockSetTask

        var onSuccessCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        inventoryService.updateItem(testUserId, testInventoryId, updatedItem, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockDocumentReference.set(updatedItem, SetOptions.merge()) }
    }

    @Test
    fun testUpdateItemFailure() {
        val testUserId = "testuser1"
        val testInventoryId = "testinventory1"
        val updatedItem =
            InventoryItem(inventoryID = testInventoryId, name = "Updated Item", quantity = 10)

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
                .document(testInventoryId)
        } returns mockDocumentReference

        val mockSetTask = mockk<Task<Void>>()
        every { mockDocumentReference.set(updatedItem, SetOptions.merge()) } returns mockSetTask

        val exception = Exception("Failed to update item")
        every { mockSetTask.addOnSuccessListener(any()) } returns mockSetTask
        every { mockSetTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockSetTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Failed to update item", e.message)
        }

        inventoryService.updateItem(testUserId, testInventoryId, updatedItem, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockDocumentReference.set(updatedItem, SetOptions.merge()) }
    }

    @Test
    fun testDeleteItemSuccess() {
        val testUserId = "testuser1"
        val testInventoryId = "testinventory1"

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
                .document(testInventoryId)
        } returns mockDocumentReference

        val mockDeleteTask = mockk<Task<Void>>()
        every { mockDocumentReference.delete() } returns mockDeleteTask
        every { mockDeleteTask.addOnSuccessListener(any()) } answers {
            firstArg<OnSuccessListener<Void>>().onSuccess(null)
            mockDeleteTask
        }
        every { mockDeleteTask.addOnFailureListener(any()) } returns mockDeleteTask

        var onSuccessCalled = false
        val onSuccess: () -> Unit = { onSuccessCalled = true }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        inventoryService.deleteItem(testUserId, testInventoryId, onSuccess, onFailure)

        assertTrue(onSuccessCalled)
        verify { mockDocumentReference.delete() }
    }

    @Test
    fun testDeleteItemFailure() {
        val testUserId = "testuser1"
        val testInventoryId = "testinventory1"

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
                .document(testInventoryId)
        } returns mockDocumentReference

        val mockDeleteTask = mockk<Task<Void>>()
        every { mockDocumentReference.delete() } returns mockDeleteTask

        val exception = Exception("Failed to delete item")
        every { mockDeleteTask.addOnSuccessListener(any()) } returns mockDeleteTask
        every { mockDeleteTask.addOnFailureListener(any()) } answers {
            firstArg<OnFailureListener>().onFailure(exception)
            mockDeleteTask
        }

        var onFailureCalled = false
        val onSuccess: () -> Unit = { fail("Expected failure but got success") }
        val onFailure: (Exception) -> Unit = { e ->
            onFailureCalled = true
            assertEquals("Failed to delete item", e.message)
        }

        inventoryService.deleteItem(testUserId, testInventoryId, onSuccess, onFailure)

        assertTrue(onFailureCalled)
        verify { mockDocumentReference.delete() }
    }

    @Test
    fun testGetInventoryUpdatesSuccess() {
        val testUserId = "testuser1"
        val testItems = listOf(
            InventoryItem(inventoryID = "item1", name = "Item 1", quantity = 2),
            InventoryItem(inventoryID = "item2", name = "Item 2", quantity = 5)
        )

        every {
            mockFirestore.collection("users").document(testUserId).collection("inventory")
        } returns mockCollectionReference

        val mockRegistration = mockk<ListenerRegistration>()
        val mockDocumentSnapshots = testItems.map { item ->
            mockk<DocumentSnapshot>().apply {
                every { id } returns item.inventoryID!!
                every { toObject(InventoryItem::class.java) } returns item
            }
        }
        val mockQuerySnapshot = mockk<QuerySnapshot>()
        every { mockQuerySnapshot.documents } returns mockDocumentSnapshots

        every {
            mockCollectionReference.addSnapshotListener(any())
        } answers {
            val listener = firstArg<EventListener<QuerySnapshot>>()
            listener.onEvent(mockQuerySnapshot, null)
            mockRegistration
        }

        var onUpdateCalled = false
        val onUpdate: (List<InventoryItem>) -> Unit = { items ->
            onUpdateCalled = true
            assertEquals(testItems.size, items.size)
        }
        val onFailure: (Exception) -> Unit =
            { exception -> fail("Exception: ${exception.message}") }

        inventoryService.getInventoryUpdates(testUserId, onUpdate, onFailure)

        assertTrue(onUpdateCalled)
        verify { mockCollectionReference.addSnapshotListener(any()) }
    }
}
