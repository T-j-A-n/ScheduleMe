package com.example.demo.controller

import com.example.demo.model.AppUser
import com.example.demo.repository.UserRepository
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import com.example.demo.security.JwtUtil

@RestController
@RequestMapping("/auth")
class AuthController(
        private val userRepository: UserRepository,
        private val jwtUtil: JwtUtil,
        private val passwordEncoder: PasswordEncoder
) {
    @PostMapping("/register")
    fun register(@RequestBody user: AppUser): ResponseEntity<String> {
        val newUser = user.copy(password = passwordEncoder.encode(user.password))
        userRepository.save(newUser)
        return ResponseEntity.ok("User registered")
    }

    @PostMapping("/login")
    fun login(@RequestBody login: AppUser): ResponseEntity<Map<String, String>> {
        val user = userRepository.findByUsername(login.username)
                ?: return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf("error" to "Invalid username"))

        return if (passwordEncoder.matches(login.password, user.password)) {
            val token = jwtUtil.generateToken(user.username)
            ResponseEntity.ok(mapOf("token" to token))
        } else {
            ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("error" to "Invalid password"))
        }
    }
}