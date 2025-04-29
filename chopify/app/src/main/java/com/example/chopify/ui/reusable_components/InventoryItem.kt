package com.example.chopify.ui.reusable_components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chopify.R
import com.example.chopify.models.GroceryItem
import com.example.chopify.models.InventoryItem
import com.example.chopify.services.GroceryService
import com.example.chopify.services.InventoryService
import com.example.chopify.ui.theme.DarkGreen
import com.example.chopify.ui.theme.Orange
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// Function to calculate the time remaining until expiry
@SuppressLint("SimpleDateFormat")
fun getTimeRemaining(expiryDate: String): Pair<String, Color> {
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val expiry = LocalDate.parse(expiryDate, formatter)
    val today = LocalDate.now()

    val daysLeft = ChronoUnit.DAYS.between(today, expiry).toInt()

    return when {
        daysLeft < 0 -> "Expired" to Color.Gray
        daysLeft == 0 -> "Expires today" to Color.Red
        daysLeft <= 3 -> "$daysLeft day${if (daysLeft > 1) "s" else ""} left" to Color.Red
        daysLeft < 7 -> "$daysLeft days left" to Orange
        daysLeft < 30 -> "${daysLeft / 7} week${if ((daysLeft / 7) > 1) "s" else ""} left" to DarkGreen
        else -> "${daysLeft / 30} month${if (daysLeft / 30 > 1) "s" else ""} left" to DarkGreen
    }
}

@Composable
fun InventoryItem(
    userId: String,
    item: InventoryItem,
    inventoryService: InventoryService,
    groceryService: GroceryService,
    onUpdate: (InventoryItem) -> Unit,
    onDelete: () -> Unit,
    enableAddGroceryList: Boolean = true
) {
    var deleteConfirm by remember { mutableStateOf(false) }
    // Store updated state locally
    var selectedUnit by remember(item.inventoryID) { mutableStateOf(item.measurementUnit) }
    var quantity by remember(item.inventoryID) { mutableIntStateOf(item.quantity) }
    var updatedExpiry by remember(item.inventoryID) { mutableStateOf(item.expiryDate) }
    var showPopup by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                showPopup = true
            }
            .background(
                color = Color(0xFFD9D9D9).copy(alpha = 0.32f),
                shape = RoundedCornerShape(10.dp)
            ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                item.expiryDate?.takeIf { it.isNotBlank() }?.let {
                    val expiry = getTimeRemaining(it)
                    Text(
                        text = expiry.first,
                        fontSize = 14.sp,
                        color = expiry.second,
                        textAlign = TextAlign.Start,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
            Text(
                text = "${item.quantity} ${item.measurementUnit}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }

    if (showPopup) {
        var updatedName by remember(item.inventoryID) { mutableStateOf(item.name) }
        Dialog(
            onDismissRequest = { showPopup = false },
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFD9D9D9)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(0.42f)
                    .padding(8.dp),
            ) {
                // delete/close
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            deleteConfirm = true
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
                            imageVector = Icons.Sharp.Delete,
                            contentDescription = "delete",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            showPopup = false
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
                        .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    EditableLabel(
                        value = updatedName,
                        onChange = { newName -> updatedName = newName }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    UnitToggle(selectedUnit = selectedUnit) { newUnit ->
                        selectedUnit = newUnit
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // quantity widget
                    val newQuantity = quantityWidget(
                        quantity = quantity,
                        onDelete = {
                            deleteConfirm = true
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // expiry date
                    if (item.expiryDate != null) {
                        updatedExpiry = DatePickerDocked(expiryDate = item.expiryDate)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(
                            onClick = {
                                if (newQuantity == 0) {
                                    deleteConfirm = true
                                } else {
                                    val updatedItem = item.copy(
                                        name = updatedName,
                                        quantity = newQuantity,
                                        measurementUnit = selectedUnit,
                                        expiryDate = updatedExpiry
                                    )
                                    onUpdate(updatedItem)
                                    showPopup = false
                                    quantity = newQuantity
                                }
                            },
                            colors = ButtonColors(
                                contentColor = Color.White,
                                containerColor = Color.Black,
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

    if (deleteConfirm) {
        Dialog(
            onDismissRequest = { deleteConfirm = false }
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFD9D9D9)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.35f)
                    .padding(8.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Remove Item?",
                        color = Color.Black,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    TextButton(
                        onClick = {
                            onDelete()
                            deleteConfirm = false
                            showPopup = false
                        },
                        colors = ButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.Black,
                            disabledContentColor = Color.White,
                            disabledContainerColor = Color.Black
                        ),
                        modifier = Modifier.width(250.dp)
                    ) {
                        Text("Remove", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (enableAddGroceryList) {
                        TextButton(
                            onClick = {
                                val gItem = GroceryItem(
                                    groceryID = item.inventoryID,
                                    name = item.name,
                                    measurementUnit = "",
                                    quantity = 1,
                                    checked = false,
                                    dateAdded = "03/21/2025"
                                )
                                groceryService.createItem(
                                    userId,
                                    gItem,
                                    onSuccess = {},
                                    onFailure = { exception ->
                                        Log.e(
                                            "Grocery",
                                            "Error creating grocery item: ${exception.message}"
                                        )
                                    })
                                onDelete()
                                deleteConfirm = false
                                showPopup = false
                            },

                            colors = ButtonColors(
                                contentColor = Color.White,
                                containerColor = Color.Black,
                                disabledContentColor = Color.White,
                                disabledContainerColor = Color.Black
                            ),
                            modifier = Modifier.width(250.dp)
                        ) {
                            Text("Add to Grocery List", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Close",
                        modifier = Modifier.clickable {
                            deleteConfirm = false
                        },
                        color = Color.Black,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun UnitToggle(selectedUnit: String, onUnitSelected: (String) -> Unit) {
    val units = listOf("PC", "ML", "G")
    val animatedOffset by animateDpAsState(
        targetValue = when (selectedUnit) {
            "PC" -> (-57).dp
            "ML" -> 4.dp
            else -> 62.dp
        }, label = "Toggle Animation"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .width(150.dp)
            .height(40.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Moving selection indicator
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .size(50.dp, 30.dp)
                .background(Color.White, shape = RoundedCornerShape(15.dp))
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            units.forEach { unit ->
                Text(
                    text = unit,
                    modifier = Modifier
                        .clickable { onUnitSelected(unit) }
                        .padding(horizontal = 8.dp),
                    color = if (unit == selectedUnit) Color.Black else Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun quantityWidget(quantity: Int, onDelete: () -> Unit): Int {
    var newQuantity by remember { mutableIntStateOf(quantity) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .width(150.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = {
                if (newQuantity > 0) {
                    newQuantity -= 1
                }
                if (newQuantity == 0) {
                    onDelete()
                }
            },
            colors = IconButtonColors(
                contentColor = Color.Black,
                containerColor = Color.White,
                disabledContentColor = Color.Black,
                disabledContainerColor = Color.White
            )
        ) {
            Icon(
                painterResource(R.drawable.minus),
                contentDescription = "Decrease Quantity",
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = "$newQuantity",
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal
        )
        IconButton(
            onClick = { newQuantity += 1 },
            colors = IconButtonColors(
                contentColor = Color.Black,
                containerColor = Color.White,
                disabledContentColor = Color.Black,
                disabledContainerColor = Color.White
            )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase Quantity")
        }
    }
    return newQuantity
}