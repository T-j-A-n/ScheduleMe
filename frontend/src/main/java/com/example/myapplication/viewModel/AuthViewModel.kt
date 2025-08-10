package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.network.AuthRequest
import com.example.myapplication.network.AuthResponse
import com.example.myapplication.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _authResult = MutableStateFlow<Result<AuthResponse>?>(null)
    val authResult: StateFlow<Result<AuthResponse>?> = _authResult

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authService.login(AuthRequest(username, password))
                if (response.isSuccessful) {
                    _authResult.value = Result.success(response.body()!!)
                } else {
                    _authResult.value = Result.failure(Exception("Login failed"))
                }
            } catch (e: Exception) {
                _authResult.value = Result.failure(e)
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.authService.register(AuthRequest(username, password))
                if (response.isSuccessful) {
                    _authResult.value = Result.success(response.body()!!)
                } else {
                    _authResult.value = Result.failure(Exception("Register failed"))
                }
            } catch (e: Exception) {
                _authResult.value = Result.failure(e)
            }
        }
    }
}
