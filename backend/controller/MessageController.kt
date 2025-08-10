package com.example.demo.controller

import com.example.demo.model.Message
import com.example.demo.model.Prescription
import com.example.demo.repository.MessageRepository
import com.example.demo.repository.PrescriptionRepository
import com.example.demo.repository.UserRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import org.springframework.security.core.context.SecurityContextHolder

@RestController
@RequestMapping("/message")
class MessageController(private val userRepository: UserRepository, private val messageRepository: MessageRepository, private val prescriptionRepository: PrescriptionRepository){
    private val uploadDir = Path.of("uploads")

    init {
        Files.createDirectories(uploadDir)
    }

    @PostMapping("/upload")
    fun uploadMessage(
            @RequestParam name: String,
            @RequestParam age: Int,
            @RequestParam gender: String,
            @RequestParam text: String,
            @RequestParam file: MultipartFile
    ): Message {
        val filename = "${System.currentTimeMillis()}_${file.originalFilename}"
        val targetLocation = uploadDir.resolve(filename)
        Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

        val message = Message(
                name = name,
                age = age,
                gender = gender,
                text = text,
                pdfFilename = filename
        )
        return messageRepository.save(message)
    }

    @PostMapping("/prescription/upload")
    fun uploadPrescription(
            @RequestParam description: String,
            @RequestParam file: MultipartFile
    ): ResponseEntity<Map<String, String>> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
                ?: throw RuntimeException("User not found")

        val filename = "${System.currentTimeMillis()}_${file.originalFilename}"
        val targetLocation = uploadDir.resolve(filename)
        Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

        val prescription = Prescription(
                userId = user.id.toString(),
                description = description,
                pdfFilename = filename
        )

        val saved = prescriptionRepository.save(prescription)
        return ResponseEntity.ok(mapOf("prescriptionId" to saved.id!!))
    }

    @GetMapping("/check")
    fun getAllMessages(): List<Message>{
        return messageRepository.findAll()
    }
    @GetMapping("/profile")
    fun getUserProfile(): Map<String, Any> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
                ?: throw RuntimeException("User not found")

        return mapOf(
                "name" to user.username,
                //"age" to 12 // Assuming `AppUser` has an `age` field
        )
    }
    @GetMapping("/prescriptions")
    fun getUserPrescriptions(): List<Prescription> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
                ?: throw RuntimeException("User not found")

        return prescriptionRepository.findAllByUserId(user.id.toString())
    }
}

