package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusStatus
import com.example.model.User
import com.example.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusTopAppBar(
    currentUser: User?,
    unreadNotifCount: Int = 0,
    activeSosCount: Int = 0,
    onRoleSelect: (UserRole) -> Unit,
    onNotifClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showRoleMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE8F0FE), RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFD3E3FD), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = "CampusTrack",
                            tint = Color(0xFF0B57D0),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CampusTrack",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp
                        )
                        Text(
                            text = "COLLEGE TRANSIT LIVE",
                            color = Color(0xFF49454F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }

                // Right Actions: Quick Role Switcher Pill & Notifications & Avatar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Role Switcher Pill Dropdown
                    Box {
                        val roleLabel = currentUser?.role?.name ?: "STUDENT"
                        val (roleBg, roleText, roleBorder) = when (currentUser?.role) {
                            UserRole.ADMIN -> Triple(Color(0xFFFCE8E6), Color(0xFFB3261E), Color(0xFFF8B4B4))
                            UserRole.DRIVER -> Triple(Color(0xFFFEF3C7), Color(0xFF78350F), Color(0xFFFDE68A))
                            else -> Triple(Color(0xFFC4EED0), Color(0xFF072711), Color(0xFFA3E7B7))
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showRoleMenu = true }
                                .testTag("role_switcher_pill"),
                            color = roleBg,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, roleBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(roleText, CircleShape)
                                )
                                Text(
                                    text = roleLabel,
                                    color = roleText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.4.sp
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Role",
                                    tint = roleText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, Color(0xFFE1E3E8), RoundedCornerShape(16.dp))
                        ) {
                            DropdownMenuItem(
                                text = { Text("🎓 Student View (Ananya)", fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showRoleMenu = false
                                    onRoleSelect(UserRole.STUDENT)
                                },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF007A5A)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🚌 Driver Console (Rajesh)", fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showRoleMenu = false
                                    onRoleSelect(UserRole.DRIVER)
                                },
                                leadingIcon = { Icon(Icons.Default.AirlineSeatReclineNormal, contentDescription = null, tint = Color(0xFFB45309)) }
                            )
                            DropdownMenuItem(
                                text = { Text("🛡️ Admin Command (Dr. Suresh)", fontWeight = FontWeight.Medium) },
                                onClick = {
                                    showRoleMenu = false
                                    onRoleSelect(UserRole.ADMIN)
                                },
                                leadingIcon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color(0xFFB3261E)) }
                            )
                        }
                    }

                    // Notifications Bell
                    IconButton(
                        onClick = onNotifClick,
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, Color(0xFFE1E3E8), CircleShape)
                            .testTag("notification_bell_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFB3261E),
                                        contentColor = Color.White
                                    ) {
                                        Text(unreadNotifCount.toString(), fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color(0xFF1D1B20),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Profile Avatar Pill (Initials AR style from HTML mockup)
                    val initials = remember(currentUser) {
                        val name = currentUser?.name ?: "User"
                        val parts = name.split(" ").filter { it.isNotBlank() }
                        if (parts.size >= 2) "${parts[0].first()}${parts[1].first()}".uppercase()
                        else name.take(2).uppercase()
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD3E3FD))
                            .border(1.5.dp, Color.White, CircleShape)
                            .clickable { onProfileClick() }
                            .testTag("profile_avatar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFA8C7FA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                color = Color(0xFF041E49),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE1E3E8), thickness = 1.dp)
        }
    }
}

@Composable
fun LiveEtaMetricsGrid(
    etaMinutes: Int,
    distanceKmText: String,
    nextStopName: String,
    busStatus: BusStatus,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top 3-card Sleek grid directly from design HTML
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Box 1: ETA
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE1E3E8), RoundedCornerShape(18.dp)),
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "ETA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (busStatus == BusStatus.TRIP_COMPLETED) "Done" else "$etaMinutes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0B57D0)
                        )
                        if (busStatus != BusStatus.TRIP_COMPLETED) {
                            Text(
                                text = "m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF0B57D0),
                                modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
                            )
                        }
                    }
                }
            }

            // Box 2: DIST
            val cleanedDist = distanceKmText.replace(" km", "").replace(" m", "m")
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE1E3E8), RoundedCornerShape(18.dp)),
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "DIST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 0.5.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = cleanedDist,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "km",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF1D1B20),
                            modifier = Modifier.padding(bottom = 2.dp, start = 1.dp)
                        )
                    }
                }
            }

            // Box 3: NEXT
            Surface(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color(0xFFE1E3E8), RoundedCornerShape(18.dp)),
                color = Color.White,
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "NEXT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF49454F),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = nextStopName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1D1B20),
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = highlightColor,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun ProximityAlertCard(
    isNear: Boolean,
    stopName: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isNear,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("proximity_alert_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFB45309), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = "Alert",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Your bus is arriving soon!",
                        color = Color(0xFF78350F),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Within 500 meters of $stopName. Please be ready at your pickup bay.",
                        color = Color(0xFF92400E),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
