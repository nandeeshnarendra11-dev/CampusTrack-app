package com.example.model

data class Stop(
    val stopId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val sequence: Int,
    val landmark: String = "",
    val morningPickupTime: String = "07:30 AM",
    val eveningDropTime: String = "04:45 PM"
)

data class Route(
    val routeId: String,
    val routeName: String,
    val description: String = "",
    val colorHex: Long = 0xFF4F46E5, // Indigo default
    val stops: List<Stop> = emptyList(),
    val totalDistanceKm: Double = 14.5,
    val estimatedDurationMin: Int = 35
)
