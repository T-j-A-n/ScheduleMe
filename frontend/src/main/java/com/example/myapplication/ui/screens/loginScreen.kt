package com.example.myapplication.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val username = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        OutlinedTextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val client = OkHttpClient()
                        val json = JSONObject()
                        json.put("username", username.value)
                        json.put("password", password.value)

                        val body = json.toString()
                            .toRequestBody("application/json".toMediaType())

                        val request = Request.Builder()
                            .url("http://10.0.2.2:8080/auth/login") // emulator loopback
                            .post(body)
                            .build()

                        val response = client.newCall(request).execute()
                        val responseBody = response.body?.string()

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val token = JSONObject(responseBody ?: "").optString("token", "")
                                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()

                                // Save token in shared preferences (optional)
                                val prefs = context.getSharedPreferences("auth", 0)
                                prefs.edit().putString("jwt", token).apply()

                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                val error = JSONObject(responseBody ?: "{}").optString("error", "Login failed.")
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        }

                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text("Don't have an account? Register")
        }
    }
}
