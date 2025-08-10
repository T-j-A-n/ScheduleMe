package com.example.myapplication.model

data class MedicineEntry(
    val name: String,
    val dosage: String,
    var time: String,
    val beforeOrAfterMeal: String
)
