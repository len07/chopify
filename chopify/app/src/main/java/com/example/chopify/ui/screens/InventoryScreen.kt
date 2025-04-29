package com.example.chopify.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.chopify.R
import com.example.chopify.models.InventoryItem
import com.example.chopify.models.UserPreferences
import com.example.chopify.services.GroceryService
import com.example.chopify.services.InventoryService
import com.example.chopify.ui.navigation.Screen
import com.example.chopify.ui.reusable_components.DatePickerDocked
import com.example.chopify.ui.reusable_components.InputField
import com.example.chopify.ui.reusable_components.InventoryItem
import com.example.chopify.ui.reusable_components.UnitToggle
import com.example.chopify.ui.reusable_components.quantityWidget
import com.example.chopify.ui.theme.DarkGreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun InventoryScreen(
    navController: NavController,
    inventoryService: InventoryService,
    groceryService: GroceryService,
    userPreferences: UserPreferences,
    context: Context
) {
    val user = Firebase.auth.currentUser
    var listInventory by remember { mutableStateOf<List<InventoryItem>>(emptyList()) }

    // state to hold the search query
    var searchQuery by remember { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var sortType by remember { mutableStateOf("Date Added") } // Default sorting type

    // fetch inventory items from Firestore
    LaunchedEffect(Unit) {
        if (user != null) {
            inventoryService.getAllItems(
                userId = user.uid,
                onSuccess = { items -> listInventory = items },
                onFailure = { exception ->
                    Log.e(
                        "InventoryScreen",
                        "Error fetching inventory: ${exception.message}"
                    )
                }
            )
        }
    }

    fun convertDate(date: String?): Int {
        if (!date.isNullOrEmpty()) {
            val parts = date.split("/")
            if (parts.size == 3) {
                val (month, day, year) = parts
                return try {
                    (year + month.padStart(2, '0') + day.padStart(2, '0')).toInt()
                } catch (e: NumberFormatException) {
                    Int.MAX_VALUE
                }
            }
        }
        return Int.MAX_VALUE
    }

    // filtered inventory based on search query
    val filteredInventory = listInventory
        .filter { it.name.contains(regex = Regex(searchQuery, RegexOption.IGNORE_CASE)) }
        .let { list ->
            when (sortType) {
                "Date Added" -> list
                "Expiry Date" -> list.sortedBy { convertDate(it.expiryDate) }
                else -> list
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // header
        TopBar("Inventory", navController)
        Spacer(modifier = Modifier.height(16.dp))
        // sort and search
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Sort(setFilter = { showFilter = true })
            Search(searchQuery, onSearchChange = { searchQuery = it })
        }
        Spacer(modifier = Modifier.height(16.dp))
        // list of items
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredInventory) { item ->
                if (user != null) {
                    InventoryItem(user.uid, item = item, inventoryService, groceryService,
                        onUpdate = { updatedItem ->
                            inventoryService.updateItem(user.uid, item.inventoryID, updatedItem,
                                onSuccess = {
                                    inventoryService.getInventoryUpdates(userId = user.uid,
                                        onUpdate = { listInventory = it },
                                        onFailure = { exception ->
                                            Log.e(
                                                "InventoryScreen",
                                                "Error fetching inventory: ${exception.message}"
                                            )
                                        })
                                },
                                onFailure = { exception ->
                                    Log.e(
                                        "InventoryScreen",
                                        "Error updating inventory item: ${exception.message}"
                                    )
                                })
                        },
                        onDelete = {
                            inventoryService.deleteItem(user.uid, item.inventoryID,
                                onSuccess = {
                                    inventoryService.getInventoryUpdates(userId = user.uid,
                                        onUpdate = { listInventory = it },
                                        onFailure = {})
                                },
                                onFailure = { exception ->
                                    Log.e(
                                        "InventoryScreen",
                                        "Error deleting inventory item: ${exception.message}"
                                    )
                                })
                        }
                    )
                }
            }
        }
    }

    // add new inventory item
    if (!showFilter) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = { showAdd = true },
                contentColor = Color.White,
                containerColor = Color.Black,
                modifier = Modifier.align(Alignment.BottomEnd),
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "add")
            }
        }
    }

    // add item
    if (showAdd) {
        // add item variables
        var itemName by remember { mutableStateOf("") }
        var expiryDate by remember { mutableStateOf("") }
        var selectedUnit by remember { mutableStateOf("PC") }
        var quantity by remember { mutableIntStateOf(1) }
        var errorMessage by remember { mutableStateOf("") }

        Dialog(
            onDismissRequest = { showAdd = false },
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFD9D9D9)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(0.52f)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                // close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            showAdd = false
                        },
                        colors = IconButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.White,
                            disabledContentColor = Color.Black
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Sharp.Close,
                            contentDescription = "close",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Add Item",
                        color = DarkGreen,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    InputField(itemName, onChange = { itemName = it })
                    Spacer(modifier = Modifier.height(12.dp))
                    UnitToggle(selectedUnit = selectedUnit) { newUnit ->
                        selectedUnit = newUnit
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    quantity = quantityWidget(quantity, onDelete = {})
                    Spacer(modifier = Modifier.height(12.dp))
                    expiryDate = DatePickerDocked()
                    Spacer(modifier = Modifier.height(12.dp))
                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            textAlign = TextAlign.Center,
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                if (itemName.isEmpty()) {
                                    errorMessage = "Item Name cannot be empty."
                                } else {
                                    errorMessage = ""

                                    val newItem = InventoryItem(
                                        name = itemName,
                                        inventoryID = "",
                                        measurementUnit = selectedUnit,
                                        expiryDate = expiryDate,
                                        quantity = quantity
                                    )
                                    if (user != null) {
                                        inventoryService.createItem(
                                            user.uid,
                                            newItem,
                                            onSuccess = {
                                                inventoryService.getInventoryUpdates(
                                                    userId = user.uid,
                                                    onUpdate = { listInventory = it },
                                                    onFailure = { exception ->
                                                        Log.e(
                                                            "InventoryScreen",
                                                            "Error fetching inventory: ${exception.message}"
                                                        )
                                                    })
                                            },
                                            onFailure = { exception ->
                                                Log.e(
                                                    "InventoryScreen",
                                                    "Error creating inventory item: ${exception.message}"
                                                )
                                            },
                                            userPreferences,
                                            context
                                        )
                                    }
                                    showAdd = false
                                }
                            },
                            colors = ButtonColors(
                                contentColor = Color.White,
                                containerColor = DarkGreen,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.Black
                            )
                        ) {
                            Text("Save", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }

    // sort filtering popup
    if (showFilter) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showFilter = false } // close when clicking outside
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFD9D9D9),
                        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                    )
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Sort by", fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(Modifier.height(12.dp))

                    Text("Date Added", color = Color.Black, modifier = Modifier.clickable {
                        sortType = "Date Added"
                        showFilter = false
                    })
                    Spacer(Modifier.height(8.dp))

                    Text("Expiry Date", color = Color.Black, modifier = Modifier.clickable {
                        sortType = "Expiry Date"
                        showFilter = false
                    })
                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Cancel",
                        color = Color.Gray,
                        modifier = Modifier.clickable { showFilter = false })
                }
            }
        }
    }
}

@Composable
fun TopBar(name: String, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, color = DarkGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        IconButton(onClick = { navController.navigate(Screen.Setting.route) }) {
            Image(
                painter = painterResource(id = R.drawable.settings),
                modifier = Modifier.size(24.dp),
                contentDescription = "settings",
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }
    }
}

@Composable
fun Sort(setFilter: (Boolean) -> Unit) {
    IconButton(onClick = { setFilter(true) }) {
        Icon(
            painter = painterResource(id = R.drawable.sort),
            contentDescription = "sort_icon",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun Search(searchQuery: String, onSearchChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    BasicTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier
            .padding(8.dp)
            .background(
                color = Color(0xFFD9D9D9).copy(alpha = 0.32f),
                shape = RoundedCornerShape(10.dp)
            ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                innerTextField()
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Sharp.Search,
                    contentDescription = "search",
                    tint = Color.Black
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        )
    )
}