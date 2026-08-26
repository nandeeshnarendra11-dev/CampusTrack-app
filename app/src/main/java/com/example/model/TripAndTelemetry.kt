package com.example.model

enum class TripStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
    NOT_STARTED
}

data class Trip(
    val tripId: String,
    val busId: String,
    val driverId: String,
    val routeId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val status: TripStatus = TripStatus.ACTIVE,
    val currentStopIndex: Int = 0,
    val distanceCoveredKm: Double = 0.0,
    val averageSpeedKmH: Double = 32.0
)

data class LiveLocation(
    val busId: String,
    val tripId: String = "",
    val latitude: Double,
    val longitude: Double,
    val speedKmH: Float = 0f,
    val headingDegrees: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val isGpsLive: Boolean = true
)

enum class NotificationType {
    ARRIVAL_ALERT,
    TRIP_STARTED,
    TRIP_COMPLETED,
    DELAY,
    ROUTE_CHANGE,
    EMERGENCY
}

data class NotificationItem(
    val id: String,
    val userId: String = "", // Empty for broadcast
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.ARRIVAL_ALERT,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false,
    val busId: String? = null
)

data class EmergencyAlert(
    val alertId: String,
    val busId: String,
    val driverId: String,
    val driverName: String,
    val busNumber: String,
    val routeName: String,
    val message: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)
