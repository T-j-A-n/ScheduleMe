package com.example.myapplication.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import org.json.JSONObject
import com.example.myapplication.navigation.NavGraph

@Composable
fun CreateScheduleScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPdfUri = it
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst()) {
                    selectedFileName = cursor.getString(nameIndex) ?: ""
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Upload Prescription PDF to Create Schedule", style = MaterialTheme.typography.headlineSmall)

        Button(onClick = {
            pdfPickerLauncher.launch("application/pdf")
        }) {
            Text("Select PDF")
        }

        if (selectedFileName.isNotEmpty()) {
            Text("Selected File: $selectedFileName")
        }

        Button(onClick = {
            if (selectedPdfUri != null) {
                coroutineScope.launch {
                    uploadPrescriptionPdf(context, selectedPdfUri!!, navController)
                }
            } else {
                Toast.makeText(context, "Please select a PDF first.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("Create Schedule")
        }

        Button(onClick = {
            navController.navigate("home")
        }) {
            Text("Back")
        }
    }
}

suspend fun createSchedule(context: Context, prescriptionId: String, navController: NavController) {
    withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:8080/schedule/upload?prescriptionId=$prescriptionId")
                .post(RequestBody.create(null, ByteArray(0))) // empty form body
                .addHeader("Authorization", "Bearer " + getToken(context))
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Schedule created successfully!", Toast.LENGTH_SHORT).show()
                    navController.navigate("schedule_view") // ✅ navigate after success
                } else {
                    Toast.makeText(context, "Failed to create schedule: $responseText", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error creating schedule: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

suspend fun uploadPrescriptionPdf(context: Context, uri: Uri, navController: NavController)
 {
    withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "upload.pdf")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            val requestBody = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("description", "UploadedViaApp")
                .addFormDataPart("file", file.name, requestBody)
                .build()

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:8080/message/prescription/upload")
                .post(multipartBody)
                .addHeader("Authorization", "Bearer " + getToken(context))
                .build()

            val response = client.newCall(request).execute()
            val responseText = response.body?.string()

            withContext(Dispatchers.Main) {
                if (response.isSuccessful && responseText != null) {
                    val prescriptionId = org.json.JSONObject(responseText).getString("prescriptionId")

                    Toast.makeText(context, "Uploaded! Creating schedule...", Toast.LENGTH_SHORT).show()

                    createSchedule(context, prescriptionId, navController) // ✅ fixed here

                } else {
                    Toast.makeText(context, "Upload failed: $responseText", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}


fun getToken(context: Context): String? {
    val prefs = context.getSharedPreferences("auth", 0)
    return prefs.getString("jwt", null)
}
