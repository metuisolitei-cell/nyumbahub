package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(viewModel: AppViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val maintenanceRequests by viewModel.maintenanceRequests.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val units by viewModel.units.collectAsState()

    var showLodgeDialog by remember { mutableStateOf(false) }
    var selectedRequestToProgress by remember { mutableStateOf<MaintenanceEntity?>(null) }
    var selectedRequestForDetails by remember { mutableStateOf<MaintenanceEntity?>(null) }

    // Forms for Lodge Request
    var rTitle by remember { mutableStateOf("") }
    var rDesc by remember { mutableStateOf("") }
    var rCategory by remember { mutableStateOf("PLUMBING") }
    var rUrgency by remember { mutableStateOf("MEDIUM") }

    // State for resolving comments
    var progressComments by remember { mutableStateOf("") }

    // Filter list appropriately
    val requests = remember(maintenanceRequests, currentRole, currentUser) {
        when {
            currentRole == "TENANT" && currentUser != null -> {
                maintenanceRequests.filter { it.tenantEmail == currentUser!!.email }
            }
            currentRole == "CARETAKER" && currentUser != null -> {
                maintenanceRequests.filter {
                    it.assignedCaretakerEmail == currentUser!!.email || it.assignedCaretakerEmail.isEmpty()
                }
            }
            else -> maintenanceRequests
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Maintenance Board",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    )
                    Text(
                        text = "Log estate defects, assign repair tasks, update completion statuses",
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                    )
                }

                if (currentRole == "TENANT") {
                    Button(
                        onClick = { showLodgeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("lodge_maintenance_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lodge", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Quick Stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MaintenanceBlockCard(title = "Open Cases", count = requests.count { it.status == "PENDING" }, color = CriticalRed, modifier = Modifier.weight(1f))
                MaintenanceBlockCard(title = "Ongoing Work", count = requests.count { it.status == "IN_PROGRESS" }, color = WarmGold, modifier = Modifier.weight(1f))
            }

            // Request list
            if (requests.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Construction, contentDescription = null, tint = MutedText, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No maintenance requests listed.", color = PureWhite, fontSize = 15.sp)
                        Text(
                            text = if (currentRole == "TENANT") "Click Lodge at the top to describe your plumbing or electrical defect." else "Enjoying zero physical estate issues currently!",
                            color = MutedText, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(requests) { req ->
                        val u = units.firstOrNull { it.id == req.unitId }
                        val p = properties.firstOrNull { it.id == req.propertyId }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth().clickable { selectedRequestForDetails = req }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = req.title,
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Category: ${req.category} | ${p?.name ?: "Estate"} - ${u?.unitNumber ?: "Room"}",
                                            color = MutedText,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        // Status badge
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (req.status) {
                                                    "PENDING" -> CriticalRed.copy(alpha = 0.15f)
                                                    "IN_PROGRESS" -> WarmGold.copy(alpha = 0.15f)
                                                    else -> MpesaGreen.copy(alpha = 0.15f)
                                                }
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = req.status,
                                                color = when (req.status) {
                                                    "PENDING" -> CriticalRed
                                                    "IN_PROGRESS" -> WarmGold
                                                    else -> MpesaGreen
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Urgency Indicator
                                        Text(
                                            text = "${req.urgency} Priority",
                                            color = when(req.urgency) {
                                                "EMERGENCY" -> CriticalRed
                                                "HIGH" -> OrangeAccent
                                                else -> MutedText
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = req.description,
                                    color = PureWhite,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = SlateBorder)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Filed on: ${req.createdAt.substringBefore(" ")}",
                                        color = MutedText,
                                        fontSize = 10.sp
                                    )

                                    if (currentRole == "CARETAKER" || currentRole == "LANDLORD" || currentRole == "ADMIN") {
                                        if (req.status == "PENDING") {
                                            Button(
                                                onClick = {
                                                    viewModel.assignCaretaker(req.id, currentUser?.email ?: "caretaker@nyumbahub.co.ke")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp).testTag("assign_caretaker_btn")
                                            ) {
                                                Text("Dispatch Job to Me", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (req.status == "IN_PROGRESS" && (req.assignedCaretakerEmail == currentUser?.email || currentRole == "LANDLORD")) {
                                            Button(
                                                onClick = { selectedRequestToProgress = req },
                                                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp).testTag("resolve_defect_btn")
                                            ) {
                                                Text("Complete Repair Job", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog for lodging a request (Tenant)
        if (showLodgeDialog) {
            val tenantUnitsList = units.filter { it.currentTenantId == currentUser?.email }
            val unitAssigned = tenantUnitsList.firstOrNull() ?: units.firstOrNull() // fallback

            AlertDialog(
                onDismissRequest = { showLodgeDialog = false },
                containerColor = SlateCard,
                title = { Text("Log New Repair Service Job", color = PureWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = rTitle,
                            onValueChange = { rTitle = it },
                            label = { Text("Title (Issue headline)") },
                            colors = loginInputColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("lodge_m_title")
                        )

                        OutlinedTextField(
                            value = rDesc,
                            onValueChange = { rDesc = it },
                            label = { Text("Description (Details of defect)") },
                            colors = loginInputColors(),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth().testTag("lodge_m_desc")
                        )

                        // Category selector
                        Column {
                            Text("Service Specialization Category", color = MutedText, fontSize = 11.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("PLUMBING", "ELECTRICAL", "GENERAL").forEach { cat ->
                                    FilterChip(
                                        selected = rCategory == cat,
                                        onClick = { rCategory = cat },
                                        label = { Text(cat, fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MpesaGreen.copy(alpha = 0.2f),
                                            containerColor = SlateBackground,
                                            selectedLabelColor = MpesaGreen,
                                            labelColor = MutedText
                                        )
                                    )
                                }
                            }
                        }

                        // Urgency selector
                        Column {
                            Text("Action Urgency priority", color = MutedText, fontSize = 11.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("LOW", "MEDIUM", "HIGH", "EMERGENCY").forEach { urg ->
                                    FilterChip(
                                        selected = rUrgency == urg,
                                        onClick = { rUrgency = urg },
                                        label = { Text(urg, fontSize = 9.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MpesaGreen.copy(alpha = 0.2f),
                                            containerColor = SlateBackground,
                                            selectedLabelColor = MpesaGreen,
                                            labelColor = MutedText
                                        )
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rTitle.isNotEmpty() && rDesc.isNotEmpty()) {
                                viewModel.lodgeMaintenance(
                                    title = rTitle,
                                    description = rDesc,
                                    category = rCategory,
                                    urgency = rUrgency,
                                    unitId = unitAssigned?.id ?: 1,
                                    propertyId = unitAssigned?.propertyId ?: 1
                                )
                                showLodgeDialog = false
                                rTitle = ""
                                rDesc = ""
                                rCategory = "PLUMBING"
                                rUrgency = "MEDIUM"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                    ) {
                        Text(" Lodge Ticket", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLodgeDialog = false }) {
                        Text("Cancel", color = MutedText)
                    }
                }
            )
        }

        // Dialog for resolving/completing a request (Caretaker)
        if (selectedRequestToProgress != null) {
            val activeReq = selectedRequestToProgress!!
            AlertDialog(
                onDismissRequest = { selectedRequestToProgress = null },
                containerColor = SlateCard,
                title = { Text("Complete Maintenance Repair", color = PureWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Describe actions taken to clear defect in unit:", color = MutedText)
                        OutlinedTextField(
                            value = progressComments,
                            onValueChange = { progressComments = it },
                            label = { Text("Resolution Actions Note") },
                            colors = loginInputColors(),
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("resolve_comments_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (progressComments.isNotEmpty()) {
                                viewModel.resolveMaintenance(activeReq.id, progressComments)
                                selectedRequestToProgress = null
                                progressComments = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                    ) {
                        Text("Resolve & Close Job", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedRequestToProgress = null }) {
                        Text("Cancel", color = MutedText)
                    }
                }
            )
        }

        // Detailed Request History dialog
        if (selectedRequestForDetails != null) {
            val d = selectedRequestForDetails!!
            AlertDialog(
                onDismissRequest = { selectedRequestForDetails = null },
                containerColor = SlateCard,
                title = { Text("Detailed Defect Log", color = WarmGold, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Title/Issue", color = MutedText, fontSize = 10.sp)
                        Text(d.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        Text("Description Details", color = MutedText, fontSize = 10.sp)
                        Text(d.description, color = PureWhite, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = SlateBorder)
                        Spacer(modifier = Modifier.height(6.dp))

                        Text("Workflow History Logs", color = WarmGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SlateBackground)
                                .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = d.notesLogs,
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }

                        if (d.completedAt.isNotEmpty()) {
                            Text("Completion Confirmed", color = MutedText, fontSize = 10.sp)
                            Text(d.completedAt, color = MpesaGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { selectedRequestForDetails = null }, colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)) {
                        Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun MaintenanceBlockCard(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        border = BorderStroke(1.dp, SlateBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = MutedText, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$count Tasks", color = if (count > 0) color else PureWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
