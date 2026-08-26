package com.example.model

enum class UserRole {
    STUDENT,
    DRIVER,
    ADMIN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val avatarUrl: String = ""
)

data class StudentProfile(
    val userId: String,
    val studentId: String,
    val assignedBusId: String,
    val assignedStopId: String,
    val department: String = "Computer Science",
    val semester: String = "6th Sem",
    val emergencyContact: String = "+1 555-0199"
)

data class DriverProfile(
    val userId: String,
    val driverId: String,
    val assignedBusId: String,
    val licenseNumber: String = "DL-2024-88491",
    val experienceYears: Int = 8,
    val rating: Float = 4.9f
)
