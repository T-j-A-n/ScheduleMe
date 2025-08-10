package com.example.demo.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.*

@Component
class JwtUtil(){
    private val secret = "aSuperSecretJWTKeyThatIsAtLeast32Bytes"
    private val expirationMs = 86400000

    fun generateToken(username: String): String {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date())
                .setExpiration(Date(System.currentTimeMillis() + expirationMs))
                .signWith(Keys.hmacShaKeyFor(secret.toByteArray()), SignatureAlgorithm.HS256)
                .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            val claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.toByteArray()))
                    .build()
                    .parseClaimsJws(token)
            !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            false
        }
    }

    fun getUsername(token: String): String {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.toByteArray()))
                .build()
                .parseClaimsJws(token)
                .body
                .subject
    }
}