package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.model.Bus
import com.example.model.Route
import com.example.model.Stop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    repository: CampusTransitRepository,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val allBuses by repository.buses.collectAsState()
    val allRoutes by repository.routes.collectAsState()
    val allStudents by repository.students.collectAsState()

    val studentProfile = remember(currentUser, allStudents) {
        currentUser?.let { repository.getStudentProfile(it.id) }
    }

    var selectedBusId by remember(studentProfile) { mutableStateOf(studentProfile?.assignedBusId ?: allBuses.firstOrNull()?.busId ?: "") }
    var selectedStopId by remember(studentProfile) { mutableStateOf(studentProfile?.assignedStopId ?: "") }

    val currentBus = remember(allBuses, selectedBusId) { allBuses.find { it.busId == selectedBusId } }
    val currentRoute = remember(allRoutes, currentBus) { allRoutes.find { it.routeId == currentBus?.routeId } }

    var isSavedSnackbarVisible by remember { mutableStateOf(false) }

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
                    text = "Student Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage your transport subscription & stop assignment",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Student ID Card
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
                                .background(Color(0xFF4F46E5), CircleShape)
                                .border(2.dp, Color(0xFF818CF8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.name?.take(1) ?: "S",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Column {
                            Text(
                                text = currentUser?.name ?: "Ananya Sharma",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Roll: ${studentProfile?.studentId ?: "CS2023-042"}",
                                fontSize = 13.sp,
                                color = Color(0xFF818CF8)
                            )
                            Text(
                                text = studentProfile?.department ?: "Computer Science & Engg",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF312E81))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Email", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(currentUser?.email ?: "student@college.edu", fontSize = 12.sp, color = Color.White)
                        }
                        Column {
                            Text("Emergency Contact", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(studentProfile?.emergencyContact ?: "+91 94480 11223", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Assigned Bus & Stop Preference Form
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
                    Text(
                        text = "Bus & Stop Assignment",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Bus Selector
                    var busDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = busDropdownExpanded,
                        onExpandedChange = { busDropdownExpanded = !busDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentBus?.let { "${it.busNumber} (${it.modelName})" } ?: "Select Bus",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assigned College Bus") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = busDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("select_bus_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = busDropdownExpanded,
                            onDismissRequest = { busDropdownExpanded = false }
                        ) {
                            allBuses.forEach { bus ->
                                DropdownMenuItem(
                                    text = { Text("${bus.busNumber} • Route ${bus.routeId.substringAfter("_")}") },
                                    onClick = {
                                        selectedBusId = bus.busId
                                        val newRoute = allRoutes.find { it.routeId == bus.routeId }
                                        selectedStopId = newRoute?.stops?.firstOrNull()?.stopId ?: ""
                                        busDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Stop Selector
                    var stopDropdownExpanded by remember { mutableStateOf(false) }
                    val currentStop = currentRoute?.stops?.find { it.stopId == selectedStopId }
                    ExposedDropdownMenuBox(
                        expanded = stopDropdownExpanded,
                        onExpandedChange = { stopDropdownExpanded = !stopDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentStop?.let { "${it.sequence}. ${it.name}" } ?: "Select Boarding Stop",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Boarding / Drop Stop") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stopDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("select_stop_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = stopDropdownExpanded,
                            onDismissRequest = { stopDropdownExpanded = false }
                        ) {
                            currentRoute?.stops?.sortedBy { it.sequence }?.forEach { stop ->
                                DropdownMenuItem(
                                    text = { Text("${stop.sequence}. ${stop.name} (${stop.morningPickupTime})") },
                                    onClick = {
                                        selectedStopId = stop.stopId
                                        stopDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            currentUser?.let {
                                repository.updateStudentAssignment(it.id, selectedBusId, selectedStopId)
                                isSavedSnackbarVisible = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_assignment_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Transport Preferences", fontWeight = FontWeight.Bold)
                    }

                    if (isSavedSnackbarVisible) {
                        Text(
                            text = "✓ Preferences updated successfully!",
                            color = Color(0xFF059669),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Logout
        item {
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("student_logout_button"),
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
