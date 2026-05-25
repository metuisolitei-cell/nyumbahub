package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import java.text.NumberFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigateToTab: (String) -> Unit
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val units by viewModel.units.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val maintenanceRequests by viewModel.maintenanceRequests.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    // Financial calculations
    val totalRevenue = payments.filter { it.status == "PAID" }.sumOf { it.amountPaid }
    val totalArrears = payments.filter { it.status == "PENDING" || it.status == "OVERDUE" }.sumOf { it.balanceDue }
    val totalUnitsCount = units.size
    val occupiedUnitsCount = units.count { it.status == "OCCUPIED" }
    val vacantUnitsCount = units.count { it.status == "VACANT" }
    val maintenanceUnitsCount = units.count { it.status == "UNDER_MAINTENANCE" }

    val vacancyRate = if (totalUnitsCount > 0) {
        (vacantUnitsCount.toDouble() / totalUnitsCount.toDouble() * 100).toInt()
    } else 0
    val occupancyRate = if (totalUnitsCount > 0) {
        (occupiedUnitsCount.toDouble() / totalUnitsCount.toDouble() * 100).toInt()
    } else 0

    val pendingMaintenanceCount = maintenanceRequests.count { it.status == "PENDING" }
    val inProgressMaintenanceCount = maintenanceRequests.count { it.status == "IN_PROGRESS" }

    val format = try {
        NumberFormat.getCurrencyInstance(Locale("en", "KE")).apply {
            currency = Currency.getInstance("KES")
        }
    } catch (e: Exception) {
        null
    }

    fun formatPrice(amount: Double): String {
        return if (format != null) {
            try {
                format.format(amount).replace("KES", "Ksh").substringBefore(".")
            } catch (e: Exception) {
                "Ksh " + String.format(Locale.US, "%,.0f", amount)
            }
        } else {
            "Ksh " + String.format(Locale.US, "%,.0f", amount)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Role Indicator
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Habari Yako,",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MutedText)
                    )
                    Text(
                        text = "NyumbaHub Portal",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (currentRole) {
                            "ADMIN" -> CriticalRed.copy(alpha = 0.15f)
                            "LANDLORD" -> MpesaGreen.copy(alpha = 0.15f)
                            "CARETAKER" -> WarmGold.copy(alpha = 0.15f)
                            else -> SecondaryAqua.copy(alpha = 0.15f)
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when (currentRole) {
                                        "ADMIN" -> CriticalRed
                                        "LANDLORD" -> MpesaGreen
                                        "CARETAKER" -> WarmGold
                                        else -> SecondaryAqua
                                    }
                                )
                        )
                        Text(
                            text = currentRole,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Announcement ticker
        val latestAnnouncement = announcements.firstOrNull()
        if (latestAnnouncement != null) {
            item {
                val latest = latestAnnouncement
                Card(
                    colors = CardDefaults.cardColors(containerColor = WarmGold.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(WarmGold.copy(alpha = 0.3f)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = WarmGold,
                            modifier = Modifier.padding(end = 12.dp).size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = latest.title,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = latest.message,
                                color = MutedText,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // ROLE-SPECIFIC QUICK ACTIONS & VIEWS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Command Actions",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentRole == "LANDLORD" || currentRole == "ADMIN") {
                            Button(
                                onClick = { viewModel.generateInvoicesForAll() },
                                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("action_gen_invoice")
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Invoicing", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onNavigateToTab("BROADCAST") },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateBackground),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SlateBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = null, tint = MpesaGreen)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Broadcast", color = PureWhite, fontSize = 11.sp)
                            }
                        } else if (currentRole == "TENANT") {
                            Button(
                                onClick = { onNavigateToTab("FINANCE") },
                                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pay Rent", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onNavigateToTab("MAINTENANCE") },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateBackground),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SlateBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Construction, contentDescription = null, tint = SecondaryAqua)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("File Ticket", color = PureWhite, fontSize = 11.sp)
                            }
                        } else if (currentRole == "CARETAKER") {
                            Button(
                                onClick = { onNavigateToTab("MAINTENANCE") },
                                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Construction, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Review Repairs", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onNavigateToTab("PROPERTIES") },
                                colors = ButtonDefaults.buttonColors(containerColor = SlateBackground),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, SlateBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, tint = WarmGold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Unit Logs", color = PureWhite, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // KEY PERFORMANCE INDICATOR CARDS (KPI)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    title = "Active Portfolio",
                    value = "${properties.size} Properties",
                    subtitle = "$totalUnitsCount Total Units",
                    icon = Icons.Default.Apartment,
                    color = SecondaryAqua,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Occupancy Rate",
                    value = "$occupancyRate%",
                    subtitle = "$occupiedUnitsCount occupied, $vacantUnitsCount vacant",
                    icon = Icons.Default.Groups,
                    color = MpesaGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val revForm = formatPrice(totalRevenue)
                val arrForm = formatPrice(totalArrears)
                
                KpiCard(
                    title = "Monthly Ledger",
                    value = revForm,
                    subtitle = "Accrued M-Pesa",
                    icon = Icons.Default.Payments,
                    color = MpesaGreen,
                    modifier = Modifier.weight(1f)
                )

                KpiCard(
                    title = "Unpaid Bills",
                    value = arrForm,
                    subtitle = "Total outstanding balance",
                    icon = Icons.Default.Upcoming,
                    color = if (totalArrears > 0) OrangeAccent else MutedText,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // CHARTS VIEW AND PROGRESS PIPELINES
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Portfolio Status Visualizers",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Donut Chart Canvas
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 12.dp.toPx()
                                val rectSize = (size.width - strokeWidth).coerceAtLeast(0f)
                                val arcSize = Size(rectSize, rectSize)
                                val offsetObj = Offset(strokeWidth / 2, strokeWidth / 2)

                                val occupiedAngle = 360f * (occupancyRate.toFloat() / 100f)
                                val vacantAngle = 360f * (vacancyRate.toFloat() / 100f)
                                val maintenanceAngle = 360f - occupiedAngle - vacantAngle

                                // Draw occupied (Green)
                                drawArc(
                                    color = MpesaGreen,
                                    startAngle = -90f,
                                    sweepAngle = occupiedAngle,
                                    useCenter = false,
                                    topLeft = offsetObj,
                                    size = arcSize,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                )

                                // Draw vacant (Blue)
                                drawArc(
                                    color = SecondaryAqua,
                                    startAngle = -90f + occupiedAngle,
                                    sweepAngle = vacantAngle,
                                    useCenter = false,
                                    topLeft = offsetObj,
                                    size = arcSize,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                )

                                // Draw maintenance (Orange/Yellow)
                                if (maintenanceCountFraction(totalUnitsCount, maintenanceUnitsCount) > 0f) {
                                    drawArc(
                                        color = WarmGold,
                                        startAngle = -90f + occupiedAngle + vacantAngle,
                                        sweepAngle = maintenanceAngle,
                                        useCenter = false,
                                        topLeft = offsetObj,
                                        size = arcSize,
                                        style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$occupancyRate%",
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Occupied",
                                    color = MutedText,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // 2. Legend
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            LegendRow(color = MpesaGreen, text = "Occupied ($occupiedUnitsCount units)")
                            LegendRow(color = SecondaryAqua, text = "Vacant ($vacantUnitsCount units)")
                            LegendRow(color = WarmGold, text = "Maintenance ($maintenanceUnitsCount units)")
                        }
                    }
                }
            }
        }

        // REAL-TIME MAINTENANCE PIPELINE TRACKING
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Repair Log Queue",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MaintenanceMiniPipe(title = "Unassigned", count = pendingMaintenanceCount, color = CriticalRed, modifier = Modifier.weight(1f))
                        MaintenanceMiniPipe(title = "In Progress", count = inProgressMaintenanceCount, color = WarmGold, modifier = Modifier.weight(1f))
                        MaintenanceMiniPipe(title = "Resolved", count = maintenanceRequests.count { it.status == "COMPLETED" }, color = MpesaGreen, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // QUICK LIST OF POPULAR LANDLORD PROPERTIES
        if (properties.isNotEmpty()) {
            item {
                Text(
                    text = "Properties Hub Directory",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(properties.take(2)) { prop ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier.fillMaxWidth().clickable {
                        viewModel.setSelectedProperty(prop.id)
                        onNavigateToTab("PROPERTIES")
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MpesaGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (prop.type) {
                                    "COMMERCIAL" -> Icons.Default.Business
                                    "SINGLE_UNIT" -> Icons.Default.Home
                                    else -> Icons.Default.Apartment
                                },
                                contentDescription = null,
                                tint = MpesaGreen,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prop.name,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = prop.address,
                                color = MutedText,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${prop.totalUnits - prop.vacantUnits}/${prop.totalUnits}",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Occupied",
                                color = MutedText,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SlateBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    color = MutedText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = MutedText,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LegendRow(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            color = PureWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MaintenanceMiniPipe(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SlateBackground)
            .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = title, color = MutedText, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count Requests",
                color = if (count > 0) color else PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

fun maintenanceCountFraction(total: Int, target: Int): Float =
    if (total > 0) target.toFloat() / total.toFloat() else 0f

val OrangeAccent = Color(0xFFFF9800)
