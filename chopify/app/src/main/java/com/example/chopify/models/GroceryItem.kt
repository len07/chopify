package com.example.chopify.models

data class GroceryItem(
    var groceryID: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val measurementUnit: String = "PC",
    val dateAdded: String = "",
    val checked: Boolean = false
)
