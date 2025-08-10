package com.example.myapplication.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val name = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fetchProfile(context, name)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome, ${name.value}!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("prescriptions")
        }) {
            Text("View Prescriptions")
        }

        Button(onClick = {
            navController.navigate("create_schedule")
        }) {
            Text("Create Schedule")
        }
        Button(onClick = { navController.navigate("schedule_view") }) {
            Text("View My Schedule")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit().clear().apply()
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }) {
            Text("Logout")
        }
    }
}

suspend fun fetchProfile(context: Context, name: MutableState<String>) {
    withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val token = prefs.getString("jwt", null)

            if (token == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
                }
                return@withContext
            }

            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://10.0.2.2:8080/message/profile")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                withContext(Dispatchers.Main) {
                    name.value = json.getString("name")
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: $responseBody", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
