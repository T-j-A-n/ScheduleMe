package com.example.myapplication.model

data class ScheduleUi(
    val id: String,
    var enabled: Boolean = true,
    var medicines: List<MedicineEntry>
)

