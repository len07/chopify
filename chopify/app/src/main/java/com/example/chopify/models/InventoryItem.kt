package com.example.chopify.models

data class InventoryItem(
    var inventoryID: String = "",
    var name: String = "",
    var quantity: Int = 0,
    var measurementUnit: String = "PC",
    var expiryDate: String? = null,
    val dateAdded: String = ""
)
