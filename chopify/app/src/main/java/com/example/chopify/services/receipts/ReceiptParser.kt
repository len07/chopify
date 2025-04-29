package com.example.chopify.services.receipts

import android.net.Uri
import android.util.Log
import com.example.chopify.BuildConfig
import com.example.chopify.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.Base64

class ReceiptParser(private val context: MainActivity) {

    suspend fun parse(uri: Uri): JSONObject {
        val jsonResponse = requestOCR(null, uri)
        val items = getItems(jsonResponse)

        return items
    }

    private fun requestOCR(fileName: String?, fileUri: Uri?): JSONObject {
        if (fileName == null && fileUri == null) {
            throw Exception("No file passed")
        }

        val endpoint = "https://vision.googleapis.com/v1/images:annotate"
        val url = "$endpoint?key=${BuildConfig.cloudVisionApiKey}"

        // OkHttp for REST http request to Google Cloud DocumentAI
        val client = OkHttpClient()

        val base64Image =
            if (fileUri != null) encodeUriToBase64(fileUri) else encodeFileToBase64(
                File(
                    context.filesDir,
                    fileName!!
                )
            )
        val jsonBody = JSONObject().apply {
            put("requests", JSONArray().put(
                JSONObject().apply {
                    put("image", JSONObject().put("content", base64Image))
                    put(
                        "features", JSONArray().put(
                            JSONObject().put("type", "DOCUMENT_TEXT_DETECTION")
                        )
                    )
                }
            ))
        }
        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")
            Log.d("ReceiptParser", "Successful parse")
            return JSONObject(response.body!!.string())
        }
    }

    // Parse InventoryItems from generated json
    private suspend fun getItems(json: JSONObject): JSONObject {
        val responses = json.getJSONArray("responses")
            .getJSONObject(0)

        if (!responses.has("fullTextAnnotation")) return JSONObject()

        val fullTextAnnotation = responses.getJSONObject("fullTextAnnotation")
        val text = fullTextAnnotation.getString("text")

        val chat = itemGenModel.startChat()
        val itemJsonResponse = JSONObject(chat.sendMessage(text).text!!)
        println(itemJsonResponse)

        return itemJsonResponse
    }

    private fun encodeFileToBase64(file: File): String {
        return Base64.getEncoder().encodeToString(file.readBytes())
    }

    private fun encodeUriToBase64(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                Base64.getEncoder().encodeToString(bytes)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}