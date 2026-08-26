package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BusStatus

@Composable
fun BusStatusBadge(
    status: BusStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status) {
        BusStatus.ON_ROUTE -> Triple(Color(0xFFC4EED0), Color(0xFF072711), Color(0xFFA3E7B7))
        BusStatus.ARRIVING_SOON -> Triple(Color(0xFFFEF3C7), Color(0xFF78350F), Color(0xFFFDE68A))
        BusStatus.DELAYED -> Triple(Color(0xFFFCE8E6), Color(0xFFB3261E), Color(0xFFF8B4B4))
        BusStatus.TRIP_COMPLETED -> Triple(Color(0xFFF1F3F4), Color(0xFF49454F), Color(0xFFE1E3E8))
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .testTag("bus_status_badge_${status.name.lowercase()}"),
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = status.emoji, fontSize = 12.sp)
            Text(
                text = status.label.uppercase(),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
