package com.example.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CampusTransitRepository
import com.example.data.GeoUtils
import com.example.model.BusStatus
import com.example.ui.components.BusStatusBadge
import com.example.ui.components.TransitMapCanvas

@Composable
fun StudentLiveMapScreen(
    repository: CampusTransitRepository,
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

    val distanceMeters = remember(liveLoc, studentStop) {
        if (liveLoc != null && studentStop != null) {
            GeoUtils.calculateDistanceMeters(
                liveLoc.latitude, liveLoc.longitude,
                studentStop.latitude, studentStop.longitude
            )
        } else {
            3800.0
        }
    }

    val distanceText = remember(distanceMeters) { GeoUtils.formatDistance(distanceMeters) }
    val etaMinutes = remember(distanceMeters, liveLoc) {
        GeoUtils.calculateEtaMinutes(distanceMeters, liveLoc?.speedKmH ?: 28f)
    }

    val nextStop = remember(assignedRoute, liveLoc) {
        assignedRoute?.stops?.sortedBy { it.sequence }?.firstOrNull { stop ->
            if (liveLoc == null) true
            else GeoUtils.calculateDistanceMeters(liveLoc.latitude, liveLoc.longitude, stop.latitude, stop.longitude) > 200
        } ?: assignedRoute?.stops?.lastOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        // Full Canvas Map
        TransitMapCanvas(
            route = assignedRoute,
            bus = assignedBus,
            liveLocation = liveLoc,
            studentStop = studentStop,
            modifier = Modifier.fillMaxSize(),
            isInteractive = true
        )

        // Top Floating Telemetry Strip
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White.copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = assignedBus?.busNumber ?: "KA-04-E-1829",
                            color = Color(0xFF1D1B20),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (assignedBus?.isDemoBus == true) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Text(
                                    text = "DEMO",
                                    color = Color(0xFF78350F),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Next: ${nextStop?.name ?: "Campus Gateway"}",
                        color = Color(0xFF49454F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                BusStatusBadge(status = assignedBus?.status ?: BusStatus.ON_ROUTE)
            }
        }

        // Bottom Floating ETA & Stop Info Card
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
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
                    Column {
                        Text(
                            text = "ESTIMATED ARRIVAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$etaMinutes min",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0B57D0)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DISTANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = distanceText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF007A5A)
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE1E3E8))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Speed,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Live Speed: ${(liveLoc?.speedKmH ?: 32f).toInt()} km/h",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1D1B20)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFF0B57D0),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = studentStop?.name ?: "Indiranagar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B57D0)
                    )
                }
            }
        }
    }
}
