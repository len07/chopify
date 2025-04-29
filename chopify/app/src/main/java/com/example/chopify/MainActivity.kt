package com.example.chopify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.chopify.services.FileService
import com.example.chopify.services.channelID
import com.example.chopify.ui.navigation.NavGraph
import com.example.chopify.ui.navigation.Screen
import com.example.chopify.ui.theme.ChopifyTheme
import com.example.chopify.ui.theme.DarkGreen
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    val fileService = FileService(this)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        // Camera permissions
        if (!hasRequiredPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                0
            )
        }

        FirebaseApp.initializeApp(this)

        setContent {
            ChopifyTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute != Screen.Login.route
                            && currentRoute != Screen.SignUp.route
                            && currentRoute != Screen.ScanResult.route
                            && currentRoute != Screen.Loading.route
                        ) {
                            BottomNavigationBar(
                                currentRoute = currentRoute,
                                onItemSelected = { navController.navigate(it) }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavGraph(this, navController, Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun BottomNavigationBar(currentRoute: String?, onItemSelected: (String) -> Unit) {
        val items = listOf(
            Screen.Inventory to Triple(
                "Inventory",
                R.drawable.inventoryline,
                R.drawable.inventoryfill
            ),
            Screen.ReceiptScanner to Triple(
                "Scanner",
                R.drawable.scan,
                R.drawable.scan
            ),
            Screen.GroceryList to Triple(
                "Grocery List",
                R.drawable.cartline,
                R.drawable.cartfill
            )
        )

        NavigationBar {
            items.forEach { (screen, data) ->
                val (label, lineIconRes, fillIconRes) = data
                val isSelected = currentRoute == screen.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onItemSelected(screen.route) },
                    icon = {
                        Image(
                            painter = painterResource(id = if (isSelected) fillIconRes else lineIconRes),
                            contentDescription = label,
                            modifier = Modifier.size(34.dp),
                            colorFilter = ColorFilter.tint(
                                if (isSelected) DarkGreen
                                else Color.Gray
                            )
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            color = if (isSelected) DarkGreen
                            else Color.Gray
                        )
                    },
                )
            }
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        val camera = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        val notif = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED

        return camera && notif
    }

    private fun createNotificationChannel() {
        val name = "Notification Channel"
        val desc = "A Description of the Channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}