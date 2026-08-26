package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeoUtils
import com.example.model.Bus
import com.example.model.LiveLocation
import com.example.model.Route
import com.example.model.Stop
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun TransitMapCanvas(
    route: Route?,
    bus: Bus?,
    liveLocation: LiveLocation?,
    studentStop: Stop?,
    allBuses: List<Pair<Bus, LiveLocation>> = emptyList(),
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    showStudentProximityRing: Boolean = true,
    onStopClick: ((Stop) -> Unit)? = null
) {
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "busPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    val textMeasurer = rememberTextMeasurer()

    // Compute bounding box of all points (route stops + bus + student stop)
    val allPoints = remember(route, liveLocation, studentStop, allBuses) {
        val pts = mutableListOf<Pair<Double, Double>>()
        route?.stops?.forEach { pts.add(Pair(it.latitude, it.longitude)) }
        if (liveLocation != null) pts.add(Pair(liveLocation.latitude, liveLocation.longitude))
        if (studentStop != null) pts.add(Pair(studentStop.latitude, studentStop.longitude))
        allBuses.forEach { (_, loc) -> pts.add(Pair(loc.latitude, loc.longitude)) }
        if (pts.isEmpty()) {
            listOf(Pair(12.9716, 77.5946), Pair(12.9250, 77.6830))
        } else {
            pts
        }
    }

    val minLat = remember(allPoints) { allPoints.minOf { it.first } }
    val maxLat = remember(allPoints) { allPoints.maxOf { it.first } }
    val minLon = remember(allPoints) { allPoints.minOf { it.second } }
    val maxLon = remember(allPoints) { allPoints.maxOf { it.second } }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, Color(0xFFE1E3E8), RoundedCornerShape(28.dp))
            .background(Color(0xFFF0F4F8)) // Sleek Interface Canvas Background
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("transit_map_canvas")
                .then(
                    if (isInteractive) {
                        Modifier.pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                zoomScale = (zoomScale * zoom).coerceIn(0.6f, 3.5f)
                                panOffsetX += pan.x
                                panOffsetY += pan.y
                            }
                        }
                    } else Modifier
                )
        ) {
            val width = size.width
            val height = size.height
            val padding = 70f

            val latSpan = (maxLat - minLat).coerceAtLeast(0.01)
            val lonSpan = (maxLon - minLon).coerceAtLeast(0.01)

            // Function to map geo-coordinate (lat, lon) to canvas offset (x, y)
            fun toScreenOffset(lat: Double, lon: Double): Offset {
                val normX = ((lon - minLon) / lonSpan).toFloat()
                val normY = (1.0f - ((lat - minLat) / latSpan).toFloat()) // Invert Y for screen coords

                val basePx = padding + normX * (width - 2 * padding)
                val basePy = padding + normY * (height - 2 * padding)

                val centerX = width / 2f
                val centerY = height / 2f

                val scaledX = centerX + (basePx - centerX) * zoomScale + panOffsetX
                val scaledY = centerY + (basePy - centerY) * zoomScale + panOffsetY
                return Offset(scaledX, scaledY)
            }

            // 1. Draw Map Grid & Radial Dot Pattern
            drawMapBackgroundGrid(width, height, zoomScale, panOffsetX, panOffsetY)

            // 2. Draw Active Route Polyline & Glow
            if (route != null && route.stops.size >= 2) {
                val stopPoints = route.stops.sortedBy { it.sequence }.map { toScreenOffset(it.latitude, it.longitude) }

                // Route underglow/casing in soft blue (#A8C7FA)
                val routePath = Path().apply {
                    moveTo(stopPoints.first().x, stopPoints.first().y)
                    for (i in 1 until stopPoints.size) {
                        val pPrev = stopPoints[i - 1]
                        val pCurr = stopPoints[i]
                        val midX = (pPrev.x + pCurr.x) / 2
                        val midY = (pPrev.y + pCurr.y) / 2
                        quadraticTo(pPrev.x, pPrev.y, midX, midY)
                        lineTo(pCurr.x, pCurr.y)
                    }
                }

                // Casing
                drawPath(
                    path = routePath,
                    color = Color(0xFFA8C7FA),
                    style = Stroke(width = 14f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Main route line in Google Electric Cobalt (#0B57D0)
                drawPath(
                    path = routePath,
                    color = Color(0xFF0B57D0),
                    style = Stroke(width = 5f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            }

            // 3. Draw Dotted Proximity Line between Bus and Student's Stop
            if (liveLocation != null && studentStop != null) {
                val busPos = toScreenOffset(liveLocation.latitude, liveLocation.longitude)
                val stopPos = toScreenOffset(studentStop.latitude, studentStop.longitude)

                drawLine(
                    color = Color(0xFF0B57D0),
                    start = busPos,
                    end = stopPos,
                    strokeWidth = 3f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )

                // Midpoint distance label
                val midX = (busPos.x + stopPos.x) / 2
                val midY = (busPos.y + stopPos.y) / 2
                val distMeters = GeoUtils.calculateDistanceMeters(
                    liveLocation.latitude, liveLocation.longitude,
                    studentStop.latitude, studentStop.longitude
                )
                val distText = GeoUtils.formatDistance(distMeters)

                drawDistancePill(textMeasurer, distText, Offset(midX, midY))
            }

            // 4. Draw Route Stops
            route?.stops?.sortedBy { it.sequence }?.forEach { stop ->
                val pos = toScreenOffset(stop.latitude, stop.longitude)
                val isStudentPickup = studentStop?.stopId == stop.stopId

                if (isStudentPickup) {
                    // Student Selected Stop Pin (Design HTML: #D3E3FD outer with white border and #0B57D0 inner dot)
                    drawCircle(
                        color = Color(0xFF0B57D0).copy(alpha = 0.18f),
                        radius = 24f * zoomScale,
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFFD3E3FD),
                        radius = 12f * zoomScale,
                        center = pos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 12f * zoomScale,
                        center = pos,
                        style = Stroke(width = 2.5f * zoomScale)
                    )
                    drawCircle(
                        color = Color(0xFF0B57D0),
                        radius = 5f * zoomScale,
                        center = pos
                    )
                    val label = "YOUR STOP (${stop.name})"
                    drawStopLabel(textMeasurer, label, pos, isHighlight = true)
                } else {
                    // Regular Stop Pin
                    drawCircle(
                        color = Color.White,
                        radius = 7f * zoomScale,
                        center = pos
                    )
                    drawCircle(
                        color = Color(0xFF49454F),
                        radius = 7f * zoomScale,
                        center = pos,
                        style = Stroke(width = 2f * zoomScale)
                    )
                    drawCircle(
                        color = Color(0xFF0B57D0),
                        radius = 3f * zoomScale,
                        center = pos
                    )
                    drawStopLabel(textMeasurer, "${stop.sequence}. ${stop.name}", pos, isHighlight = false)
                }
            }

            // 5. Draw Other Fleet Buses (if multi-bus view)
            allBuses.forEach { (otherBus, otherLoc) ->
                if (bus == null || otherBus.busId != bus.busId) {
                    val pos = toScreenOffset(otherLoc.latitude, otherLoc.longitude)
                    drawBusMarker(
                        textMeasurer = textMeasurer,
                        pos = pos,
                        heading = otherLoc.headingDegrees,
                        busNumber = otherBus.busNumber,
                        isPrimary = false,
                        isDemo = otherBus.isDemoBus,
                        speed = otherLoc.speedKmH,
                        zoomScale = zoomScale
                    )
                }
            }

            // 6. Draw Primary Bus Marker with Pulsing Radar
            if (bus != null && liveLocation != null) {
                val busPos = toScreenOffset(liveLocation.latitude, liveLocation.longitude)

                // Pulsing radar wave
                drawCircle(
                    color = Color(0xFF0B57D0).copy(alpha = pulseAlpha),
                    radius = pulseRadius * zoomScale * 1.5f,
                    center = busPos
                )

                drawBusMarker(
                    textMeasurer = textMeasurer,
                    pos = busPos,
                    heading = liveLocation.headingDegrees,
                    busNumber = bus.busNumber,
                    isPrimary = true,
                    isDemo = bus.isDemoBus,
                    speed = liveLocation.speedKmH,
                    zoomScale = zoomScale
                )
            }
        }

        // Floating Map Controls
        if (isInteractive) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(3.5f) },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE1E3E8), RoundedCornerShape(14.dp))
                        .shadow(3.dp, RoundedCornerShape(14.dp))
                        .testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color(0xFF041E49), modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE1E3E8), RoundedCornerShape(14.dp))
                        .shadow(3.dp, RoundedCornerShape(14.dp))
                        .testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color(0xFF041E49), modifier = Modifier.size(20.dp))
                }

                IconButton(
                    onClick = {
                        zoomScale = 1.0f
                        panOffsetX = 0f
                        panOffsetY = 0f
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .border(1.dp, Color(0xFFE1E3E8), RoundedCornerShape(14.dp))
                        .shadow(3.dp, RoundedCornerShape(14.dp))
                        .testTag("recenter_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter Map", tint = Color(0xFF0B57D0), modifier = Modifier.size(20.dp))
                }
            }
        }

        // Bottom Left Legend / Status Pill
        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE1E3E8)),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFF007A5A), CircleShape)
                )
                Text(
                    text = if (bus?.isDemoBus == true) "LIVE (DEMO BUS)" else "LIVE GPS",
                    color = Color(0xFF1D1B20),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Canvas Drawing Helper Functions
// -----------------------------------------------------------------------------

private fun DrawScope.drawMapBackgroundGrid(
    width: Float,
    height: Float,
    zoomScale: Float,
    panX: Float,
    panY: Float
) {
    val gridSpacing = 36f * zoomScale
    val startX = (panX % gridSpacing)
    val startY = (panY % gridSpacing)

    // Subtle dot grid (#CED4DA) matching HTML: radial-gradient(#CED4DA 1.5px, transparent 1.5px)
    var x = startX
    while (x < width) {
        var y = startY
        while (y < height) {
            drawCircle(
                color = Color(0xFFCED4DA),
                radius = 1.5f * zoomScale.coerceIn(0.8f, 1.8f),
                center = Offset(x, y)
            )
            y += gridSpacing
        }
        x += gridSpacing
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawBusMarker(
    textMeasurer: TextMeasurer,
    pos: Offset,
    heading: Float,
    busNumber: String,
    isPrimary: Boolean,
    isDemo: Boolean,
    speed: Float,
    zoomScale: Float
) {
    val busColor = if (isPrimary) Color(0xFF0B57D0) else Color(0xFFB45309)
    val markerSize = 22f * zoomScale

    // Bus Outer Halo
    drawCircle(
        color = Color.White,
        radius = markerSize + 3f,
        center = pos
    )

    drawCircle(
        color = busColor,
        radius = markerSize,
        center = pos
    )

    // Direction arrow / bus icon
    rotate(degrees = heading, pivot = pos) {
        val arrowPath = Path().apply {
            moveTo(pos.x, pos.y - markerSize * 0.65f)
            lineTo(pos.x - markerSize * 0.45f, pos.y + markerSize * 0.45f)
            lineTo(pos.x, pos.y + markerSize * 0.15f)
            lineTo(pos.x + markerSize * 0.45f, pos.y + markerSize * 0.45f)
            close()
        }
        drawPath(path = arrowPath, color = Color.White)
    }

    // Bus Label Badge (#1D1B20 rounded container with white text)
    val label = if (isDemo) "Demo $busNumber" else busNumber
    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(label),
        style = TextStyle(
            color = Color.White,
            fontSize = (10 * zoomScale).coerceIn(9f, 12f).sp,
            fontWeight = FontWeight.Bold
        )
    )

    val labelX = pos.x - (textLayoutResult.size.width / 2f)
    val labelY = pos.y - markerSize - textLayoutResult.size.height - 8f

    drawRoundRect(
        color = Color(0xFF1D1B20),
        topLeft = Offset(labelX - 8f, labelY - 4f),
        size = androidx.compose.ui.geometry.Size(
            textLayoutResult.size.width + 16f,
            textLayoutResult.size.height + 8f
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(labelX, labelY)
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawStopLabel(
    textMeasurer: TextMeasurer,
    text: String,
    pos: Offset,
    isHighlight: Boolean
) {
    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(text),
        style = TextStyle(
            color = if (isHighlight) Color(0xFF041E49) else Color(0xFF49454F),
            fontSize = if (isHighlight) 10.sp else 9.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium
        )
    )

    val labelX = pos.x + 14f
    val labelY = pos.y - (textLayoutResult.size.height / 2f)

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(labelX - 6f, labelY - 3f),
        size = androidx.compose.ui.geometry.Size(
            textLayoutResult.size.width + 12f,
            textLayoutResult.size.height + 6f
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    drawRoundRect(
        color = if (isHighlight) Color(0xFFD3E3FD) else Color(0xFFE1E3E8),
        topLeft = Offset(labelX - 6f, labelY - 3f),
        size = androidx.compose.ui.geometry.Size(
            textLayoutResult.size.width + 12f,
            textLayoutResult.size.height + 6f
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 1f)
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(labelX, labelY)
    )
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawDistancePill(
    textMeasurer: TextMeasurer,
    distText: String,
    pos: Offset
) {
    val textLayoutResult = textMeasurer.measure(
        text = AnnotatedString(distText),
        style = TextStyle(
            color = Color(0xFF0B57D0),
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    )

    val pillWidth = textLayoutResult.size.width + 16f
    val pillHeight = textLayoutResult.size.height + 8f

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(pos.x - pillWidth / 2, pos.y - pillHeight / 2),
        size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
    )

    drawRoundRect(
        color = Color(0xFFD3E3FD),
        topLeft = Offset(pos.x - pillWidth / 2, pos.y - pillHeight / 2),
        size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f),
        style = Stroke(width = 1.5f)
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(pos.x - textLayoutResult.size.width / 2, pos.y - textLayoutResult.size.height / 2)
    )
}
