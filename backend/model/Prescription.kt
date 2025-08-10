package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("prescriptions")
data class Prescription(
        @Id val id: String? = null,
        val userId: String,
        val description: String,
        val pdfFilename: String
)