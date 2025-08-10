package com.example.myapplication.model

data class EditSchedule (
    val id: String,
    val medicines: List<MedicineEntry>,
    val enabled: Boolean = true
)