package com.example.data

import com.example.model.*
import kotlin.math.sin
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class CampusTransitRepository private constructor() {

    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Auth & User State
    private val _currentUser = MutableStateFlow<User?>(DemoDataProvider.initialUsers[0]) // Defaults to Student
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(DemoDataProvider.initialUsers)
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // Core Transit Entities
    private val _buses = MutableStateFlow<List<Bus>>(DemoDataProvider.initialBuses)
    val buses: StateFlow<List<Bus>> = _buses.asStateFlow()

    private val _routes = MutableStateFlow<List<Route>>(DemoDataProvider.initialRoutes)
    val routes: StateFlow<List<Route>> = _routes.asStateFlow()

    private val _students = MutableStateFlow<List<StudentProfile>>(DemoDataProvider.initialStudents)
    val students: StateFlow<List<StudentProfile>> = _students.asStateFlow()

    private val _drivers = MutableStateFlow<List<DriverProfile>>(DemoDataProvider.initialDrivers)
    val drivers: StateFlow<List<DriverProfile>> = _drivers.asStateFlow()

    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(DemoDataProvider.initialNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _emergencyAlerts = MutableStateFlow<List<EmergencyAlert>>(emptyList())
    val emergencyAlerts: StateFlow<List<EmergencyAlert>> = _emergencyAlerts.asStateFlow()

    // Real-Time Telemetry Map: busId -> LiveLocation
    private val _liveLocations = MutableStateFlow<Map<String, LiveLocation>>(emptyMap())
    val liveLocations: StateFlow<Map<String, LiveLocation>> = _liveLocations.asStateFlow()

    // Active trip for logged in driver
    private val _driverActiveTrip = MutableStateFlow<Trip?>(null)
    val driverActiveTrip: StateFlow<Trip?> = _driverActiveTrip.asStateFlow()

    // Demo Simulation State
    private val _isDemoModeEnabled = MutableStateFlow(true)
    val isDemoModeEnabled: StateFlow<Boolean> = _isDemoModeEnabled.asStateFlow()

    private val _simulationSpeedMultiplier = MutableStateFlow(1.0f)
    val simulationSpeedMultiplier: StateFlow<Float> = _simulationSpeedMultiplier.asStateFlow()

    private var simulationJob: Job? = null

    // Bus Simulation Progress: busId -> Progress float between [0..stops.size]
    private val busProgressMap = mutableMapOf<String, Double>()

    init {
        // Initialize initial locations for demo buses at their first stop
        val initialLocs = mutableMapOf<String, LiveLocation>()
        for (bus in DemoDataProvider.initialBuses) {
            val route = DemoDataProvider.initialRoutes.find { it.routeId == bus.routeId }
            if (route != null && route.stops.isNotEmpty()) {
                val firstStop = route.stops[0]
                initialLocs[bus.busId] = LiveLocation(
                    busId = bus.busId,
                    latitude = firstStop.latitude,
                    longitude = firstStop.longitude,
                    speedKmH = 32f,
                    headingDegrees = 45f,
                    timestamp = System.currentTimeMillis()
                )
                busProgressMap[bus.busId] = 0.0
            }
        }
        _liveLocations.value = initialLocs

        // Start real-time simulation engine
        startSimulationEngine()
    }

    // =========================================================================
    // AUTHENTICATION & ROLE SWITCHING
    // =========================================================================

    fun login(email: String, role: UserRole): Boolean {
        val existing = _allUsers.value.find { it.email.equals(email, ignoreCase = true) }
        val user = if (existing != null) {
            existing.copy(role = role)
        } else {
            val newUser = User(
                id = "user_${UUID.randomUUID().toString().take(8)}",
                name = email.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() },
                email = email,
                phone = "+91 98000 00000",
                role = role
            )
            _allUsers.value = _allUsers.value + newUser
            newUser
        }
        _currentUser.value = user
        return true
    }

    fun switchRole(role: UserRole) {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(role = role)
    }

    fun logout() {
        _currentUser.value = null
    }

    fun selectPresetUser(role: UserRole) {
        val preset = when (role) {
            UserRole.STUDENT -> DemoDataProvider.initialUsers[0]
            UserRole.DRIVER -> DemoDataProvider.initialUsers[1]
            UserRole.ADMIN -> DemoDataProvider.initialUsers[2]
        }
        _currentUser.value = preset
    }

    // =========================================================================
    // STUDENT ASSIGNMENT & DATA
    // =========================================================================

    fun getStudentProfile(userId: String): StudentProfile? {
        return _students.value.find { it.userId == userId }
            ?: _students.value.firstOrNull() // fallback to first student for demo
    }

    fun updateStudentAssignment(userId: String, busId: String, stopId: String) {
        _students.value = _students.value.map {
            if (it.userId == userId) it.copy(assignedBusId = busId, assignedStopId = stopId) else it
        }
    }

    // =========================================================================
    // DRIVER OPERATIONS (GPS, START TRIP, END TRIP, SOS)
    // =========================================================================

    fun getDriverProfile(userId: String): DriverProfile? {
        return _drivers.value.find { it.userId == userId }
            ?: _drivers.value.firstOrNull()
    }

    fun startDriverTrip(driverId: String, busId: String, routeId: String): Trip {
        val trip = Trip(
            tripId = "trip_${System.currentTimeMillis()}",
            busId = busId,
            driverId = driverId,
            routeId = routeId,
            startTime = System.currentTimeMillis(),
            status = TripStatus.ACTIVE
        )
        _driverActiveTrip.value = trip
        _trips.value = listOf(trip) + _trips.value

        // Update bus status to ON_ROUTE
        _buses.value = _buses.value.map {
            if (it.busId == busId) it.copy(status = BusStatus.ON_ROUTE) else it
        }

        // Send notification
        val bus = _buses.value.find { it.busId == busId }
        val route = _routes.value.find { it.routeId == routeId }
        addNotification(
            title = "Trip Started: ${bus?.busNumber ?: "Campus Bus"}",
            message = "Driver started morning transit trip on ${route?.routeName ?: "assigned route"}.",
            type = NotificationType.TRIP_STARTED,
            busId = busId
        )

        return trip
    }

    fun endDriverTrip(tripId: String) {
        val current = _driverActiveTrip.value
        if (current != null && current.tripId == tripId) {
            val ended = current.copy(
                endTime = System.currentTimeMillis(),
                status = TripStatus.COMPLETED
            )
            _driverActiveTrip.value = null
            _trips.value = _trips.value.map { if (it.tripId == tripId) ended else it }

            // Update bus status to TRIP_COMPLETED
            _buses.value = _buses.value.map {
                if (it.busId == current.busId) it.copy(status = BusStatus.TRIP_COMPLETED) else it
            }

            addNotification(
                title = "Trip Completed",
                message = "Bus trip has safely concluded. All scheduled stops reached.",
                type = NotificationType.TRIP_COMPLETED,
                busId = current.busId
            )
        }
    }

    fun updateDriverLiveLocation(
        busId: String,
        tripId: String,
        lat: Double,
        lng: Double,
        speed: Float,
        heading: Float
    ) {
        val loc = LiveLocation(
            busId = busId,
            tripId = tripId,
            latitude = lat,
            longitude = lng,
            speedKmH = speed,
            headingDegrees = heading,
            timestamp = System.currentTimeMillis(),
            isGpsLive = true
        )
        val updatedMap = _liveLocations.value.toMutableMap()
        updatedMap[busId] = loc
        _liveLocations.value = updatedMap

        checkProximityAlertsForBus(busId, lat, lng)
    }

    fun advanceDriverStop(tripId: String) {
        val active = _driverActiveTrip.value ?: return
        if (active.tripId == tripId) {
            val updated = active.copy(currentStopIndex = active.currentStopIndex + 1)
            _driverActiveTrip.value = updated
            _trips.value = _trips.value.map { if (it.tripId == tripId) updated else it }
        }
    }

    fun triggerEmergencyAlert(
        busId: String,
        driverId: String,
        driverName: String,
        busNumber: String,
        routeName: String,
        message: String,
        lat: Double,
        lng: Double
    ) {
        val alert = EmergencyAlert(
            alertId = "sos_${System.currentTimeMillis()}",
            busId = busId,
            driverId = driverId,
            driverName = driverName,
            busNumber = busNumber,
            routeName = routeName,
            message = message,
            latitude = lat,
            longitude = lng,
            timestamp = System.currentTimeMillis()
        )
        _emergencyAlerts.value = listOf(alert) + _emergencyAlerts.value

        // Update bus status to DELAYED
        _buses.value = _buses.value.map {
            if (it.busId == busId) it.copy(status = BusStatus.DELAYED) else it
        }

        // Broadcast high-priority notification
        addNotification(
            title = "⚠️ EMERGENCY ALERT - Bus $busNumber",
            message = "$message (Driver: $driverName). Campus Security & Transport Office notified.",
            type = NotificationType.EMERGENCY,
            busId = busId
        )
    }

    // =========================================================================
    // NOTIFICATIONS & PROXIMITY ENGINE
    // =========================================================================

    fun addNotification(
        title: String,
        message: String,
        type: NotificationType,
        busId: String? = null,
        userId: String = ""
    ) {
        val item = NotificationItem(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            userId = userId,
            title = title,
            message = message,
            type = type,
            timestamp = System.currentTimeMillis(),
            read = false,
            busId = busId
        )
        _notifications.value = listOf(item) + _notifications.value
    }

    fun markNotificationAsRead(notifId: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notifId) it.copy(read = true) else it
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    private fun checkProximityAlertsForBus(busId: String, busLat: Double, busLng: Double) {
        val bus = _buses.value.find { it.busId == busId } ?: return
        val route = _routes.value.find { it.routeId == bus.routeId } ?: return

        for (student in _students.value.filter { it.assignedBusId == busId }) {
            val stop = route.stops.find { it.stopId == student.assignedStopId }
            if (stop != null) {
                val distanceMeters = GeoUtils.calculateDistanceMeters(
                    busLat, busLng, stop.latitude, stop.longitude
                )
                if (distanceMeters <= 500 && distanceMeters > 50) {
                    // Update bus status if appropriate
                    if (bus.status != BusStatus.ARRIVING_SOON) {
                        _buses.value = _buses.value.map {
                            if (it.busId == busId) it.copy(status = BusStatus.ARRIVING_SOON) else it
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // ADMIN OPERATIONS (STUDENTS, DRIVERS, BUSES, ROUTES, STOPS, EMERGENCIES)
    // =========================================================================

    fun addStudent(name: String, email: String, phone: String, studentId: String, busId: String, stopId: String) {
        val newUserId = "student_${UUID.randomUUID().toString().take(6)}"
        val user = User(id = newUserId, name = name, email = email, phone = phone, role = UserRole.STUDENT)
        val profile = StudentProfile(
            userId = newUserId,
            studentId = studentId,
            assignedBusId = busId,
            assignedStopId = stopId
        )
        _allUsers.value = _allUsers.value + user
        _students.value = _students.value + profile
    }

    fun deleteStudent(userId: String) {
        _students.value = _students.value.filter { it.userId != userId }
        _allUsers.value = _allUsers.value.filter { it.id != userId }
    }

    fun addDriver(name: String, email: String, phone: String, driverId: String, busId: String, license: String) {
        val newUserId = "driver_${UUID.randomUUID().toString().take(6)}"
        val user = User(id = newUserId, name = name, email = email, phone = phone, role = UserRole.DRIVER)
        val profile = DriverProfile(
            userId = newUserId,
            driverId = driverId,
            assignedBusId = busId,
            licenseNumber = license
        )
        _allUsers.value = _allUsers.value + user
        _drivers.value = _drivers.value + profile
    }

    fun deleteDriver(userId: String) {
        _drivers.value = _drivers.value.filter { it.userId != userId }
        _allUsers.value = _allUsers.value.filter { it.id != userId }
    }

    fun addBus(busNumber: String, regNo: String, driverId: String, routeId: String, capacity: Int) {
        val newBus = Bus(
            busId = "bus_${UUID.randomUUID().toString().take(6)}",
            busNumber = busNumber,
            registrationNumber = regNo,
            driverId = driverId,
            routeId = routeId,
            capacity = capacity,
            status = BusStatus.ON_ROUTE,
            isDemoBus = true
        )
        _buses.value = _buses.value + newBus
    }

    fun updateBusStatus(busId: String, status: BusStatus) {
        _buses.value = _buses.value.map {
            if (it.busId == busId) it.copy(status = status) else it
        }
    }

    fun deleteBus(busId: String) {
        _buses.value = _buses.value.filter { it.busId != busId }
    }

    fun addRoute(name: String, description: String, stops: List<Stop>, colorHex: Long) {
        val newRoute = Route(
            routeId = "route_${UUID.randomUUID().toString().take(6)}",
            routeName = name,
            description = description,
            stops = stops,
            colorHex = colorHex
        )
        _routes.value = _routes.value + newRoute
    }

    fun updateRouteStops(routeId: String, updatedStops: List<Stop>) {
        _routes.value = _routes.value.map {
            if (it.routeId == routeId) it.copy(stops = updatedStops) else it
        }
    }

    fun deleteRoute(routeId: String) {
        _routes.value = _routes.value.filter { it.routeId != routeId }
    }

    fun resolveEmergencyAlert(alertId: String) {
        _emergencyAlerts.value = _emergencyAlerts.value.map {
            if (it.alertId == alertId) it.copy(isResolved = true) else it
        }
    }

    fun toggleDemoMode(enabled: Boolean) {
        _isDemoModeEnabled.value = enabled
    }

    fun setSimulationSpeed(speed: Float) {
        _simulationSpeedMultiplier.value = speed
    }

    fun resetAllData() {
        _buses.value = DemoDataProvider.initialBuses
        _routes.value = DemoDataProvider.initialRoutes
        _students.value = DemoDataProvider.initialStudents
        _drivers.value = DemoDataProvider.initialDrivers
        _notifications.value = DemoDataProvider.initialNotifications
        _emergencyAlerts.value = emptyList()
        _trips.value = emptyList()
    }

    // =========================================================================
    // REAL-TIME BUS SIMULATION ENGINE (Smooth multi-bus progression)
    // =========================================================================

    private fun startSimulationEngine() {
        simulationJob?.cancel()
        simulationJob = repositoryScope.launch {
            while (isActive) {
                if (_isDemoModeEnabled.value) {
                    val currentLocs = _liveLocations.value.toMutableMap()
                    val allRoutesMap = _routes.value.associateBy { it.routeId }
                    val currentSpeedMult = _simulationSpeedMultiplier.value

                    for (bus in _buses.value.filter { it.isDemoBus && it.status != BusStatus.TRIP_COMPLETED }) {
                        val route = allRoutesMap[bus.routeId] ?: continue
                        if (route.stops.size < 2) continue

                        val stops = route.stops.sortedBy { it.sequence }
                        val currentProg = busProgressMap[bus.busId] ?: 0.0

                        // Step progression (0.015 per tick * speed multiplier)
                        var nextProg = currentProg + (0.012 * currentSpeedMult)
                        if (nextProg >= (stops.size - 1)) {
                            nextProg = 0.0 // Loop back for continuous simulation
                        }
                        busProgressMap[bus.busId] = nextProg

                        val segmentIndex = nextProg.toInt().coerceIn(0, stops.size - 2)
                        val fraction = (nextProg - segmentIndex).coerceIn(0.0, 1.0)

                        val stopA = stops[segmentIndex]
                        val stopB = stops[segmentIndex + 1]

                        val (lat, lng) = GeoUtils.interpolatePoint(
                            stopA.latitude, stopA.longitude,
                            stopB.latitude, stopB.longitude,
                            fraction
                        )

                        val bearing = GeoUtils.calculateBearing(
                            stopA.latitude, stopA.longitude,
                            stopB.latitude, stopB.longitude
                        )

                        val speed = (28f + (10f * sin(nextProg * 3.14).toFloat())).coerceAtLeast(15f)

                        currentLocs[bus.busId] = LiveLocation(
                            busId = bus.busId,
                            latitude = lat,
                            longitude = lng,
                            speedKmH = speed,
                            headingDegrees = bearing,
                            timestamp = System.currentTimeMillis(),
                            isGpsLive = true
                        )

                        checkProximityAlertsForBus(bus.busId, lat, lng)
                    }

                    _liveLocations.value = currentLocs
                }
                delay(2000L)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: CampusTransitRepository? = null

        fun getInstance(): CampusTransitRepository {
            return instance ?: synchronized(this) {
                instance ?: CampusTransitRepository().also { instance = it }
            }
        }
    }
}
