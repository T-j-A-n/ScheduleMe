package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "messages")
data class Message(
        @Id val id: String? = null,
        val name: String,
        val age: Int,
        val gender: String,
        val text: String,
        val pdfFilename: String
)

