package com.example.chopify.ui.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

class ReceiptScannerViewModel: ViewModel() {
    private val _scanResult = MutableStateFlow<JSONObject?>(null)
    val scanResult: StateFlow<JSONObject?> = _scanResult

    fun setScanResult(result: JSONObject) {
        _scanResult.value = result
    }
}
