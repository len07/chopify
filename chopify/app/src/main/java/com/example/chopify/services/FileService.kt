package com.example.chopify.services

import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.example.chopify.MainActivity
import java.io.File

class FileService(private val context: MainActivity) {

    private var onFileSelected: ((Uri) -> Unit)? = null

    private val filePickerLauncher =
        context.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val savedUri = saveFileToInternalStorage(it)
                onFileSelected?.invoke(savedUri)
            }
        }

    fun openFilePicker(onFilePicked: (Uri) -> Unit) {
        onFileSelected = onFilePicked
        filePickerLauncher.launch("*/*") // Accept all file types
    }

    private fun saveFileToInternalStorage(uri: Uri): Uri {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uri
        val fileName = getFileName(uri)
        val file = File(context.filesDir, fileName)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        Log.d("FileService", "File saved to app storage: ${file.absolutePath}")

        return Uri.fromFile(file)
    }

    fun getFileName(uri: Uri): String {
        var name = "default_filename"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        return name
    }
}
