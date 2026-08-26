package com.example.ui.screens.driver

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CampusTransitRepository
import com.example.model.*
import com.example.ui.components.BusStatusBadge
import com.example.ui.components.TransitMapCanvas
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DriverDashboardScreen(
    repository: CampusTransitRepository,
    onNavigateToRouteMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val allBuses by repository.buses.collectAsState()
    val allRoutes by repository.routes.collectAsState()
    val allDrivers by repository.drivers.collectAsState()
    val liveLocations by repository.liveLocations.collectAsState()
    val activeTrip by repository.driverActiveTrip.collectAsState()

    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val driverProfile = remember(currentUser, allDrivers) {
        currentUser?.let { repository.getDriverProfile(it.id) }
    }

    val assignedBus = remember(driverProfile, allBuses) {
        allBuses.find { it.busId == driverProfile?.assignedBusId } ?: allBuses.firstOrNull()
    }

    val assignedRoute = remember(assignedBus, allRoutes) {
        allRoutes.find { it.routeId == assignedBus?.routeId } ?: allRoutes.firstOrNull()
    }

    val liveLoc = remember(assignedBus, liveLocations) {
        assignedBus?.let { liveLocations[it.busId] }
    }

    val stops = remember(assignedRoute) { assignedRoute?.stops?.sortedBy { it.sequence } ?: emptyList() }
    val currentStopIndex = activeTrip?.currentStopIndex ?: 0
    val currentStop = stops.getOrNull(currentStopIndex) ?: stops.firstOrNull()
    val nextStop = stops.getOrNull(currentStopIndex + 1)

    // Elapsed trip timer
    var tripElapsedSeconds by remember { mutableLongStateOf(0L) }
    LaunchedEffect(activeTrip) {
        if (activeTrip != null) {
            while (true) {
                tripElapsedSeconds = (System.currentTimeMillis() - (activeTrip?.startTime ?: System.currentTimeMillis())) / 1000
                delay(1000L)
            }
        } else {
            tripElapsedSeconds = 0L
        }
    }

    val formattedDuration = remember(tripElapsedSeconds) {
        val hrs = tripElapsedSeconds / 3600
        val mins = (tripElapsedSeconds % 3600) / 60
        val secs = tripElapsedSeconds % 60
        String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }

    var showSosDialog by remember { mutableStateOf(false) }
    var sosMessageText by remember { mutableStateOf("Breakdown / Heavy traffic on transit route") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Driver & Bus Information Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("driver_cockpit_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFFEF3C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AirlineSeatReclineNormal,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "DRIVER COCKPIT",
                                color = Color(0xFF49454F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }
                        BusStatusBadge(
                            status = if (activeTrip != null) BusStatus.ON_ROUTE else (assignedBus?.status ?: BusStatus.TRIP_COMPLETED)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = assignedBus?.busNumber ?: "KA-04-E-1829",
                                color = Color(0xFF1D1B20),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.3.sp
                            )
                            Text(
                                text = assignedRoute?.routeName ?: "Route 1: Central Metro → Campus",
                                color = Color(0xFF49454F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currentUser?.name ?: "Rajesh Kumar",
                                color = Color(0xFF1D1B20),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ID: ${driverProfile?.driverId ?: "DRV-101"}",
                                color = Color(0xFF0B57D0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Primary Trip Action: START TRIP / END TRIP
        item {
            if (activeTrip == null) {
                Button(
                    onClick = {
                        if (!locationPermissionState.status.isGranted) {
                            locationPermissionState.launchPermissionRequest()
                        }
                        if (assignedBus != null && assignedRoute != null && driverProfile != null) {
                            repository.startDriverTrip(
                                driverId = driverProfile.driverId,
                                busId = assignedBus.busId,
                                routeId = assignedRoute.routeId
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_trip_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007A5A),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("START TRIP & BROADCAST GPS", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                }
            } else {
                Button(
                    onClick = {
                        repository.endDriverTrip(activeTrip!!.tripId)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("end_trip_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB3261E),
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("END TRIP & STOP GPS", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp)
                }
            }
        }

        // 3. Live Tracking Telemetry Banner
        item {
            AnimatedVisibility(visible = activeTrip != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_live_telemetry_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA3E7B7))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Color(0xFF007A5A), CircleShape)
                                )
                                Text(
                                    text = "LIVE TRACKING ACTIVE",
                                    color = Color(0xFF072711),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "⏱️ $formattedDuration",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1D1B20)
                            )
                        }

                        HorizontalDivider(color = Color(0xFFE1E3E8))

                        // GPS Coordinates & Speed Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Current GPS Coordinates", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                                Text(
                                    text = String.format(Locale.US, "%.5f° N, %.5f° E", liveLoc?.latitude ?: 12.9716, liveLoc?.longitude ?: 77.5946),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF1D1B20)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Vehicle Speed", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${(liveLoc?.speedKmH ?: 32f).toInt()} km/h",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF007A5A)
                                )
                            }
                        }

                        // Current Stop & Next Stop
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Current Stop", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                                Text(
                                    text = currentStop?.name ?: "Central Metro Hub",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0B57D0)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Next Scheduled Stop", fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.Medium)
                                Text(
                                    text = nextStop?.name ?: "Campus Terminal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }

                        // Mark Stop Reached CTA
                        Button(
                            onClick = {
                                activeTrip?.let { repository.advanceDriverStop(it.tripId) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("mark_stop_reached_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F3F4),
                                contentColor = Color(0xFF1D1B20)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF007A5A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Stop as Reached (${currentStopIndex + 1}/${stops.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
                        }
                    }
                }
            }
        }

        // 4. Live Driver Mini-Map Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Route Navigation Map",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1D1B20)
                        )
                        TextButton(onClick = onNavigateToRouteMap) {
                            Text("Full Map", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B57D0))
                        }
                    }

                    TransitMapCanvas(
                        route = assignedRoute,
                        bus = assignedBus,
                        liveLocation = liveLoc,
                        studentStop = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }
        }

        // 5. Emergency SOS Trigger Button
        item {
            Button(
                onClick = { showSosDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("driver_sos_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCE8E6)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8B4B4))
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB3261E), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TRIGGER EMERGENCY / SOS ALERT",
                    color = Color(0xFFB3261E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }

    // Emergency SOS Confirmation Dialog
    if (showSosDialog) {
        AlertDialog(
            onDismissRequest = { showSosDialog = false },
            title = { Text("🚨 Trigger Emergency Broadcast") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("This will immediately broadcast an urgent emergency alert to Transport Admin and all students on this route.")
                    OutlinedTextField(
                        value = sosMessageText,
                        onValueChange = { sosMessageText = it },
                        label = { Text("Emergency Situation") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (assignedBus != null && assignedRoute != null && driverProfile != null) {
                            repository.triggerEmergencyAlert(
                                busId = assignedBus.busId,
                                driverId = driverProfile.driverId,
                                driverName = currentUser?.name ?: "Driver",
                                busNumber = assignedBus.busNumber,
                                routeName = assignedRoute.routeName,
                                message = sosMessageText,
                                lat = liveLoc?.latitude ?: 12.9716,
                                lng = liveLoc?.longitude ?: 77.5946
                            )
                        }
                        showSosDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Broadcast SOS Alert", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSosDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
