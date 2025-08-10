package com.example.demo.repository

import com.example.demo.model.Message
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : MongoRepository<Message, String>

