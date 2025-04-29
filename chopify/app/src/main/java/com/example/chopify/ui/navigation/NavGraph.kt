package com.example.chopify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.chopify.MainActivity
import com.example.chopify.models.UserPreferences
import com.example.chopify.services.FirebaseService
import com.example.chopify.services.GroceryService
import com.example.chopify.services.InventoryService
import com.example.chopify.services.LoginService
import com.example.chopify.services.UserService
import com.example.chopify.ui.screens.GroceryListScreen
import com.example.chopify.ui.screens.InventoryScreen
import com.example.chopify.ui.screens.LoadingScreen
import com.example.chopify.ui.screens.LoginScreen
import com.example.chopify.ui.screens.ReceiptScannerScreen
import com.example.chopify.ui.screens.ScanResultScreen
import com.example.chopify.ui.screens.SettingScreen
import com.example.chopify.ui.screens.SignUpScreen

@Composable
fun NavGraph(
    context: MainActivity,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val firebaseService = remember { FirebaseService() }
    val userService = remember { UserService(firebaseService) }
    val loginService = remember { LoginService(firebaseService) }
    val inventoryService = remember { InventoryService(firebaseService) }
    val groceryService = remember { GroceryService(firebaseService) }
    val viewModel: ReceiptScannerViewModel = viewModel()
    val userPreferences = remember { UserPreferences(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                context = context,
                onSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                loginService = loginService,
                onLoginSuccess = {
                    navController.navigate(Screen.Inventory.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                userService = userService,
                onLoginSuccess = {
                    navController.navigate(Screen.Inventory.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onReturn = { navController.popBackStack() }
            )
        }
        composable(Screen.Inventory.route) {
            InventoryScreen(
                navController,
                inventoryService = inventoryService,
                groceryService = groceryService,
                userPreferences,
                context
            )
        }
        composable(Screen.GroceryList.route) {
            GroceryListScreen(
                navController,
                groceryService = groceryService
            )
        }
        composable(Screen.ReceiptScanner.route) {
            ReceiptScannerScreen(
                context,
                navController,
                viewModel
            )
        }
        composable(Screen.ScanResult.route) {
            ScanResultScreen(
                navController,
                inventoryService,
                groceryService,
                viewModel,
                userPreferences,
                context
            )
        }
        composable(Screen.Loading.route) { LoadingScreen() }
        composable(Screen.Setting.route) {
            SettingScreen(
                loginService = loginService,
                userService,
                navController,
                onLogoutSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Inventory.route) { inclusive = true }
                    }
                },
                userPreferences,
                context
            )
        }
    }
}
