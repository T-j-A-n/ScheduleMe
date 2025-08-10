package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.RegisterScreen
import com.example.myapplication.ui.screens.PrescriptionScreen
import com.example.myapplication.ui.screens.CreateScheduleScreen
import com.example.myapplication.ui.screens.ViewScheduleScreen
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("home") {
            HomeScreen(navController)
        }
        composable("prescriptions") {
            PrescriptionScreen(navController)
        }
        composable("create_schedule") {
            CreateScheduleScreen(navController)
        }
        composable("schedule_view") {
            ViewScheduleScreen(navController)
        }
    }
}
