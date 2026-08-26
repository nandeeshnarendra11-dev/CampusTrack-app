package com.example.data

import com.example.model.*

object DemoDataProvider {

    val demoStopsRoute1 = listOf(
        Stop("stop_101", "Central Metro Hub", 12.9716, 77.5946, 1, "Opp. City Railway", "07:15 AM", "04:30 PM"),
        Stop("stop_102", "MG Road Boulevard", 12.9756, 77.6066, 2, "Near Metro Pillar 140", "07:25 AM", "04:40 PM"),
        Stop("stop_103", "Indiranagar 100ft Rd", 12.9784, 77.6408, 3, "KFC Junction", "07:38 AM", "04:55 PM"),
        Stop("stop_104", "Domlur Flyover Stop", 12.9610, 77.6387, 4, "Dell Building Bus Bay", "07:50 AM", "05:10 PM"),
        Stop("stop_105", "Global Tech Park Gate", 12.9352, 77.6946, 5, "Near Bellandur Pedestrian Bridge", "08:05 AM", "05:25 PM"),
        Stop("stop_106", "Campus Main Terminal", 12.9250, 77.6830, 6, "Admin Block Fountain", "08:20 AM", "05:40 PM")
    )

    val demoStopsRoute2 = listOf(
        Stop("stop_201", "Jayanagar 4th Block", 12.9299, 77.5824, 1, "Complex Bus Stop", "07:20 AM", "04:30 PM"),
        Stop("stop_202", "BTM Layout Ring Rd", 12.9166, 77.6101, 2, "Water Tank Signal", "07:35 AM", "04:45 PM"),
        Stop("stop_203", "Silk Board Transit Hub", 12.9176, 77.6234, 3, "Flyover Underpass", "07:50 AM", "05:00 PM"),
        Stop("stop_204", "HSR Layout Sector 1", 12.9121, 77.6445, 4, "Club House Corner", "08:05 AM", "05:15 PM"),
        Stop("stop_205", "Campus Tech Gate 2", 12.9250, 77.6830, 5, "Computer Science Block", "08:25 AM", "05:35 PM")
    )

    val demoStopsRoute3 = listOf(
        Stop("stop_301", "West Railway Terminal", 12.9782, 77.5695, 1, "Main Exit Porch", "07:10 AM", "04:20 PM"),
        Stop("stop_302", "Malleshwaram 8th Cross", 12.9984, 77.5704, 2, "Canara Bank Corner", "07:25 AM", "04:35 PM"),
        Stop("stop_303", "Yeshwanthpur Junction", 13.0224, 77.5528, 3, "Govt Hospital Bay", "07:42 AM", "04:50 PM"),
        Stop("stop_304", "Hebbal Lake Express Bay", 13.0358, 77.5970, 4, "Under Flyover Loop", "08:00 AM", "05:10 PM"),
        Stop("stop_305", "Campus North Quad", 12.9250, 77.6830, 5, "Library & Sports Complex", "08:30 AM", "05:40 PM")
    )

    val initialRoutes = listOf(
        Route(
            routeId = "route_1",
            routeName = "Route 1: Central Metro → Campus Terminal",
            description = "Express college route covering MG Road, Indiranagar, and Tech Corridor",
            colorHex = 0xFF4F46E5, // Indigo
            stops = demoStopsRoute1,
            totalDistanceKm = 16.4,
            estimatedDurationMin = 45
        ),
        Route(
            routeId = "route_2",
            routeName = "Route 2: Jayanagar & HSR → Campus Gate 2",
            description = "South suburbs college route via BTM, Silk Board and HSR Layout",
            colorHex = 0xFF059669, // Emerald
            stops = demoStopsRoute2,
            totalDistanceKm = 13.8,
            estimatedDurationMin = 40
        ),
        Route(
            routeId = "route_3",
            routeName = "Route 3: West Rail & Hebbal → North Quad",
            description = "North-West student shuttle connecting railway terminus and Hebbal ring",
            colorHex = 0xFFD97706, // Amber
            stops = demoStopsRoute3,
            totalDistanceKm = 18.2,
            estimatedDurationMin = 50
        )
    )

