package com.example.chopify.ui.screens

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.sharp.Close
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.chopify.models.GroceryItem
import com.example.chopify.services.GroceryService
import com.example.chopify.ui.reusable_components.InputField
import com.example.chopify.ui.theme.DarkGreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlin.math.roundToInt

@Composable
fun GroceryListScreen(
    navController: NavController,
    groceryService: GroceryService
) {
    val user = Firebase.auth.currentUser
    var groceryItems by remember { mutableStateOf<List<GroceryItem>>(emptyList()) }

    var showAdd by remember { mutableStateOf(false) }

    // Fetch inventory items from Firestore
    LaunchedEffect(Unit) {
        if (user != null) {
            groceryService.getAllItems(
                userId = user.uid,
                onSuccess = { items -> groceryItems = items },
                onFailure = { exception ->
                    Log.e(
                        "GroceryScreen",
                        "Error fetching grocery: ${exception.message}"
                    )
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopBar("Grocery List", navController)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(groceryItems, key = { it.name }) { item ->
                SwipeToDeleteRow(
                    item = item,
                    onDelete = {
                        if (user != null) {
                            groceryService.deleteItem(user.uid, item.groceryID,
                                onSuccess = {
                                    groceryService.getGroceryUpdates(
                                        userId = user.uid,
                                        onUpdate = { groceryItems = it },
                                        onFailure = {})
                                },
                                onFailure = {})
                        }
                    },
                    onCheckedChange = { isChecked ->
                        groceryItems = groceryItems.map {
                            if (it.groceryID == item.groceryID) it.copy(checked = isChecked) else it
                        }
                        val updatedItem = item.copy(checked = isChecked)
                        if (user != null) {
                            groceryService.updateItem(user.uid, item.groceryID, updatedItem,
                                onSuccess = {
                                    groceryService.getGroceryUpdates(
                                        userId = user.uid,
                                        onUpdate = { groceryItems = it },
                                        onFailure = { exception ->
                                            Log.e(
                                                "GroceryScreen",
                                                "Error fetching grocery: ${exception.message}"
                                            )
                                        })

                                },
                                onFailure = { exception ->
                                    Log.e(
                                        "GroceryScreen",
                                        "Error updating grocery item: ${exception.message}"
                                    )
                                })
                        }
                    }
                )
            }
        }
    }

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

    // add item
    if (showAdd) {
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
                    .fillMaxHeight(0.35f)
                    .padding(8.dp),
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
                        .padding(16.dp)
                ) {

                    var itemName by remember { mutableStateOf("") }

                    Text(
                        text = "Add Item",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkGreen
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // item name
                    InputField(itemName, onChange = { itemName = it })
                    Spacer(modifier = Modifier.height(32.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                // date added should be current date
                                val userid: String = user?.uid ?: ""
                                val newItem = GroceryItem(
                                    name = itemName,
                                    groceryID = "",
                                    measurementUnit = "",
                                    quantity = 1,
                                    checked = false
                                )
                                if (user != null) {
                                    groceryService.createItem(user.uid, newItem,
                                        onSuccess = {
                                            groceryService.getGroceryUpdates(userId = user.uid,
                                                onUpdate = { groceryItems = it },
                                                onFailure = {})
                                        },
                                        onFailure = {})
                                }
                                showAdd = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 32.dp),
                            colors = ButtonColors(
                                contentColor = Color.White,
                                containerColor = DarkGreen,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.Gray
                            )
                        ) {
                            Text(
                                "Save",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToDeleteRow(item: GroceryItem, onDelete: () -> Unit, onCheckedChange: (Boolean) -> Unit) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    offsetX = (offsetX + dragAmount).coerceIn(-300f, 0f)
                }
            }
    ) {
        // Delete button
        if (offsetX < -100f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        Color.Gray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)
                    )
                    .clickable { onDelete() }
                    .padding(16.dp)
                    .width(-(animatedOffsetX.roundToInt() / 3.2).dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        }

        // Grocery Item (moves left on swipe)
        Box(modifier = Modifier.offset { IntOffset(animatedOffsetX.roundToInt(), 0) }) {
            GroceryItem(item = item, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun GroceryItem(item: GroceryItem, onCheckedChange: (Boolean) -> Unit) {
    Log.d("groceryscreen", "$item")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(
                color = Color(0xFFD9D9D9).copy(alpha = 0.32f),
                shape = RoundedCornerShape(10.dp)
            ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //checkbox
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color = if (item.checked) DarkGreen else Color.Transparent)
                    .border(
                        2.dp,
                        if (item.checked) DarkGreen else Color.Gray,
                        CircleShape
                    ) // Border color changes based on state
                    .clickable { onCheckedChange(!item.checked) }, // Toggle checked state
                contentAlignment = Alignment.Center
            ) {
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = item.name, fontSize = 18.sp, textAlign = TextAlign.Start)
        }
    }
}
