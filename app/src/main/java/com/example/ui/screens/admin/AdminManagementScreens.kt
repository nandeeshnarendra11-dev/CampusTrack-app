package com.example.ui.screens.admin

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CampusTransitRepository
import com.example.model.*
import com.example.ui.components.BusStatusBadge
import com.example.ui.components.TransitMapCanvas
import java.text.SimpleDateFormat
import java.util.*

// =============================================================================
// 1. LIVE FLEET MAP SCREEN
// =============================================================================

@Composable
fun AdminFleetMapScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    val liveLocations by repository.liveLocations.collectAsState()

    var selectedBusId by remember { mutableStateOf<String?>(null) }
    val selectedBus = buses.find { it.busId == selectedBusId }
    val selectedRoute = routes.find { it.routeId == selectedBus?.routeId }

    val allBusesWithLocations = remember(buses, liveLocations) {
        buses.mapNotNull { bus ->
            val loc = liveLocations[bus.busId]
            if (loc != null) Pair(bus, loc) else null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        TransitMapCanvas(
            route = selectedRoute ?: routes.firstOrNull(),
            bus = selectedBus,
            liveLocation = selectedBus?.let { liveLocations[it.busId] },
            studentStop = null,
            allBuses = allBusesWithLocations,
            modifier = Modifier.fillMaxSize(),
            isInteractive = true
        )

        // Top Horizontal Bus Selector Chips
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0F172A).copy(alpha = 0.95f),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Live Fleet Movement (${allBusesWithLocations.size} Tracking)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedBusId == null,
                        onClick = { selectedBusId = null },
                        label = { Text("All Buses") },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4F46E5),
                            selectedLabelColor = Color.White
                        )
                    )
                    buses.forEach { bus ->
                        val isSel = selectedBusId == bus.busId
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedBusId = if (isSel) null else bus.busId },
                            label = { Text(bus.busNumber) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF10B981),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// 2. STUDENTS MANAGEMENT SCREEN
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentsScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val students by repository.students.collectAsState()
    val allUsers by repository.allUsers.collectAsState()
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newStudentId by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("+91 ") }
    var newBusId by remember { mutableStateOf(buses.firstOrNull()?.busId ?: "") }
    var newStopId by remember { mutableStateOf("") }

    val filteredStudents = remember(students, allUsers, searchQuery) {
        students.filter { student ->
            val user = allUsers.find { it.id == student.userId }
            val name = user?.name ?: ""
            name.contains(searchQuery, ignoreCase = true) ||
                    student.studentId.contains(searchQuery, ignoreCase = true)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Students Management",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${students.size} enrolled transport students",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    modifier = Modifier.testTag("admin_add_student_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Student")
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by student name or roll ID") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )
        }

        items(filteredStudents) { student ->
            val user = allUsers.find { it.id == student.userId }
            val bus = buses.find { it.busId == student.assignedBusId }
            val route = routes.find { it.routeId == bus?.routeId }
            val stop = route?.stops?.find { it.stopId == student.assignedStopId }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.name?.take(1) ?: "S",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0284C7)
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = user?.name ?: "Student",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Roll: ${student.studentId} • ${student.department}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "🚌 Bus: ${bus?.busNumber ?: "KA-04"} | 📍 Stop: ${stop?.name ?: "Main Road"}",
                            fontSize = 11.sp,
                            color = Color(0xFF4F46E5),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { repository.deleteStudent(student.userId) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Student & Assign Transport") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("College Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newStudentId, onValueChange = { newStudentId = it }, label = { Text("Student Roll ID (e.g. CS2024-001)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                            val defaultBus = buses.firstOrNull()?.busId ?: "bus_101"
                            val defaultStop = routes.firstOrNull()?.stops?.firstOrNull()?.stopId ?: "stop_101"
                            repository.addStudent(newName, newEmail, newPhone, newStudentId, defaultBus, defaultStop)
                            showAddDialog = false
                            newName = ""
                            newEmail = ""
                            newStudentId = ""
                        }
                    }
                ) {
                    Text("Add & Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// =============================================================================
// 3. DRIVERS MANAGEMENT SCREEN
// =============================================================================

@Composable
fun AdminDriversScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val drivers by repository.drivers.collectAsState()
    val allUsers by repository.allUsers.collectAsState()
    val buses by repository.buses.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newDriverId by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("+91 ") }
    var newLicense by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Drivers Roster",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${drivers.size} licensed transport drivers",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Driver")
                }
            }
        }

        items(drivers) { driver ->
            val user = allUsers.find { it.id == driver.userId }
            val bus = buses.find { it.busId == driver.assignedBusId }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFF059669).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AirlineSeatReclineNormal, contentDescription = null, tint = Color(0xFF059669))
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = user?.name ?: "Driver",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Driver ID: ${driver.driverId} • Rating: ★ ${driver.rating}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "🚌 Bus: ${bus?.busNumber ?: "Unassigned"} | License: ${driver.licenseNumber}",
                            fontSize = 11.sp,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    IconButton(
                        onClick = { repository.deleteDriver(driver.userId) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Transit Driver") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Driver Full Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newDriverId, onValueChange = { newDriverId = it }, label = { Text("Driver ID (e.g. DRV-104)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newLicense, onValueChange = { newLicense = it }, label = { Text("Commercial License No.") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotEmpty() && newEmail.isNotEmpty()) {
                            val defaultBus = buses.firstOrNull()?.busId ?: "bus_101"
                            repository.addDriver(newName, newEmail, newPhone, newDriverId, defaultBus, newLicense)
                            showAddDialog = false
                            newName = ""
                            newEmail = ""
                        }
                    }
                ) { Text("Save Driver") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

// =============================================================================
// 4. BUSES MANAGEMENT SCREEN
// =============================================================================

@Composable
fun AdminBusesScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val buses by repository.buses.collectAsState()
    val routes by repository.routes.collectAsState()
    val drivers by repository.drivers.collectAsState()
    val allUsers by repository.allUsers.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newBusNumber by remember { mutableStateOf("") }
    var newRegNo by remember { mutableStateOf("") }
    var newCapacity by remember { mutableStateOf("48") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bus Fleet Management",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${buses.size} campus transport vehicles",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Bus")
                }
            }
        }

        items(buses) { bus ->
            val route = routes.find { it.routeId == bus.routeId }
            val driver = drivers.find { it.driverId == bus.driverId }
            val driverUser = allUsers.find { it.id == driver?.userId }

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
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = bus.busNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (bus.isDemoBus) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEDE9FE)) {
                                        Text("DEMO", color = Color(0xFF6D28D9), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(
                                text = bus.modelName,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        BusStatusBadge(status = bus.status)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Route", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(route?.routeName?.substringAfter(":") ?: "Campus Route", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Capacity", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${bus.occupancy}/${bus.capacity} Seats", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("Driver", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(driverUser?.name ?: "Rajesh K", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Status Changer Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BusStatus.values().forEach { statusOption ->
                            val isSel = bus.status == statusOption
                            AssistChip(
                                onClick = { repository.updateBusStatus(bus.busId, statusOption) },
                                label = { Text(statusOption.label, fontSize = 10.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (isSel) Color(0xFF312E81) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    labelColor = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Fleet Bus") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newBusNumber, onValueChange = { newBusNumber = it }, label = { Text("Bus Number (e.g. KA-02-B-3344)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newRegNo, onValueChange = { newRegNo = it }, label = { Text("Registration Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newCapacity, onValueChange = { newCapacity = it }, label = { Text("Seating Capacity") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBusNumber.isNotEmpty()) {
                            val defaultRoute = routes.firstOrNull()?.routeId ?: "route_1"
                            val defaultDriver = drivers.firstOrNull()?.driverId ?: "driver_1"
                            repository.addBus(newBusNumber, newRegNo, defaultDriver, defaultRoute, newCapacity.toIntOrNull() ?: 45)
                            showAddDialog = false
                            newBusNumber = ""
                        }
                    }
                ) { Text("Save Bus") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }
}

// =============================================================================
// 5. ROUTES & STOPS SCREEN
// =============================================================================

@Composable
fun AdminRoutesScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val routes by repository.routes.collectAsState()

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
                    text = "Routes & Stops",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${routes.size} active campus transit loops",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(routes) { route ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
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
                            text = route.routeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(route.colorHex).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${route.stops.size} Stops",
                                color = Color(route.colorHex),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = route.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text("Waypoint Sequence:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    route.stops.sortedBy { it.sequence }.forEach { stop ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stop.sequence}. ${stop.name}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stop.morningPickupTime,
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

// =============================================================================
// 6. EMERGENCY SOS ALERTS SCREEN
// =============================================================================

@Composable
fun AdminEmergenciesScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val emergencyAlerts by repository.emergencyAlerts.collectAsState()
    val dateFormat = remember { SimpleDateFormat("hh:mm a • MMM dd", Locale.getDefault()) }

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
                    text = "🚨 Emergency & SOS Dispatch",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Real-time driver SOS triggers and roadside distress tickets",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (emergencyAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(44.dp))
                        Text("No Active Emergency Alerts", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("All campus transit fleets are operating safely.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(emergencyAlerts) { alert ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alert.isResolved) MaterialTheme.colorScheme.surface else Color(0xFFFEF2F2)
                    ),
                    border = if (!alert.isResolved) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFDC2626)) else null
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
                                text = "⚠️ SOS: Bus ${alert.busNumber}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (alert.isResolved) MaterialTheme.colorScheme.onSurface else Color(0xFFB91C1C)
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (alert.isResolved) Color(0xFFECFDF5) else Color(0xFFDC2626)
                            ) {
                                Text(
                                    text = if (alert.isResolved) "RESOLVED" else "ACTIVE SOS",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = alert.message,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Driver: ${alert.driverName} | Route: ${alert.routeName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Time: ${dateFormat.format(Date(alert.timestamp))}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!alert.isResolved) {
                            Button(
                                onClick = { repository.resolveEmergencyAlert(alert.alertId) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Alert as Resolved", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 7. BROADCAST NOTIFICATIONS SCREEN
// =============================================================================

@Composable
fun AdminNotificationsScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var notifType by remember { mutableStateOf(NotificationType.ROUTE_CHANGE) }
    var broadcastSentBanner by remember { mutableStateOf(false) }

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
                    text = "Broadcast Notifications",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Send instant transit notices to all student & driver devices",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Broadcast Message Body") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Notification Category:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NotificationType.values().take(4).forEach { type ->
                            val isSel = notifType == type
                            FilterChip(
                                selected = isSel,
                                onClick = { notifType = type },
                                label = { Text(type.name.replace("_", " "), fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4F46E5),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && message.isNotEmpty()) {
                                repository.addNotification(
                                    title = title,
                                    message = message,
                                    type = notifType
                                )
                                broadcastSentBanner = true
                                title = ""
                                message = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Broadcast to All Users", fontWeight = FontWeight.Bold)
                    }

                    if (broadcastSentBanner) {
                        Text("✓ Notification successfully broadcasted!", color = Color(0xFF059669), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// =============================================================================
// 8. SIMULATION & SETTINGS SCREEN
// =============================================================================

@Composable
fun AdminSettingsScreen(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val isDemoEnabled by repository.isDemoModeEnabled.collectAsState()
    val speedMultiplier by repository.simulationSpeedMultiplier.collectAsState()

    var showResetConfirm by remember { mutableStateOf(false) }

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
                    text = "Transit Controls & Simulator",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configure demo simulation speed, telemetry and data state",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Demo Mode Toggle Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Demo Bus Simulator", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "Simulates 3 college buses moving continuously along routes with live GPS coordinates.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isDemoEnabled,
                            onCheckedChange = { repository.toggleDemoMode(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Speed Multiplier
                    Text("Simulation Speed Multiplier: ${speedMultiplier}x", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1.0f, 2.0f, 5.0f).forEach { speed ->
                            val isSel = speedMultiplier == speed
                            FilterChip(
                                selected = isSel,
                                onClick = { repository.setSimulationSpeed(speed) },
                                label = { Text("${speed.toInt()}x Speed") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF4F46E5),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // Reset Data Button
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Database & State", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Reset all live locations, student assignments, and demo data to default state.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Button(
                        onClick = { showResetConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset All Demo Data", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset Demo Data?") },
            text = { Text("This will restore default routes, demo buses, and demo accounts.") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.resetAllData()
                        showResetConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") } }
        )
    }
}
