package com.example.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AppViewModel) {
    val users by viewModel.users.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var showLicensingSheet by remember { mutableStateOf(false) }
    var selectedLicensePlan by remember { mutableStateOf("Corporate Enterprise Hub") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title
        item {
            Column {
                Text(
                    text = "Admin operations console",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                )
                Text(
                    text = "Track sandbox accounts, audit logs trail, security permissions",
                    style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                )
            }
        }

        // LICENSING SECTOR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System License & Plan Status",
                        color = WarmGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Current Tier Plan", color = MutedText, fontSize = 11.sp)
                            Text(text = selectedLicensePlan, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Button(
                            onClick = { showLicensingSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change Plan", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // USER DIRECTORY SECTOR
        item {
            Text(
                text = "Registered Accounts Directory (${users.size})",
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(users) { usr ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (usr.role) {
                                    "ADMIN" -> CriticalRed.copy(alpha = 0.15f)
                                    "LANDLORD" -> MpesaGreen.copy(alpha = 0.15f)
                                    "CARETAKER" -> WarmGold.copy(alpha = 0.15f)
                                    else -> SecondaryAqua.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (usr.role) {
                                "ADMIN" -> Icons.Default.AdminPanelSettings
                                "LANDLORD" -> Icons.Default.Business
                                "CARETAKER" -> Icons.Default.Construction
                                else -> Icons.Default.Person
                            },
                            contentDescription = null,
                            tint = when (usr.role) {
                                "ADMIN" -> CriticalRed
                                "LANDLORD" -> MpesaGreen
                                "CARETAKER" -> WarmGold
                                else -> SecondaryAqua
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = usr.name,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${usr.email} | Mobile: ${usr.phoneNumber}",
                            color = MutedText,
                            fontSize = 11.sp
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateBackground),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = usr.role,
                            color = MutedText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // AUDIT LOGS TRAIL SECTOR
        item {
            Text(
                text = "System Audit Trail Logs (${auditLogs.size})",
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(auditLogs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, SlateBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when(log.action) {
                                            "LOGIN" -> MpesaGreen
                                            "MPESA_STK_SUCCESS" -> MpesaGreen
                                            "CREATE_PROPERTY" -> SecondaryAqua
                                            "TENANT_ASSIGN" -> WarmGold
                                            else -> PureWhite
                                        }
                                    )
                            )
                            Text(
                                text = log.action,
                                color = WarmGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = log.timestamp,
                            color = MutedText,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = log.details,
                        color = PureWhite,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Trigger Actor: ${log.actorEmail}",
                        color = MutedText,
                        fontSize = 9.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(64.dp)) }
    }

    // Changing licensing tier dialog
    if (showLicensingSheet) {
        AlertDialog(
            onDismissRequest = { showLicensingSheet = false },
            containerColor = SlateCard,
            title = { Text("Select System License Tier", color = PureWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Basic Starter Trial (Max 2 properties)",
                        "Professional Midclass Estate Hub (Max 15 properties)",
                        "Corporate Enterprise Hub (Unlimited & M-Pesa direct API)"
                    ).forEach { plan ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedLicensePlan == plan) MpesaGreen.copy(alpha = 0.15f) else SlateBackground)
                                .border(1.dp, if (selectedLicensePlan == plan) MpesaGreen else SlateBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedLicensePlan = plan }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLicensePlan == plan,
                                onClick = { selectedLicensePlan = plan },
                                colors = RadioButtonDefaults.colors(selectedColor = MpesaGreen, unselectedColor = MutedText)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = plan, color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showLicensingSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
