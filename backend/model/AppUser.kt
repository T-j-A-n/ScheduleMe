package com.example.demo.model

import org.springframework.data.annotation.Id

data class AppUser(
        @Id val id: String? = null,
        val username: String,
        val password: String
)