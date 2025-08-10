package com.example.demo.model

data class MedicineEntry(
        val name: String,               // e.g., "Paracetamol"
        val dosage: String,             // e.g., "500mg"
        val time: String,               // e.g., "08:00 AM"
        val beforeOrAfterMeal: String   // e.g., "before food"
)
