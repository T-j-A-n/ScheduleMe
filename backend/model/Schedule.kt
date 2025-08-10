package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "schedules")
data class Schedule(
        @Id val id: String? = null,
        val userId: String,
        val prescriptionId: String,
        var medicines: List<MedicineEntry>,
        var enabled: Boolean = true,
        val createdAt: Long = System.currentTimeMillis()
)