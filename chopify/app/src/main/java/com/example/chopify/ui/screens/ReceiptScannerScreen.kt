package com.example.chopify.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.chopify.MainActivity
import com.example.chopify.R
import com.example.chopify.services.receipts.ReceiptParser
import com.example.chopify.ui.navigation.ReceiptScannerViewModel
import com.example.chopify.ui.navigation.Screen
import com.example.chopify.ui.theme.DarkGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@Composable
fun ReceiptScannerScreen(
    mainActivity: MainActivity,
    navController: NavController,
    viewModel: ReceiptScannerViewModel
) {
    val context = LocalContext.current
    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE
            )
        }
    }

    // Store captured image
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding()
    ) {
        when {
            // Show live camera
            capturedImageUri == null -> {
                CameraPreview(
                    controller = controller,
                    modifier = Modifier.fillMaxSize()
                )
                // Gallery + capture buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CameraControls(
                        context = context,
                        mainActivity = mainActivity,
                        controller = controller,
                        onPhotoTaken = { uri ->
                            capturedImageUri = uri
                            Log.d("Photo", "Photo taken: $uri")
                        }
                    )
                }
            }

            // Show image preview
            else -> {
                Image(
                    painter = rememberAsyncImagePainter(capturedImageUri),
                    contentDescription = "Captured Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Retake + save buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter) // Align the row to the bottom
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RetakeSaveButtons(
                        onRetake = { capturedImageUri = null },
                        onScan = {
                            Log.d("Click", "Use this picture")
                            // Load screen until callback scanPhoto is done
                            navController.navigate(Screen.Loading.route)
                            scanPhoto(mainActivity, capturedImageUri!!, viewModel,
                                onScanComplete = {
                                    navController.navigate(Screen.ScanResult.route)
                                },
                                onScanFail = {
                                    navController.navigate(Screen.Inventory.route)
                                }
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun RetakeSaveButtons(
    onRetake: () -> Unit,
    onScan: () -> Unit,
) {
    // Retake button
    Button(
        onClick = {
            onRetake()
            Log.d("Click", "Go back")
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkGreen,
            contentColor = Color.White
        )
    ) {
        Icon(
            Icons.Filled.Clear,
            contentDescription = "Back",
            modifier = Modifier.padding(0.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Retake")
    }
    // Scan button
    Button(
        onClick = { onScan() },
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkGreen,
            contentColor = Color.White
        )
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = "Forward",
            modifier = Modifier.padding(0.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Scan")
    }
}

@Composable
fun CameraPreview(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = {
            PreviewView(it).apply {
                this.controller = controller
                controller.bindToLifecycle(lifecycleOwner)
            }
        },
        modifier = modifier
    )
}

@Composable
fun CameraControls(
    context: Context,
    mainActivity: MainActivity,
    controller: LifecycleCameraController,
    onPhotoTaken: (Uri) -> Unit
) {
    // Gallery Button
    IconButton(
        onClick = {
            mainActivity.fileService.openFilePicker { selectedUri ->
                onPhotoTaken(selectedUri)
            }
        },
        modifier = Modifier.size(50.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.photograph),
            contentDescription = "Open gallery",
            modifier = Modifier.size(40.dp),
            colorFilter = ColorFilter.tint(Color.White)
        )
    }
    Spacer(modifier = Modifier.width(60.dp))
    // Capture Button
    IconButton(
        onClick = {
            takePhoto(
                context = context,
                controller = controller,
                onPhotoTaken = onPhotoTaken
            )
        },
        modifier = Modifier.size(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(5.dp, Color.Gray, CircleShape)
        )
    }
    Spacer(modifier = Modifier.width(112.dp))
}


private fun takePhoto(
    context: Context,
    controller: LifecycleCameraController,
    onPhotoTaken: (Uri) -> Unit
) {
    val outputDirectory = context.filesDir // Change this to another directory to save pic?
    val photoFile = File(outputDirectory, "photo_${System.currentTimeMillis()}.jpg")

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    controller.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onPhotoTaken(Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}

private fun scanPhoto(
    mainActivity: MainActivity,
    uri: Uri,
    viewModel: ReceiptScannerViewModel,
    onScanComplete: () -> Unit,
    onScanFail: () -> Unit
) {

    // Parse receipt
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val rp = ReceiptParser(mainActivity)
            val itemJsonResponse = rp.parse(uri)
            withContext(Dispatchers.Main) {
                viewModel.setScanResult(itemJsonResponse)
                onScanComplete()
            }
        } catch (e: Exception) {
            Log.e("ReceiptScanner", "Error scanning receipt: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    mainActivity,
                    "Failed to scan receipt. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
                onScanFail()
            }
        }
    }
}

