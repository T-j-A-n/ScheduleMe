package com.example.demo.repository

import com.example.demo.model.Prescription
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PrescriptionRepository : MongoRepository<Prescription, String> {
    fun findAllByUserId(userId: String): List<Prescription>
}