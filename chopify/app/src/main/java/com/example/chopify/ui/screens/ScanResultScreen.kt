package com.example.chopify.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chopify.models.InventoryItem
import com.example.chopify.models.UserPreferences
import com.example.chopify.services.GroceryService
import com.example.chopify.services.InventoryService
import com.example.chopify.services.addToDate
import com.example.chopify.services.getCurrentDate
import com.example.chopify.ui.navigation.ReceiptScannerViewModel
import com.example.chopify.ui.navigation.Screen
import com.example.chopify.ui.reusable_components.InventoryItem
import com.example.chopify.ui.theme.DarkGreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.json.JSONObject


@Composable
fun ScanResultScreen(
    navController: NavController,
    inventoryService: InventoryService,
    groceryService: GroceryService,
    viewModel: ReceiptScannerViewModel,
    userPreferences: UserPreferences,
    context: Context
) {
    val user = Firebase.auth.currentUser
    val state = viewModel.scanResult.collectAsState()
    val initialScanResult = state.value?.let {
        if (it.has("grocery_items"))
            parseInventoryItems(it)
        else
            emptyList()
    } ?: emptyList()

    val listItems = remember { mutableStateOf(initialScanResult) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (listItems.value.isNotEmpty()) {
            TopBarButtons(
                onSave = {
                    // populate items to firebase
                    if (user != null) {
                        var totalItems = listItems.value.size
                        var completedItems = 0

                        if (totalItems == 0) {
                            navController.navigate(Screen.Inventory.route)
                            return@TopBarButtons
                        }

                        for (item in listItems.value) {
                            if (item.expiryDate.isNullOrEmpty()) {
                                item.expiryDate = ""
                            }
                            inventoryService.createItem(
                                user.uid, item,
                                onSuccess = {
                                    completedItems++
                                    if (completedItems == totalItems) {
                                        navController.navigate(Screen.Inventory.route)
                                    }
                                },
                                onFailure = {
                                    completedItems++
                                    if (completedItems == totalItems) {
                                        navController.navigate(Screen.Inventory.route)
                                    }
                                },
                                userPreferences,
                                context
                            )
                        }
                    } else {
                        navController.navigate(Screen.Inventory.route)
                    }
                },
                onCancel = {
                    navController.navigate(Screen.ReceiptScanner.route)
                }
            )
            Text(
                text = "Here's What We Scanned!",
                color = DarkGreen,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally)
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listItems.value) {
                    item ->
                    if (item.expiryDate.isNullOrEmpty()) {
                        item.expiryDate = ""
                    }
                    InventoryItem(
                        userId = "testuser1",
                        item = item,
                        inventoryService,
                        groceryService,
                        onUpdate = { updatedItem ->
                            item.name = updatedItem.name
                            item.quantity = updatedItem.quantity
                            item.measurementUnit = updatedItem.measurementUnit
                            item.expiryDate = updatedItem.expiryDate
                        },
                        onDelete = {
                            listItems.value = listItems.value.filter { it != item }
                        },
                        enableAddGroceryList = false
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No items found",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                Text(
                    text = "Life's better with food! Try another photo.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Retry
                Button(
                    onClick = { navController.navigate(Screen.ReceiptScanner.route) },
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .fillMaxWidth(0.5f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Retry",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                // Home
                Button(
                    onClick = { navController.navigate(Screen.Inventory.route) },
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Home")
                }
            }
        }
    }
}

fun parseInventoryItems(jsonObject: JSONObject): List<InventoryItem> {
    val items = jsonObject.getJSONArray("grocery_items")
    val dateOfPurchase = jsonObject.optString("date_of_purchase")
        .takeUnless { it.isBlank() || it.equals("NULL", ignoreCase = true) }
        ?: getCurrentDate()

    val itemList = mutableListOf<InventoryItem>()

    for (i in 0 until items.length()) {
        val itemObject = items.getJSONObject(i)


        val item = InventoryItem(
            name = itemObject.getString("name"),
            quantity = itemObject.optInt("quantity", 1),
            measurementUnit = itemObject.optString("unit", "PC"),
            dateAdded = dateOfPurchase,
            expiryDate = if (itemObject.getInt("days_till_expiry") < 30) {
                addToDate(dateOfPurchase, itemObject.getInt("days_till_expiry"))
            } else {
                null
            }
        )

        itemList.add(item)
    }

    return itemList
}

@Composable
fun TopBarButtons(onSave: () -> Unit, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = {
                Log.d("Click", "Cancel scan")
                onCancel()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = Color.Black
            ),
            modifier = Modifier.padding(0.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("Cancel")
        }
        Button(
            onClick = {
                Log.d("Click", "Add scan to inventory")
                onSave()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkGreen,
                contentColor = Color.White
            )
        ) {
            Text("Save")
        }
    }
}
