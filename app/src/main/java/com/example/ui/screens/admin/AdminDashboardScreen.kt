package com.example.ui.screens.admin

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CampusTransitRepository
import com.example.model.*
import com.example.ui.components.BusStatusBadge
import com.example.ui.components.TransitMapCanvas

@Composable
fun AdminDashboardScreen(
    repository: CampusTransitRepository,
    onNavigateToFleetMap: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToDrivers: () -> Unit,
    onNavigateToBuses: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    onNavigateToEmergencies: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    val students by repository.students.collectAsState()
    val drivers by repository.drivers.collectAsState()
    val emergencyAlerts by repository.emergencyAlerts.collectAsState()
    val liveLocations by repository.liveLocations.collectAsState()

    val activeBuses = buses.filter { it.status == BusStatus.ON_ROUTE || it.status == BusStatus.ARRIVING_SOON }
    val unresolvedEmergencies = emergencyAlerts.filter { !it.isResolved }

    val allBusesWithLocations = remember(buses, liveLocations) {
        buses.mapNotNull { bus ->
            val loc = liveLocations[bus.busId]
            if (loc != null) Pair(bus, loc) else null
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "COMMAND CENTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF49454F),
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "Transport Admin Command",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
                Text(
                    text = "College fleet monitoring, resource management & live telemetry",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        // Active Emergency Alert Banner if any
        if (unresolvedEmergencies.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToEmergencies() }
                        .testTag("admin_emergency_banner"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E6)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF8B4B4))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFB3261E), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚠️ ${unresolvedEmergencies.size} ACTIVE EMERGENCY SOS ALERT!",
                                color = Color(0xFF601410),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${unresolvedEmergencies.first().busNumber}: ${unresolvedEmergencies.first().message}",
                                color = Color(0xFFB3261E),
                                fontSize = 12.sp
                            )
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB3261E))
                    }
                }
            }
        }

        // Metrics Grid (4 Stat Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        title = "Active Fleet",
                        value = "${activeBuses.size}/${buses.size}",
                        subtitle = "Buses on Route",
                        icon = Icons.Default.DirectionsBus,
                        color = Color(0xFF0B57D0),
                        modifier = Modifier.weight(1f).clickable { onNavigateToBuses() }
                    )

                    AdminMetricCard(
                        title = "Active Drivers",
                        value = "${drivers.size}",
                        subtitle = "Licensed Drivers",
                        icon = Icons.Default.AirlineSeatReclineNormal,
                        color = Color(0xFF007A5A),
                        modifier = Modifier.weight(1f).clickable { onNavigateToDrivers() }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        title = "Registered Students",
                        value = "${students.size}",
                        subtitle = "Enrolled Transport",
                        icon = Icons.Default.School,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f).clickable { onNavigateToStudents() }
                    )

                    AdminMetricCard(
                        title = "Active Routes",
                        value = "${routes.size}",
                        subtitle = "Covering 16+ stops",
                        icon = Icons.Default.AltRoute,
                        color = Color(0xFFB45309),
                        modifier = Modifier.weight(1f).clickable { onNavigateToRoutes() }
                    )
                }
            }
        }

        // Live Fleet Map Preview Card
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
                            text = "Live Fleet Movement",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1D1B20)
                        )
                        TextButton(onClick = onNavigateToFleetMap) {
                            Text("Full Fleet Map", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B57D0))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF0B57D0), modifier = Modifier.size(13.dp).padding(start = 2.dp))
                        }
                    }

                    TransitMapCanvas(
                        route = routes.firstOrNull(),
                        bus = null,
                        liveLocation = null,
                        studentStop = null,
                        allBuses = allBusesWithLocations,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    )
                }
            }
        }

        // Quick Navigation Tiles
        item {
            Text(
                text = "MANAGEMENT MODULES",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF49454F),
                letterSpacing = 0.5.sp
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminNavRow("Students Directory", "${students.size} enrolled students", Icons.Default.School, Color(0xFF0284C7), onNavigateToStudents)
                AdminNavRow("Drivers Roster", "${drivers.size} active drivers", Icons.Default.AirlineSeatReclineNormal, Color(0xFF007A5A), onNavigateToDrivers)
                AdminNavRow("Bus Fleet Roster", "${buses.size} total buses", Icons.Default.DirectionsBus, Color(0xFF0B57D0), onNavigateToBuses)
                AdminNavRow("Routes & Stops", "${routes.size} transit routes", Icons.Default.AltRoute, Color(0xFFB45309), onNavigateToRoutes)
                AdminNavRow("Broadcast Notifications", "Send instant campus alerts", Icons.Default.Campaign, Color(0xFF6750A4), onNavigateToNotifications)
                AdminNavRow("Emergency Center", "${unresolvedEmergencies.size} unresolved alerts", Icons.Default.Warning, Color(0xFFB3261E), onNavigateToEmergencies)
                AdminNavRow("Simulation & Settings", "Demo mode & speed controls", Icons.Default.Settings, Color(0xFF49454F), onNavigateToSettings)
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = Color(0xFF49454F), fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1D1B20))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF49454F))
        }
    }
}

@Composable
private fun AdminNavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(iconColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1D1B20))
                Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF49454F))
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF79747E))
        }
    }
}
