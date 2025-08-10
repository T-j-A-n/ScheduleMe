package com.example.demo.controller

import com.example.demo.model.Schedule
import com.example.demo.model.MedicineEntry
import com.example.demo.repository.PrescriptionRepository
import com.example.demo.repository.ScheduleRepository
import com.example.demo.repository.UserRepository
import com.example.demo.service.GeminiService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import java.io.File
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

@RestController
@RequestMapping("/schedule")
class ScheduleController(
        private val userRepository: UserRepository,
        private val prescriptionRepository: PrescriptionRepository,
        private val scheduleRepository: ScheduleRepository,
        private val geminiService: GeminiService
) {

    @PostMapping("/upload")
    fun uploadSchedule(@RequestParam prescriptionId: String): ResponseEntity<Schedule> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username)
                ?: return ResponseEntity.badRequest().build()

        val prescription = prescriptionRepository.findById(prescriptionId)
        if (prescription.isEmpty) {
            return ResponseEntity.notFound().build()
        }

        val pdfPath = "uploads/${prescription.get().pdfFilename}"
        println("Looking for PDF at path: $pdfPath")
        println("File exists: ${File(pdfPath).exists()}")
        val ocrText = extractTextFromPdf(pdfPath)
        println("DEBUG: OCR Extracted text: [$ocrText]")
        val userRegion = getUserRegionBasedOnIP()

        // Send extracted text + region to Gemini for schedule generation
        val scheduleList: List<MedicineEntry> = geminiService.generateScheduleFromText(ocrText, userRegion)

        val schedule = Schedule(
                userId = user.id.toString(),
                prescriptionId = prescriptionId,
                medicines = scheduleList
        )

        //return ResponseEntity.ok(scheduleRepository.save(schedule))
        return try {
            val saved = scheduleRepository.save(schedule)
            println("✅ Successfully saved schedule: $saved")
            ResponseEntity.ok(saved)
        } catch (e: Exception) {
            println("❌ Error saving schedule: ${e.message}")
            ResponseEntity.internalServerError().build()
        }
    }
    @PatchMapping("/{id}")
    fun updateSchedule(
            @PathVariable id: String,
            @RequestBody updateRequest: Map<String, Any>
    ): ResponseEntity<Schedule> {
        val username = SecurityContextHolder.getContext().authentication.name
        println("🔐 Authenticated username: $username")

        val user = userRepository.findByUsername(username)
        println("🧍 Found user: $user")

        val schedule = scheduleRepository.findById(id).orElse(null)
        println("📋 Schedule with ID $id: $schedule")

        if (schedule == null) return ResponseEntity.notFound().build()
        if (user != null) {
            if (schedule.userId != user.id.toString()) {
                return ResponseEntity.status(403).build()
            }
        }

        updateRequest["enabled"]?.let {
            schedule.enabled = it as Boolean
        }

        updateRequest["medicines"]?.let { meds ->
            val medicineList = (meds as List<Map<String, String>>).map {
                MedicineEntry(
                        name = it["name"] ?: "",
                        dosage = it["dosage"] ?: "",
                        time = it["time"] ?: "",
                        beforeOrAfterMeal = it["beforeOrAfterMeal"] ?: ""
                )
            }
            schedule.medicines = medicineList
        }

        return ResponseEntity.ok(scheduleRepository.save(schedule))
    }

    @DeleteMapping("/{id}")
    fun deleteSchedule(@PathVariable id: String): ResponseEntity<Void> {
        val username = SecurityContextHolder.getContext().authentication.name
        val user = userRepository.findByUsername(username) ?: return ResponseEntity.badRequest().build()

        val schedule = scheduleRepository.findById(id).orElse(null) ?: return ResponseEntity.notFound().build()
        if (schedule.userId != user.id.toString()) {
            return ResponseEntity.status(403).build()
        }

        scheduleRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/my")
    fun getMySchedules(): ResponseEntity<List<Schedule>> {
        println("⚡ /schedule/my was hit")
        val username = SecurityContextHolder.getContext().authentication.name
        println("Fetching schedules for user: $username")

        val user = userRepository.findByUsername(username)
        if (user == null) {
            println("User not found for username: $username")
            return ResponseEntity.badRequest().build()
        }

        val schedules = scheduleRepository.findAllByUserId(user.id.toString())
        println("Found ${schedules.size} schedules for user: ${user.id}")

        return ResponseEntity.ok(schedules)
    }

    fun extractTextFromPdf(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) {
            throw RuntimeException("PDF file not found at path: $filePath")
        }

        PDDocument.load(file).use { document ->
            val stripper = PDFTextStripper()
            return stripper.getText(document).trim()
        }
    }


    fun getUserRegionBasedOnIP(): String {
        return "South India" // Replace with IP-based geolocation logic if needed
    }
}
