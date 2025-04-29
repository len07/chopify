package com.example.chopify.ui.navigation

sealed class Screen(val route: String) {
    object SignUp : Screen("signup")
    object Login : Screen("Login")
    object Inventory : Screen("inventory")
    object ReceiptScanner : Screen("receipt_scanner")
    object GroceryList : Screen("grocery_list")
    object ScanResult : Screen("scan_result")
    object Loading : Screen("loading")
    object Setting : Screen("setting")
}
