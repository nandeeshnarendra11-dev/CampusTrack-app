package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CampusTransitRepository
import com.example.model.UserRole
import com.example.ui.components.CampusTopAppBar
import com.example.ui.screens.admin.*
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.driver.*
import com.example.ui.screens.student.*

enum class StudentTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    LIVE_MAP("Live Map", Icons.Filled.Navigation, Icons.Outlined.Navigation),
    SCHEDULE("Schedule", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    NOTIFICATIONS("Alerts", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

enum class DriverTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    DASHBOARD("Console", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    ROUTE("Route Map", Icons.Filled.AltRoute, Icons.Outlined.AltRoute),
    HISTORY("Trips", Icons.Filled.History, Icons.Outlined.History),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

enum class AdminTab(val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    DASHBOARD("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    FLEET_MAP("Fleet Map", Icons.Filled.Map, Icons.Outlined.Map),
    STUDENTS("Students", Icons.Filled.School, Icons.Outlined.School),
    BUSES("Fleet", Icons.Filled.DirectionsBus, Icons.Outlined.DirectionsBus),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun CampusNavHost(
    repository: CampusTransitRepository,
    modifier: Modifier = Modifier
) {
    val currentUser by repository.currentUser.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val emergencies by repository.emergencyAlerts.collectAsState()

    val unreadCount = remember(notifications) { notifications.count { !it.read } }
    val activeSosCount = remember(emergencies) { emergencies.count { !it.isResolved } }

    var studentTab by remember { mutableStateOf(StudentTab.HOME) }
    var driverTab by remember { mutableStateOf(DriverTab.DASHBOARD) }
    var adminTab by remember { mutableStateOf(AdminTab.DASHBOARD) }

    // Subscreen state for admin deep-navigation
    var adminSubScreen by remember { mutableStateOf<String?>(null) }

    if (currentUser == null) {
        LoginScreen(
            repository = repository,
            onLoginSuccess = {}
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                CampusTopAppBar(
                    currentUser = currentUser,
                    unreadNotifCount = unreadCount,
                    activeSosCount = activeSosCount,
                    onRoleSelect = { newRole ->
                        repository.switchRole(newRole)
                        adminSubScreen = null
                    },
                    onNotifClick = {
                        if (currentUser?.role == UserRole.STUDENT) {
                            studentTab = StudentTab.NOTIFICATIONS
                        } else if (currentUser?.role == UserRole.ADMIN) {
                            adminSubScreen = "NOTIFICATIONS"
                        }
                    },
                    onProfileClick = {
                        if (currentUser?.role == UserRole.STUDENT) {
                            studentTab = StudentTab.PROFILE
                        } else if (currentUser?.role == UserRole.DRIVER) {
                            driverTab = DriverTab.PROFILE
                        }
                    },
                    onLogoutClick = { repository.logout() }
                )
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("app_bottom_navigation"),
                    containerColor = Color.White,
                    tonalElevation = 0.dp
                ) {
                    when (currentUser?.role) {
                        UserRole.STUDENT -> {
                            StudentTab.values().forEach { tab ->
                                val isSelected = studentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { studentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            tab.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF0B57D0),
                                        selectedTextColor = Color(0xFF0B57D0),
                                        indicatorColor = Color(0xFFE8F0FE),
                                        unselectedIconColor = Color(0xFF49454F),
                                        unselectedTextColor = Color(0xFF49454F)
                                    ),
                                    modifier = Modifier.testTag("student_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                        UserRole.DRIVER -> {
                            DriverTab.values().forEach { tab ->
                                val isSelected = driverTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { driverTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            tab.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF007A5A),
                                        selectedTextColor = Color(0xFF007A5A),
                                        indicatorColor = Color(0xFFE6F4EA),
                                        unselectedIconColor = Color(0xFF49454F),
                                        unselectedTextColor = Color(0xFF49454F)
                                    ),
                                    modifier = Modifier.testTag("driver_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                        UserRole.ADMIN -> {
                            AdminTab.values().forEach { tab ->
                                val isSelected = adminTab == tab && adminSubScreen == null
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        adminTab = tab
                                        adminSubScreen = null
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            tab.title,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF0B57D0),
                                        selectedTextColor = Color(0xFF0B57D0),
                                        indicatorColor = Color(0xFFE8F0FE),
                                        unselectedIconColor = Color(0xFF49454F),
                                        unselectedTextColor = Color(0xFF49454F)
                                    ),
                                    modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentUser?.role) {
                    UserRole.STUDENT -> {
                        when (studentTab) {
                            StudentTab.HOME -> StudentHomeScreen(
                                repository = repository,
                                onNavigateToLiveMap = { studentTab = StudentTab.LIVE_MAP },
                                onNavigateToSchedule = { studentTab = StudentTab.SCHEDULE }
                            )
                            StudentTab.LIVE_MAP -> StudentLiveMapScreen(repository = repository)
                            StudentTab.SCHEDULE -> StudentScheduleScreen(repository = repository)
                            StudentTab.NOTIFICATIONS -> StudentNotificationsScreen(repository = repository)
                            StudentTab.PROFILE -> StudentProfileScreen(
                                repository = repository,
                                onLogout = { repository.logout() }
                            )
                        }
                    }

                    UserRole.DRIVER -> {
                        when (driverTab) {
                            DriverTab.DASHBOARD -> DriverDashboardScreen(
                                repository = repository,
                                onNavigateToRouteMap = { driverTab = DriverTab.ROUTE }
                            )
                            DriverTab.ROUTE -> DriverRouteMapScreen(repository = repository)
                            DriverTab.HISTORY -> DriverTripHistoryScreen(repository = repository)
                            DriverTab.PROFILE -> DriverProfileScreen(
                                repository = repository,
                                onLogout = { repository.logout() }
                            )
                        }
                    }

                    UserRole.ADMIN -> {
                        if (adminSubScreen != null) {
                            when (adminSubScreen) {
                                "DRIVERS" -> AdminDriversScreen(repository = repository)
                                "ROUTES" -> AdminRoutesScreen(repository = repository)
                                "EMERGENCIES" -> AdminEmergenciesScreen(repository = repository)
                                "NOTIFICATIONS" -> AdminNotificationsScreen(repository = repository)
                                else -> AdminDashboardScreen(
                                    repository = repository,
                                    onNavigateToFleetMap = { adminTab = AdminTab.FLEET_MAP; adminSubScreen = null },
                                    onNavigateToStudents = { adminTab = AdminTab.STUDENTS; adminSubScreen = null },
                                    onNavigateToDrivers = { adminSubScreen = "DRIVERS" },
                                    onNavigateToBuses = { adminTab = AdminTab.BUSES; adminSubScreen = null },
                                    onNavigateToRoutes = { adminSubScreen = "ROUTES" },
                                    onNavigateToEmergencies = { adminSubScreen = "EMERGENCIES" },
                                    onNavigateToNotifications = { adminSubScreen = "NOTIFICATIONS" },
                                    onNavigateToSettings = { adminTab = AdminTab.SETTINGS; adminSubScreen = null }
                                )
                            }
                        } else {
                            when (adminTab) {
                                AdminTab.DASHBOARD -> AdminDashboardScreen(
                                    repository = repository,
                                    onNavigateToFleetMap = { adminTab = AdminTab.FLEET_MAP },
                                    onNavigateToStudents = { adminTab = AdminTab.STUDENTS },
                                    onNavigateToDrivers = { adminSubScreen = "DRIVERS" },
                                    onNavigateToBuses = { adminTab = AdminTab.BUSES },
                                    onNavigateToRoutes = { adminSubScreen = "ROUTES" },
                                    onNavigateToEmergencies = { adminSubScreen = "EMERGENCIES" },
                                    onNavigateToNotifications = { adminSubScreen = "NOTIFICATIONS" },
                                    onNavigateToSettings = { adminTab = AdminTab.SETTINGS }
                                )
                                AdminTab.FLEET_MAP -> AdminFleetMapScreen(repository = repository)
                                AdminTab.STUDENTS -> AdminStudentsScreen(repository = repository)
                                AdminTab.BUSES -> AdminBusesScreen(repository = repository)
                                AdminTab.SETTINGS -> AdminSettingsScreen(repository = repository)
                            }
                        }
                    }

                    null -> {}
                }
            }
        }
    }
}
