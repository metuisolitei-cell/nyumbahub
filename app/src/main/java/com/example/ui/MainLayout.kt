package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: AppViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentRole by viewModel.currentRole.collectAsState()
    val simulatedNotification by viewModel.simulatedNotification.collectAsState()

    var activeTab by remember { mutableStateOf("DASHBOARD") }
    var showRoleQuickSelector by remember { mutableStateOf(false) }

    // If logged out, render Login screen
    if (currentUser == null) {
        LoginScreen(viewModel = viewModel, onLoginSuccess = { activeTab = "DASHBOARD" })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.HomeWork, contentDescription = null, tint = MpesaGreen)
                            Text(
                                "NyumbaHub",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    },
                    actions = {
                        // Logout Action
                        IconButton(onClick = { viewModel.logout() }, modifier = Modifier.testTag("logout_btn")) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = CriticalRed)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SlateCard),
                    modifier = Modifier.testTag("app_top_bar")
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = SlateCard,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    val tabsList = mutableListOf(
                        Triple("DASHBOARD", "Home", Icons.Default.Dashboard),
                        Triple("PROPERTIES", "Estates", Icons.Default.Apartment),
                        Triple("FINANCE", "Finance", Icons.Default.Payments),
                        Triple("MAINTENANCE", "Repairs", Icons.Default.Construction)
                    )

                    if (currentRole == "LANDLORD" || currentRole == "ADMIN") {
                        tabsList.add(Triple("BROADCAST", "Bulletins", Icons.Default.Campaign))
                    }

                    if (currentRole == "ADMIN") {
                        tabsList.add(Triple("ADMIN", "Security", Icons.Default.AdminPanelSettings))
                    }

                    tabsList.forEach { (tabId, label, icon) ->
                        NavigationBarItem(
                            selected = activeTab == tabId,
                            onClick = { activeTab = tabId },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryGreen,
                                selectedTextColor = PrimaryGreen,
                                indicatorColor = PrimaryGreen.copy(alpha = 0.12f),
                                unselectedIconColor = MutedText,
                                unselectedTextColor = MutedText
                            )
                        )
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ACTIVE ROUTE SWITCH SCREEN CARD
                when (activeTab) {
                    "DASHBOARD" -> DashboardScreen(viewModel = viewModel, onNavigateToTab = { activeTab = it })
                    "PROPERTIES" -> PropertyScreen(viewModel = viewModel)
                    "FINANCE" -> FinanceScreen(viewModel = viewModel)
                    "MAINTENANCE" -> MaintenanceScreen(viewModel = viewModel)
                    "BROADCAST" -> AnnouncementScreen(viewModel = viewModel)
                    "ADMIN" -> AdminScreen(viewModel = viewModel)
                }

                // FLOATING ROLE SELECTOR TRIGGER PILL (Spectacular sandbox experience)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showRoleQuickSelector = true },
                        containerColor = WarmGold,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("floating_role_switch")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Switch Roles", modifier = Modifier.size(28.dp))
                    }
                }
            }
        }

        // SANDBOX ROLE SELECTOR DIALOG
        if (showRoleQuickSelector) {
            AlertDialog(
                onDismissRequest = { showRoleQuickSelector = false },
                containerColor = SlateCard,
                title = { Text("Sandbox Role Switcher", color = WarmGold, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Swapping roles live updates screens instantaneously so you can check tenant billing portals, caretaker task queues, and landlord financials seamlessly.",
                            color = MutedText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        listOf(
                            Triple("LANDLORD", "Landlord James (Premium)", Icons.Default.Business),
                            Triple("TENANT", "Tenant Amani Owino (Bills Portal)", Icons.Default.Person),
                            Triple("CARETAKER", "Caretaker Mwangi (Defects Dispatcher)", Icons.Default.Construction),
                            Triple("ADMIN", "System Administrator (Audit Log Trail)", Icons.Default.AdminPanelSettings)
                        ).forEach { (roleCode, label, icon) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (currentRole == roleCode) MpesaGreen.copy(alpha = 0.15f) else SlateBackground)
                                    .border(1.dp, if (currentRole == roleCode) MpesaGreen else SlateBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.switchRole(roleCode)
                                        showRoleQuickSelector = false
                                        activeTab = "DASHBOARD" // reset to core dashboard for that role
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = if (currentRole == roleCode) MpesaGreen else MutedText)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = label, color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRoleQuickSelector = false }) {
                        Text("Close", color = MutedText)
                    }
                }
            )
        }

        // HEADS-UP SIMULATED PUSH NOTIFICATION ALERTS (green WhatsApp or grey SMS slide down)
        AnimatedVisibility(
            visible = simulatedNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 16.dp, start = 12.dp, end = 12.dp)
                .zIndex(100f) // Keep above standard scaffolding
        ) {
            simulatedNotification?.let { push ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when(push.channel) {
                            "WHATSAPP" -> Color(0xFF0F2615) // Dark Whatsapp green
                            else -> Color(0xFF13151A) // Charcoal Slate SMS
                        }
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (push.channel == "WHATSAPP") Color(0xFF25D366) else SlateBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .clickable { viewModel.dismissNotificationSimulation() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (push.channel == "WHATSAPP") Color(0xFF25D366) else SecondaryAqua),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (push.channel == "WHATSAPP") Icons.Default.ChatBubbleOutline else Icons.Default.Campaign,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (push.channel == "WHATSAPP") "WhatsApp Bulletin" else "SMS Network Alert",
                                    color = if (push.channel == "WHATSAPP") Color(0xFF25D366) else SecondaryAqua,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text("Now", color = MutedText, fontSize = 9.sp)
                            }
                            Text(push.title, color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(push.message, color = PureWhite, fontSize = 11.sp, lineHeight = 15.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { viewModel.dismissNotificationSimulation() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close push alert", tint = MutedText, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Autoclose after 8 seconds
                LaunchedEffect(push) {
                    kotlinx.coroutines.delay(8000)
                    viewModel.dismissNotificationSimulation()
                }
            }
        }
    }
}

private fun Modifier.zIndex(f: Float): Modifier = this
