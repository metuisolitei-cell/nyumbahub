package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementScreen(viewModel: AppViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val properties by viewModel.properties.collectAsState()

    var showComposer by remember { mutableStateOf(false) }

    // Forms for Composer
    var aTitle by remember { mutableStateOf("") }
    var aMessage by remember { mutableStateOf("") }
    var selectedPropertyId by remember { mutableStateOf(0L) } // 0 = All Property Estates
    var selectedMethod by remember { mutableStateOf("BULK_ALL") } // BULK_ALL, SMS, WHATSAPP, IN_APP

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
                        text = "Broadcasting Hub",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    )
                    Text(
                        text = "Transmit bulk announcements to all properties via SMS or WhatsApp",
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                    )
                }

                if (currentRole == "LANDLORD" || currentRole == "ADMIN") {
                    Button(
                        onClick = { showComposer = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("open_composer_btn")
                    ) {
                        Icon(Icons.Default.AddComment, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compose", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            if (announcements.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = MutedText, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No bulk announcements published yet.", color = PureWhite, fontSize = 15.sp)
                        Text("Click Compose above to write a new notice warning.", color = MutedText, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(announcements) { ann ->
                        val targetProp = if (ann.propertyId == 0L) "All Estates" else {
                            properties.firstOrNull { it.id == ann.propertyId }?.name ?: "Specific Estate"
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateCard),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SlateBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = ann.title,
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "Directed to: $targetProp",
                                            color = Mmuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Platform Badge
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when(ann.sentVia) {
                                                "WHATSAPP" -> MpesaGreen.copy(alpha = 0.15f)
                                                "SMS" -> SecondaryAqua.copy(alpha = 0.15f)
                                                else -> WarmGold.copy(alpha = 0.15f)
                                            }
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = ann.sentVia,
                                            color = when(ann.sentVia) {
                                                "WHATSAPP" -> MpesaGreen
                                                "SMS" -> SecondaryAqua
                                                else -> WarmGold
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = ann.message,
                                    color = PureWhite,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = SlateBorder)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Author: ${ann.senderEmail}",
                                        color = MutedText,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = ann.sentTimestamp,
                                        color = MutedText,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // COMPOSE DIALOG
        if (showComposer) {
            AlertDialog(
                onDismissRequest = { showComposer = false },
                containerColor = SlateCard,
                title = { Text("Publish Announcement Bullet", color = PureWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = aTitle,
                            onValueChange = { aTitle = it },
                            label = { Text("Notice Header Title") },
                            colors = loginInputColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("composer_title")
                        )

                        OutlinedTextField(
                            value = aMessage,
                            onValueChange = { aMessage = it },
                            label = { Text("Message Body (Leads, Warnings...)") },
                            colors = loginInputColors(),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth().testTag("composer_message")
                        )

                        // Estate Target Dropdown Slider
                        Column {
                            Text("Target Estate Group", color = MutedText, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedPropertyId == 0L,
                                    onClick = { selectedPropertyId = 0L },
                                    label = { Text("All Estates", fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MpesaGreen.copy(alpha = 0.2f),
                                        containerColor = SlateBackground,
                                        selectedLabelColor = MpesaGreen,
                                        labelColor = MutedText
                                    )
                                )

                                properties.take(2).forEach { prop ->
                                    FilterChip(
                                        selected = selectedPropertyId == prop.id,
                                        onClick = { selectedPropertyId = prop.id },
                                        label = { Text(prop.name, fontSize = 9.sp, maxLines = 1) },
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

                        // Method selection
                        Column {
                            Text("Transmission Route Channel", color = MutedText, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("BULK_ALL", "SMS", "WHATSAPP", "IN_APP").forEach { route ->
                                    FilterChip(
                                        selected = selectedMethod == route,
                                        onClick = { selectedMethod = route },
                                        label = { Text(route, fontSize = 9.sp) },
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
                            if (aTitle.isNotEmpty() && aMessage.isNotEmpty()) {
                                viewModel.sendAnnouncement(selectedPropertyId, aTitle, aMessage, selectedMethod)
                                showComposer = false
                                aTitle = ""
                                aMessage = ""
                                selectedPropertyId = 0L
                                selectedMethod = "BULK_ALL"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                    ) {
                        Text("Broadcast Live", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showComposer = false }) {
                        Text("Cancel", color = MutedText)
                    }
                }
            )
        }
    }
}
