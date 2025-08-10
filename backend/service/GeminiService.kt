package com.example.demo.service

import com.example.demo.model.MedicineEntry
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL

@Service
class GeminiService {

    private val apiKey = "xxxx" // Replace with your own gemini token 
    private val model = "gemini-1.5-flash"
    private val endpoint =
            "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
    private val objectMapper = jacksonObjectMapper()

    fun generateScheduleFromText(ocrText: String, location: String): List<MedicineEntry> {
        if (ocrText.isBlank()) {
            throw RuntimeException("OCR text is empty. Cannot generate schedule.")
        }

        val prompt = """
            You are a medical assistant. Based on the following prescription text and considering typical meal times in $location, generate a schedule in this **exact JSON** format:
            
            [
              {
                "name": "Paracetamol",
                "dosage": "500mg",
                "time": "08:00 AM",
                "beforeOrAfterMeal": "before food"
              }
            ]
            
            Prescription Text:
            $ocrText
        """.trimIndent()

        val requestBody = mapOf(
                "contents" to listOf(
                        mapOf("parts" to listOf(mapOf("text" to prompt)))
                )
        )

        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true

        val requestJson = objectMapper.writeValueAsString(requestBody)
        connection.outputStream.use { it.write(requestJson.toByteArray()) }

        val response = connection.inputStream.bufferedReader().readText()
        val jsonNode: JsonNode = objectMapper.readTree(response)

        val rawText = jsonNode["candidates"]
                ?.get(0)
                ?.get("content")
                ?.get("parts")
                ?.get(0)
                ?.get("text")
                ?.asText()
                ?: throw RuntimeException("Gemini did not return a valid 'text' response")

        println("DEBUG: Gemini raw response = \n$rawText")

        // Clean and parse the JSON if wrapped in triple backticks
        val cleaned = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

        return try {
            objectMapper.readValue(cleaned)
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse Gemini response: ${e.message}\nRaw: $cleaned")
        }
    }
}
