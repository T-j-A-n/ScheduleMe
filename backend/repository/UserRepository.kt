package com.example.demo.repository
import com.example.demo.model.AppUser
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository



@Repository
interface UserRepository : MongoRepository<AppUser, String> {
    fun findByUsername(username: String): AppUser?
}