package com.example.demo.security

import com.example.demo.security.JwtUtil
import com.example.demo.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
        private val jwtUtil: JwtUtil,
        private val userRepository: UserRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)
            if (jwtUtil.validateToken(token)) {
                val username = jwtUtil.getUsername(token)
                val userDetails = userRepository.findByUsername(username)

                if (userDetails != null) {
                    val auth = UsernamePasswordAuthenticationToken(username, null, emptyList())
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        }
        filterChain.doFilter(request, response)
    }
}