package com.example.demo.config

import com.example.demo.security.JwtAuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
        private val jwtAuthFilter: JwtAuthFilter // your custom JWT filter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .csrf { it.disable() }
                .authorizeHttpRequests {
                    it.requestMatchers("/auth/**").permitAll()
                            .requestMatchers("/uploads/**").permitAll()
                            .requestMatchers("/schedule/my").permitAll()
                            .requestMatchers("/schedule/**").permitAll()
                            .anyRequest().authenticated()

                }
                .sessionManagement {
                    it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                }
                .formLogin { it.disable() }
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java) // ✅ ADD THIS
        return http.build()
    }
}