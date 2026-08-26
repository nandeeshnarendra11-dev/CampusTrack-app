package com.example.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.GeoUtils
import com.example.model.*
import com.example.ui.components.BusStatusBadge
import com.example.ui.components.LiveEtaMetricsGrid
import com.example.ui.components.ProximityAlertCard
import com.example.ui.components.TransitMapCanvas

@Composable
fun StudentHomeScreen(
    repository: CampusTransitRepository,
    onNavigateToLiveMap: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val allBuses by repository.buses.collectAsState()
    val allRoutes by repository.routes.collectAsState()
    val allStudents by repository.students.collectAsState()
    val liveLocations by repository.liveLocations.collectAsState()

    val studentProfile = remember(currentUser, allStudents) {
        currentUser?.let { repository.getStudentProfile(it.id) }
    }

    val assignedBus = remember(studentProfile, allBuses) {
        allBuses.find { it.busId == studentProfile?.assignedBusId } ?: allBuses.firstOrNull()
    }

    val assignedRoute = remember(assignedBus, allRoutes) {
        allRoutes.find { it.routeId == assignedBus?.routeId } ?: allRoutes.firstOrNull()
    }

    val studentStop = remember(assignedRoute, studentProfile) {
        assignedRoute?.stops?.find { it.stopId == studentProfile?.assignedStopId }
            ?: assignedRoute?.stops?.firstOrNull()
    }

    val liveLoc = remember(assignedBus, liveLocations) {
        assignedBus?.let { liveLocations[it.busId] }
    }

    // Distance & ETA Calculations
    val distanceMeters = remember(liveLoc, studentStop) {
        if (liveLoc != null && studentStop != null) {
            GeoUtils.calculateDistanceMeters(
                liveLoc.latitude, liveLoc.longitude,
                studentStop.latitude, studentStop.longitude
            )
        } else {
            4200.0
        }
    }

    val distanceText = remember(distanceMeters) { GeoUtils.formatDistance(distanceMeters) }
    val etaMinutes = remember(distanceMeters, liveLoc) {
        GeoUtils.calculateEtaMinutes(distanceMeters, liveLoc?.speedKmH ?: 28f)
    }

    val isWithinProximity = remember(distanceMeters) {
        distanceMeters <= 600 && distanceMeters > 30
    }

    val nextStop = remember(assignedRoute, liveLoc) {
        assignedRoute?.stops?.sortedBy { it.sequence }?.firstOrNull { stop ->
            if (liveLoc == null) true
            else GeoUtils.calculateDistanceMeters(liveLoc.latitude, liveLoc.longitude, stop.latitude, stop.longitude) > 200
        } ?: assignedRoute?.stops?.lastOrNull()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // 1. Greeting Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "GOOD MORNING,",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF49454F),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = currentUser?.name ?: "Student",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = "College Transit is running on normal schedule today.",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        // 2. Proximity Arrival Alert Banner (<500m)
        item {
            ProximityAlertCard(
                isNear = isWithinProximity,
                stopName = studentStop?.name ?: "Your Assigned Stop"
            )
        }

        // 3. [🚌 My Bus] Summary Card - Sleek White Surface
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_my_bus_card"),
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
                                    .background(Color(0xFFE8F0FE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsBus,
                                    contentDescription = null,
                                    tint = Color(0xFF0B57D0),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "ASSIGNED TRANSIT",
                                color = Color(0xFF49454F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                        }
                        if (assignedBus?.isDemoBus == true) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Text(
                                    text = "DEMO BUS",
                                    color = Color(0xFF78350F),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
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
                                text = "Route: ${assignedRoute?.routeName?.substringAfter(":") ?: "College → City"}",
                                color = Color(0xFF49454F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        BusStatusBadge(status = assignedBus?.status ?: BusStatus.ON_ROUTE)
                    }

                    // Assigned Stop Pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFD),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF0B57D0),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Your Pickup Stop: ",
                                color = Color(0xFF49454F),
                                fontSize = 12.sp
                            )
                            Text(
                                text = studentStop?.name ?: "Indiranagar 100ft Rd",
                                color = Color(0xFF1D1B20),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. [📍 LIVE TRACK BUS] Interactive Map Preview
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE TRACK BUS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 0.5.sp
                    )
                    TextButton(
                        onClick = onNavigateToLiveMap,
                        modifier = Modifier.testTag("expand_map_button")
                    ) {
                        Text("Full Screen Map", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B57D0))
                        Icon(Icons.Default.OpenInFull, contentDescription = null, tint = Color(0xFF0B57D0), modifier = Modifier.size(13.dp).padding(start = 4.dp))
                    }
                }

                TransitMapCanvas(
                    route = assignedRoute,
                    bus = assignedBus,
                    liveLocation = liveLoc,
                    studentStop = studentStop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }
        }

        // 5. Live Telemetry Metrics Card (Estimated Arrival, Distance, Next Stop, Bus Status)
        item {
            LiveEtaMetricsGrid(
                etaMinutes = etaMinutes,
                distanceKmText = distanceText,
                nextStopName = nextStop?.name ?: "Main Road",
                busStatus = assignedBus?.status ?: BusStatus.ON_ROUTE
            )
        }

        // 6. Prominent [Track My Bus] Action Button
        item {
            Button(
                onClick = onNavigateToLiveMap,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("track_my_bus_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0B57D0),
                    contentColor = Color.White
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White)
                    Text(
                        text = "Track My Bus Live",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 7. Route Stops Timeline
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Route Stops & Waypoints",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1D1B20)
                        )
                        TextButton(onClick = onNavigateToSchedule) {
                            Text("View Schedule", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B57D0))
                        }
                    }

                    assignedRoute?.stops?.sortedBy { it.sequence }?.forEachIndexed { index, stop ->
                        val isStudentStop = stop.stopId == studentStop?.stopId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (isStudentStop) Color(0xFF0B57D0) else Color(0xFFF1F3F4),
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (isStudentStop) Color(0xFF0B57D0) else Color(0xFFE1E3E8),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${stop.sequence}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isStudentStop) Color.White else Color(0xFF49454F)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stop.name + if (isStudentStop) " (Your Stop)" else "",
                                    fontWeight = if (isStudentStop) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = if (isStudentStop) Color(0xFF0B57D0) else Color(0xFF1D1B20)
                                )
                                Text(
                                    text = "ETA: ${stop.morningPickupTime} • ${stop.landmark}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF49454F)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
