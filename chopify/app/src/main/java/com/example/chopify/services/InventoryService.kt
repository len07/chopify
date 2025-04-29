package com.example.chopify.services

import android.content.Context
import android.util.Log
import com.example.chopify.models.InventoryItem
import com.example.chopify.models.UserPreferences
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// CRUD for "InventoryItem" in Firestore under users/{userId}/inventory
class InventoryService(private val fb: FirebaseService) {

    fun getInventoryUpdates(
        userId: String,
        onUpdate: (List<InventoryItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("inventory")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val inventoryList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(InventoryItem::class.java)?.copy(inventoryID = doc.id)
                }

                if (inventoryList != null) {
                    onUpdate(inventoryList)
                }
            }
    }

    fun createItem(
        userId: String,
        item: InventoryItem,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit,
        userPreferences: UserPreferences,
        context: Context
    ) {
        val zoneId = ZoneId.systemDefault()

        // Schedule notifications
        if (!item.expiryDate.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                val selectedExpiryDays =
                    userPreferences.selectedDaysFlow.first()

                val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                val expiryDate = LocalDate.parse(item.expiryDate, formatter)

                val startDate = expiryDate.minusDays(selectedExpiryDays.toLong())

                // Schedule each notification per day
                for (i in 0..<selectedExpiryDays) {
                    val notifyDate = startDate.plusDays(i.toLong())

                    // 10:00 AM alert
                    val epochMillis = notifyDate
                        .atTime(LocalTime.of(10, 0))
                        .atZone(zoneId)
                        .toInstant()
                        .toEpochMilli()

                    val daysLeft = selectedExpiryDays - i
                    NotificationService.scheduleNotification(
                        context = context,
                        title = "Hurry! Don’t forget your food.",
                        message = "Your ${item.name} is expiring in $daysLeft day${if (daysLeft > 1) "s" else ""}",
                        time = epochMillis
                    )

                    Log.d("Notification", "Your ${item.name} is expiring in $daysLeft day${if (daysLeft > 1) "s" else ""}")
                }
            }
        }


        val userInventoryRef =
            fb.db.collection("users").document(userId).collection("inventory")

        userInventoryRef.add(item)
            .addOnSuccessListener { documentReference ->
                val genID = documentReference.id
                item.inventoryID = genID

                userInventoryRef.document(genID).set(item)
                    .addOnSuccessListener {
                        Log.d(
                            "InventoryService",
                            "InventoryItem created successfully: inventoryID=$genID"
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "InventoryService",
                            "Failed to create InventoryItem: ${exception.message}"
                        )
                        onFailure(exception)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getAllItems(
        userId: String,
        onSuccess: (List<InventoryItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("inventory")
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { it.toObject(InventoryItem::class.java) }
                onSuccess(items)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getItem(
        userId: String,
        inventoryId: String,
        onSuccess: (InventoryItem?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("inventory")
            .document(inventoryId)
            .get()
            .addOnSuccessListener { document ->
                val item = document.toObject(InventoryItem::class.java)
                if (item != null) {
                    Log.d("InventoryService", "InventoryItem found for inventoryID=$inventoryId")
                } else {
                    Log.e("InventoryService", "InventoryItem not found")
                }
                onSuccess(item)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "InventoryService",
                    "Failed to get item inventoryID=${inventoryId}: ${exception.message}"
                )
                onFailure(exception)
            }
    }

    fun updateItem(
        userId: String,
        inventoryId: String,
        updatedInventoryItem: InventoryItem,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("inventory")
            .document(inventoryId)
            .set(updatedInventoryItem, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(
                    "InventoryService",
                    "InventoryItem updated successfully: inventoryID=$inventoryId"
                )
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "InventoryService",
                    "Failed update for inventoryID=$inventoryId: ${exception.message}"
                )
                onFailure(exception)
            }
    }

    fun deleteItem(
        userId: String,
        inventoryId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("inventory")
            .document(inventoryId)
            .delete()
            .addOnSuccessListener {
                Log.d(
                    "InventoryService",
                    "InventoryItem deleted successfully: inventoryID=$inventoryId"
                )
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "InventoryService",
                    "Failed delete for inventoryID=$inventoryId: ${exception.message}"
                )
                onFailure(exception)
            }
    }
}
