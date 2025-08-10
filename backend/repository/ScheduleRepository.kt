package com.example.demo.repository

import com.example.demo.model.Schedule
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleRepository : MongoRepository<Schedule, String> {
    fun findAllByUserId(userId: String): List<Schedule>
    fun findByPrescriptionId(prescriptionId: String): Schedule?
}
