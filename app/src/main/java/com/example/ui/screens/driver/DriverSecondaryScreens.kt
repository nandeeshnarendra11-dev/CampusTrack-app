package com.example.ui.screens.driver

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
import com.example.model.Trip
import com.example.model.TripStatus
import com.example.ui.components.BusStatusBadge
import com.example.ui.components.TransitMapCanvas
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DriverRouteMapScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val allBuses by repository.buses.collectAsState()
    val allRoutes by repository.routes.collectAsState()
    val allDrivers by repository.drivers.collectAsState()
    val liveLocations by repository.liveLocations.collectAsState()

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        TransitMapCanvas(
            route = assignedRoute,
            bus = assignedBus,
            liveLocation = liveLoc,
            studentStop = null,
            modifier = Modifier.fillMaxSize(),
            isInteractive = true
        )

        // Top Route Info Pill
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = assignedRoute?.routeName ?: "Assigned Route",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${assignedRoute?.stops?.size ?: 0} Waypoint Stops • ${assignedRoute?.totalDistanceKm ?: 0.0} km",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF4F46E5)
                ) {
                    Text(
                        text = assignedBus?.busNumber ?: "KA-04",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DriverTripHistoryScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val trips by repository.trips.collectAsState()
    val allBuses by repository.buses.collectAsState()
    val allRoutes by repository.routes.collectAsState()

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Trip History & Logs",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Log of completed transit journeys and passenger runs",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (trips.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No Recent Trips Recorded",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Trips started in Driver Dashboard will appear here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(trips) { trip ->
                val bus = allBuses.find { it.busId == trip.busId }
                val route = allRoutes.find { it.routeId == trip.routeId }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bus?.busNumber ?: "Bus",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (trip.status == TripStatus.ACTIVE) Color(0xFFECFDF5) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = trip.status.name,
                                    color = if (trip.status == TripStatus.ACTIVE) Color(0xFF059669) else Color(0xFF475569),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = route?.routeName ?: "Transit Route",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Start: ${dateFormat.format(Date(trip.startTime))}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (trip.endTime != null) {
                                Text(
                                    text = "Completed",
                                    fontSize = 11.sp,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverProfileScreen(
    repository: CampusTransitRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val allBuses by repository.buses.collectAsState()
    val allDrivers by repository.drivers.collectAsState()

    val driverProfile = remember(currentUser, allDrivers) {
        currentUser?.let { repository.getDriverProfile(it.id) }
    }

    val assignedBus = remember(driverProfile, allBuses) {
        allBuses.find { it.busId == driverProfile?.assignedBusId } ?: allBuses.firstOrNull()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Driver Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Transportation duty details & assigned vehicle",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFFD97706), CircleShape)
                                .border(2.dp, Color(0xFFFBBF24), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AirlineSeatReclineNormal, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        Column {
                            Text(
                                text = currentUser?.name ?: "Rajesh Kumar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Driver ID: ${driverProfile?.driverId ?: "DRV-101"}",
                                fontSize = 13.sp,
                                color = Color(0xFFFBBF24)
                            )
                            Text(
                                text = "★ ${driverProfile?.rating ?: 4.9f} Rating • ${driverProfile?.experienceYears ?: 8} yrs Experience",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF312E81))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Assigned Vehicle", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("${assignedBus?.busNumber} (${assignedBus?.modelName})", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Commercial Heavy Vehicle License", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text(driverProfile?.licenseNumber ?: "KA-04-2015-0038291", fontSize = 13.sp, color = Color.White)
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("driver_logout_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }
}
