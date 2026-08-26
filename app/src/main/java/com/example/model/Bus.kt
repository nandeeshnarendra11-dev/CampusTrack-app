package com.example.model

enum class BusStatus(val label: String, val emoji: String) {
    ON_ROUTE("On Route", "🟢"),
    ARRIVING_SOON("Arriving Soon", "🟡"),
    DELAYED("Delayed", "🔴"),
    TRIP_COMPLETED("Trip Completed", "⚪")
}

data class Bus(
    val busId: String,
    val busNumber: String,
    val registrationNumber: String,
    val driverId: String,
    val routeId: String,
    val status: BusStatus = BusStatus.ON_ROUTE,
    val capacity: Int = 45,
    val occupancy: Int = 28,
    val isDemoBus: Boolean = false,
    val modelName: String = "Volvo 9400 Transit"
)