    val initialBuses = listOf(
        Bus(
            busId = "bus_101",
            busNumber = "KA-04-E-1829",
            registrationNumber = "IND-KA04-2022-1829",
            driverId = "driver_1",
            routeId = "route_1",
            status = BusStatus.ON_ROUTE,
            capacity = 48,
            occupancy = 34,
            isDemoBus = true,
            modelName = "Tata Starbus Ultra"
        ),
        Bus(
            busId = "bus_102",
            busNumber = "KA-01-B-4920",
            registrationNumber = "IND-KA01-2023-4920",
            driverId = "driver_2",
            routeId = "route_2",
            status = BusStatus.ARRIVING_SOON,
            capacity = 45,
            occupancy = 29,
            isDemoBus = true,
            modelName = "Ashok Leyland Sunshine"
        ),
        Bus(
            busId = "bus_103",
            busNumber = "KA-05-TR-8831",
            registrationNumber = "IND-KA05-2021-8831",
            driverId = "driver_3",
            routeId = "route_3",
            status = BusStatus.ON_ROUTE,
            capacity = 52,
            occupancy = 41,
            isDemoBus = true,
            modelName = "BharatBenz School Edition"
        )
    )

    val initialUsers = listOf(
        User(
            id = "student_user_1",
            name = "Ananya Sharma",
            email = "student.ananya@college.edu",
            phone = "+91 98450 12345",
            role = UserRole.STUDENT
        ),
        User(
            id = "driver_user_1",
            name = "Rajesh Kumar",
            email = "driver.rajesh@college.edu",
            phone = "+91 98450 67890",
            role = UserRole.DRIVER
        ),
        User(
            id = "admin_user_1",
            name = "Dr. Suresh Verma",
            email = "admin@college.edu",
            phone = "+91 98450 99999",
            role = UserRole.ADMIN
        )
    )

    val initialStudents = listOf(
        StudentProfile(
            userId = "student_user_1",
            studentId = "CS2023-042",
            assignedBusId = "bus_101",
            assignedStopId = "stop_103", // Indiranagar 100ft Rd
            department = "Computer Science & Engg",
            semester = "6th Semester",
            emergencyContact = "+91 94480 11223"
        ),
        StudentProfile(
            userId = "student_user_2",
            studentId = "EC2023-118",
            assignedBusId = "bus_102",
            assignedStopId = "stop_203", // Silk Board
            department = "Electronics & Comm",
            semester = "4th Semester",
            emergencyContact = "+91 94480 22334"
        ),
        StudentProfile(
            userId = "student_user_3",
            studentId = "ME2022-089",
            assignedBusId = "bus_103",
            assignedStopId = "stop_304", // Hebbal Lake
            department = "Mechanical Engg",
            semester = "8th Semester",
            emergencyContact = "+91 94480 33445"
        )
    )

    val initialDrivers = listOf(
        DriverProfile(
            userId = "driver_user_1",
            driverId = "DRV-101",
            assignedBusId = "bus_101",
            licenseNumber = "KA-04-2015-0038291",
            experienceYears = 9,
            rating = 4.9f
        ),
        DriverProfile(
            userId = "driver_user_2",
            driverId = "DRV-102",
            assignedBusId = "bus_102",
            licenseNumber = "KA-01-2017-0091823",
            experienceYears = 6,
            rating = 4.8f
        ),
        DriverProfile(
            userId = "driver_user_3",
            driverId = "DRV-103",
            assignedBusId = "bus_103",
            licenseNumber = "KA-05-2012-0044190",
            experienceYears = 12,
            rating = 5.0f
        )
    )

    val initialNotifications = listOf(
        NotificationItem(
            id = "notif_1",
            title = "Trip Started",
            message = "Bus KA-04-E-1829 has departed from Central Metro Hub on Route 1.",
            type = NotificationType.TRIP_STARTED,
            timestamp = System.currentTimeMillis() - 15 * 60 * 1000,
            busId = "bus_101",
            read = true
        ),
        NotificationItem(
            id = "notif_2",
            title = "Approaching Your Stop",
            message = "Bus KA-04-E-1829 is currently 4.2 km away from Indiranagar 100ft Rd. Estimated arrival in 12 min.",
            type = NotificationType.ARRIVAL_ALERT,
            timestamp = System.currentTimeMillis() - 2 * 60 * 1000,
            busId = "bus_101",
            read = false
        ),
        NotificationItem(
            id = "notif_3",
            title = "Morning Schedule Reminder",
            message = "College transport will operate normally tomorrow. Morning pickup begins at 07:15 AM.",
            type = NotificationType.ROUTE_CHANGE,
            timestamp = System.currentTimeMillis() - 120 * 60 * 1000,
            read = false
        )
    )
}
