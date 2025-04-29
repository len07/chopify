package com.example.chopify.services

import android.util.Log
import com.example.chopify.models.GroceryItem
import com.google.firebase.firestore.SetOptions

// CRUD for "GroceryItem" in Firestore under users/{userId}/grocery_list
class GroceryService(private val fb: FirebaseService) {

    fun getGroceryUpdates(
        userId: String,
        onUpdate: (List<GroceryItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("grocery_list")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                val groceryList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroceryItem::class.java)?.copy(groceryID = doc.id)
                }

                if (groceryList != null) {
                    onUpdate(groceryList)
                } else {
                    Log.d("GroceryService", "Empty grocery list")
                }
            }
    }

    fun createItem(
        userId: String,
        item: GroceryItem,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userGroceryRef = fb.db.collection("users").document(userId).collection("grocery_list")

        userGroceryRef.add(item)
            .addOnSuccessListener { documentReference ->
                val genID = documentReference.id
                item.groceryID = genID

                userGroceryRef.document(genID).set(item)
                    .addOnSuccessListener {
                        Log.d(
                            "GroceryService",
                            "GroceryItem created successfully: groceryID=$genID"
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "GroceryService",
                            "Failed to create GroceryItem: ${exception.message}"
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
        onSuccess: (List<GroceryItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("grocery_list")
            .get()
            .addOnSuccessListener { result ->
                val items = result.documents.mapNotNull { it.toObject(GroceryItem::class.java) }
                onSuccess(items)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun getItem(
        userId: String,
        groceryId: String,
        onSuccess: (GroceryItem?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("grocery_list").document(groceryId)
            .get()
            .addOnSuccessListener { document ->
                val item = document.toObject(GroceryItem::class.java)
                if (item != null) {
                    Log.d("GroceryService", "GroceryItem found for groceryID=$groceryId")
                } else {
                    Log.e("GroceryService", "GroceryItem not found")
                }
                onSuccess(item)
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "GroceryService",
                    "Failed to get item groceryID=${groceryId}: ${exception.message}"
                )
                onFailure(exception)
            }
    }

    fun updateItem(
        userId: String,
        groceryId: String,
        updatedGroceryItem: GroceryItem,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("grocery_list").document(groceryId)
            .set(updatedGroceryItem, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("GroceryService", "GroceryItem updated successfully: groceryID=$groceryId")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "GroceryService",
                    "Failed update for groceryID=$groceryId: ${exception.message}"
                )
                onFailure(exception)
            }
    }

    fun deleteItem(
        userId: String,
        groceryId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        fb.db.collection("users").document(userId).collection("grocery_list").document(groceryId)
            .delete()
            .addOnSuccessListener {
                Log.d("GroceryService", "GroceryItem deleted successfully: groceryID=$groceryId")
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "GroceryService",
                    "Failed delete for groceryID=$groceryId: ${exception.message}"
                )
                onFailure(exception)
            }
    }
}
