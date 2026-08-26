package com.example.data

import kotlin.math.*

object GeoUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Calculates distance in meters between two coordinates using Haversine formula
     */
    fun calculateDistanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Calculates distance in kilometers formatted as string (e.g. "4.2 km")
     */
    fun formatDistance(meters: Double): String {
        return if (meters < 1000) {
            "${meters.roundToInt()} m"
        } else {
            val km = meters / 1000.0
            String.format(java.util.Locale.US, "%.1f km", km)
        }
    }

    /**
     * Calculates estimated time of arrival in minutes based on distance and average speed (default 25 km/h in city)
     */
    fun calculateEtaMinutes(distanceMeters: Double, speedKmH: Float = 25f): Int {
        val effectiveSpeed = if (speedKmH > 5f) speedKmH else 25f
        val distanceKm = distanceMeters / 1000.0
        val hours = distanceKm / effectiveSpeed
        val minutes = (hours * 60).roundToInt()
        return max(1, minutes)
    }

    /**
     * Calculates heading/bearing in degrees (0..360) from point 1 to point 2
     */
    fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
        val brng = Math.toDegrees(atan2(y, x))
        return ((brng + 360) % 360).toFloat()
    }

    /**
     * Linearly interpolates between two geo points with a fraction t in [0..1]
     */
    fun interpolatePoint(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        fraction: Double
    ): Pair<Double, Double> {
        val lat = lat1 + (lat2 - lat1) * fraction
        val lon = lon1 + (lon2 - lon1) * fraction
        return Pair(lat, lon)
    }
}
