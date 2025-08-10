package com.example.myapplication.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.model.MedicineEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import java.text.SimpleDateFormat
import java.util.*
import com.example.myapplication.alarm.AlarmReceiver

@RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
fun scheduleAlarm(context: Context, medicineName: String, dosage: String, time: String) {
    try {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("medicine_name", medicineName)
            putExtra("dosage", dosage)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (System.currentTimeMillis() % Int.MAX_VALUE).toInt(), // unique ID
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val parsedTime = sdf.parse(time) ?: Date()

        val cal = Calendar.getInstance().apply {
            timeInMillis = parsedTime.time  // ✅ This sets the time correctly
            val now = Calendar.getInstance()
            set(Calendar.YEAR, now.get(Calendar.YEAR))
            set(Calendar.MONTH, now.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1) // Schedule for next day if time passed
        }

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewScheduleScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var schedules by remember { mutableStateOf<List<EditableSchedule>>(emptyList()) }

    fun deleteScheduleFromBackend(id: String) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val token = getToken(context)
                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/schedule/$id")
                    .delete()
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Failed to delete schedule", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun updateScheduleInBackend(schedule: EditableSchedule) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val token = getToken(context)

                val jsonArray = JSONArray()
                schedule.medicines.forEach {
                    val obj = JSONObject()
                    obj.put("name", it.name)
                    obj.put("dosage", it.dosage)
                    obj.put("time", it.time)
                    obj.put("beforeOrAfterMeal", it.beforeOrAfterMeal)
                    jsonArray.put(obj)
                }

                val bodyJson = JSONObject().apply {
                    put("enabled", schedule.enabled)
                    put("medicines", jsonArray)
                }

                val requestBody: RequestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/schedule/${schedule.id}")
                    .patch(requestBody)
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                client.newCall(request).execute().use { response ->
                    withContext(Dispatchers.Main) {
                        if (!response.isSuccessful) {
                            Toast.makeText(context, "Failed to update: ${response.code} - ${response.body?.string()}", Toast.LENGTH_LONG).show()
                        } else {
                            println("✅ Successfully updated")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Update error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val token = getToken(context)
                val client = OkHttpClient()

                val request = Request.Builder()
                    .url("http://10.0.2.2:8080/schedule/my")
                    .addHeader("Authorization", "Bearer $token")
                    .build()

                val responseText = withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()
                    val body = response.body?.string()
                    println("Response code: ${response.code}")
                    println("Response body: $body")
                    if (response.isSuccessful && !body.isNullOrBlank()) body else null
                }

                withContext(Dispatchers.Main) {
                    if (responseText != null) {
                        schedules = parseEditableSchedules(responseText)
                    } else {
                        Toast.makeText(context, "Error loading schedules", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Exception: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Schedules") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No schedule available.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(schedules) { index, schedule ->
                    EditableScheduleCard(index, schedule,
                        onDelete = {
                            deleteScheduleFromBackend(schedule.id)
                            schedules = schedules.filterIndexed { i, _ -> i != index }
                        },
                        onToggleEnabled = { enabled ->
                            val updated = schedule.copy(enabled = enabled)
                            schedules = schedules.toMutableList().apply { this[index] = updated }
                            updateScheduleInBackend(updated)
                        },
                        onTimeChanged = { scheduleIndex, medicineIndex, newTime ->
                            val updatedMeds = schedules[scheduleIndex].medicines.toMutableList().apply {
                                this[medicineIndex] = this[medicineIndex].copy(time = newTime)
                            }
                            val updated = schedules[scheduleIndex].copy(medicines = updatedMeds)
                            schedules = schedules.toMutableList().apply { this[scheduleIndex] = updated }
                            updateScheduleInBackend(updated)
                        }
                    )
                }
            }
        }
    }
}

data class EditableSchedule(
    val id: String,
    val medicines: List<MedicineEntry>,
    val enabled: Boolean = true
)

@Composable
fun EditableScheduleCard(
    index: Int,
    schedule: EditableSchedule,
    onDelete: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onTimeChanged: (scheduleIndex: Int, medicineIndex: Int, newTime: String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("SCHEDULE ${index + 1}", style = MaterialTheme.typography.titleMedium)
                Row {
                    Switch(
                        checked = schedule.enabled,
                        onCheckedChange = { onToggleEnabled(it) }
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            schedule.medicines.forEachIndexed { medIndex, med ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)) {
                    Text("${med.name} (${med.dosage}) - ${med.beforeOrAfterMeal}")

                    OutlinedTextField(
                        value = med.time,
                        onValueChange = { newTime -> onTimeChanged(index, medIndex, newTime) },
                        label = { Text("Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

fun parseEditableSchedules(json: String): List<EditableSchedule> {
    val scheduleJson = JSONArray(json)
    val parsed = mutableListOf<EditableSchedule>()

    for (i in 0 until scheduleJson.length()) {
        val obj = scheduleJson.getJSONObject(i)
        val id = obj.getString("id")
        val medArray = obj.getJSONArray("medicines")
        val enabled = obj.optBoolean("enabled", true)

        val meds = mutableListOf<MedicineEntry>()
        for (j in 0 until medArray.length()) {
            val m = medArray.getJSONObject(j)
            meds.add(
                MedicineEntry(
                    m.getString("name"),
                    m.getString("dosage"),
                    m.getString("time"),
                    m.getString("beforeOrAfterMeal")
                )
            )
        }

        parsed.add(EditableSchedule(id = id, medicines = meds, enabled = enabled))
    }

    return parsed
}
