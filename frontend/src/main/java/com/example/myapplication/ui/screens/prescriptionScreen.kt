package com.example.myapplication.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.ui.Alignment

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun PrescriptionScreen(navController: NavController) {
    val context = LocalContext.current
    var prescriptions by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val token = context.getSharedPreferences("auth", 0).getString("jwt", null)
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:8080/message/prescriptions")
                .addHeader("Authorization", "Bearer $token")
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && body != null) {
                val jsonArray = JSONArray(body)
                prescriptions = (0 until jsonArray.length()).map {
                    val obj = jsonArray.getJSONObject(it)
                    "http://10.0.2.2:8080/uploads/${obj.getString("pdfFilename")}"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Your Prescriptions", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        prescriptions.forEach { pdfUrl ->
            PrescriptionPreview(pdfUrl)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp)) // optional breathing room

        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back to Home")
        }
    }
}



suspend fun fetchPrescriptions(context: Context, prescriptions: MutableList<String>) {
    withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val token = prefs.getString("jwt", null) ?: return@withContext

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:8080/message/prescriptions")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val jsonArray = JSONArray(responseBody)
                val resultList = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val desc = obj.getString("description")
                    resultList.add(desc)
                }
                withContext(Dispatchers.Main) {
                    prescriptions.clear()
                    prescriptions.addAll(resultList)
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to load prescriptions", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
