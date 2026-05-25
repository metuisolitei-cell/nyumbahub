package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyScreen(viewModel: AppViewModel) {
    val currentRole by viewModel.currentRole.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val units by viewModel.units.collectAsState()
    val selectedPropertyId by viewModel.selectedPropertyId.collectAsState()

    var showAddPropertyDialog by remember { mutableStateOf(false) }
    val selectedProperty = remember(properties, selectedPropertyId) {
        properties.firstOrNull { it.id == selectedPropertyId }
    }

    // Forms for Property Creation
    var propName by remember { mutableStateOf("") }
    var propAddress by remember { mutableStateOf("") }
    var propType by remember { mutableStateOf("APARTMENT") }
    var propDesc by remember { mutableStateOf("") }
    var propUnits by remember { mutableStateOf("6") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        if (selectedProperty == null) {
            // LIST VIEW OF ALL PROPERTIES
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Real Estate Portfolio",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        )
                        Text(
                            text = "Manage estates, check units & assign tenants",
                            style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                        )
                    }

                    if (currentRole == "LANDLORD" || currentRole == "ADMIN") {
                        Button(
                            onClick = { showAddPropertyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_property_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", color = Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                if (properties.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Apartment, contentDescription = null, tint = MutedText, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No properties configured yet.", color = PureWhite, style = MaterialTheme.typography.titleMedium)
                            Text("Click Add at the top to configure your first estate.", color = MutedText)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(properties) { prop ->
                            val pUnits = units.filter { it.propertyId == prop.id }
                            PropertyItemCard(
                                property = prop,
                                unitCount = pUnits.size,
                                vacantCount = pUnits.count { it.status == "VACANT" },
                                onClick = {
                                    viewModel.setSelectedProperty(prop.id)
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // INDIVIDUAL PROPERTY DETAIL VIEW
            val prop = selectedProperty!!
            val propertyUnits = units.filter { it.propertyId == prop.id }

            PropertyDetailView(
                property = prop,
                units = propertyUnits,
                viewModel = viewModel,
                currentRole = currentRole,
                onBack = {
                    viewModel.setSelectedProperty(null)
                }
            )
        }

        // Add Property Dialog
        if (showAddPropertyDialog) {
            AlertDialog(
                onDismissRequest = { showAddPropertyDialog = false },
                containerColor = SlateCard,
                title = { Text("List New Estate Property", color = PureWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = propName,
                            onValueChange = { propName = it },
                            label = { Text("Estate / Building Name") },
                            colors = loginInputColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_prop_name")
                        )

                        OutlinedTextField(
                            value = propAddress,
                            onValueChange = { propAddress = it },
                            label = { Text("Physical Location / Address") },
                            colors = loginInputColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_prop_address")
                        )

                        // Selector
                        Column {
                            Text("Property Class Category", color = MutedText, fontSize = 11.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("APARTMENT", "SINGLE_UNIT", "COMMERCIAL", "PLOT").forEach { type ->
                                    FilterChip(
                                        selected = propType == type,
                                        onClick = { propType = type },
                                        label = { Text(type, fontSize = 9.sp) },
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

                        OutlinedTextField(
                            value = propDesc,
                            onValueChange = { propDesc = it },
                            label = { Text("Description (Amenities, features)") },
                            colors = loginInputColors(),
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = propUnits,
                            onValueChange = { propUnits = it },
                            label = { Text("Number of Units/Rooms to Auto-Generate") },
                            colors = loginInputColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_prop_units")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val unitsCount = propUnits.toIntOrNull() ?: 4
                            if (propName.isNotEmpty() && propAddress.isNotEmpty()) {
                                viewModel.createProperty(propName, propAddress, propType, propDesc, unitsCount)
                                showAddPropertyDialog = false
                                propName = ""
                                propAddress = ""
                                propDesc = ""
                                propUnits = "6"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                    ) {
                        Text("Save Estate", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddPropertyDialog = false }) {
                        Text("Cancel", color = MutedText)
                    }
                }
            )
        }
    }
}

@Composable
fun PropertyItemCard(
    property: PropertyEntity,
    unitCount: Int,
    vacantCount: Int,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SlateBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MpesaGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (property.type) {
                                "COMMERCIAL" -> Icons.Default.Business
                                "SINGLE_UNIT" -> Icons.Default.Home
                                "PLOT" -> Icons.Default.Landscape
                                else -> Icons.Default.Apartment
                            },
                            contentDescription = null,
                            tint = MpesaGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = property.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MutedText, modifier = Modifier.size(12.dp))
                            Text(
                                text = property.address,
                                style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                            )
                        }
                    }
                }

                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MutedText)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Details/Metrics preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PropInfoTag(label = "Type", value = property.type, color = SecondaryAqua)
                PropInfoTag(label = "Total Rooms", value = "$unitCount units", color = PureWhite)
                PropInfoTag(
                    label = "Vacancies",
                    value = if (vacantCount > 0) "$vacantCount Vacant" else "Fully Leased",
                    color = if (vacantCount > 0) MpesaGreen else WarmGold
                )
            }
        }
    }
}

@Composable
fun PropInfoTag(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SlateBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label + ":", color = MutedText, fontSize = 10.sp)
            Text(value, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// INDIVIDUAL PROPERTY DEEPER PROFILE
@Composable
fun PropertyDetailView(
    property: PropertyEntity,
    units: List<UnitEntity>,
    viewModel: AppViewModel,
    currentRole: String,
    onBack: () -> Unit
) {
    var showAssignTenantDialog by remember { mutableStateOf(false) }
    var showAddUnitDialog by remember { mutableStateOf(false) }
    var selectedUnitForAssign by remember { mutableStateOf<UnitEntity?>(null) }

    // Forms for Tenant Assignment
    var tenantName by remember { mutableStateOf("") }
    var tenantEmail by remember { mutableStateOf("") }
    var tenantPhone by remember { mutableStateOf("") }
    var tenantID by remember { mutableStateOf("") }
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }
    var leaseStart by remember { mutableStateOf("01-05-2026") }
    var leaseEnd by remember { mutableStateOf("30-04-2027") }

    // Forms for custom Unit additions
    var newUnitNo by remember { mutableStateOf("") }
    var newUnitRent by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back toolbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Go back", tint = PureWhite)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Estate Details",
                    style = MaterialTheme.typography.titleMedium.copy(color = MutedText)
                )
            }
        }

        // Estate Identity Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, SlateBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = property.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = property.address,
                                    color = MutedText,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (currentRole == "LANDLORD" || currentRole == "ADMIN") {
                            IconButton(onClick = { viewModel.removeProperty(property); onBack() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete property", tint = CriticalRed)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = property.description,
                        color = PureWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Simulated GPS Coordinates UI Block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SlateBackground)
                            .border(1.dp, SlateBorder, RoundedCornerShape(12.dp))
                    ) {
                        // Drawing simulated schematic map
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(color = MpesaGreen.copy(alpha = 0.15f), radius = 60.dp.toPx(), center = center)
                            drawCircle(color = MpesaGreen, radius = 5.dp.toPx(), center = center)
                            
                            // Mocking intersecting roads
                            drawLine(color = SlateBorder, start = Offset(0f, size.height*0.3f), end = Offset(size.width, size.height*0.4f), strokeWidth = 3.dp.toPx())
                            drawLine(color = SlateBorder, start = Offset(size.width*0.4f, 0f), end = Offset(size.width*0.5f, size.height), strokeWidth = 3.dp.toPx())
                        }
                        
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .align(Alignment.BottomStart)
                                .background(SlateCard.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                                .border(1.dp, SlateBorder, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Nairobi Central Map Plot",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "GPS Coordinate: ${property.latitude}° S, ${property.longitude}° E",
                                color = MpesaGreen,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }

        // Units Title Toolbar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lobby Units (${units.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                )

                if (currentRole == "LANDLORD" || currentRole == "ADMIN") {
                    TextButton(onClick = { showAddUnitDialog = true }) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Unit", color = MpesaGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Units Grid Listing
        if (units.isEmpty()) {
            item {
                Text(
                    text = "No private rooms set up. Click Add above.",
                    color = MutedText,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
        } else {
            items(units) { unitObj ->
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Default.DoorSliding, contentDescription = null, tint = PureWhite, modifier = Modifier.size(20.dp))
                                Text(
                                    text = unitObj.unitNumber,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            // OCCUPANCY STATE BADGE
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when (unitObj.status) {
                                        "OCCUPIED" -> MpesaGreen.copy(alpha = 0.15f)
                                        "UNDER_MAINTENANCE" -> CriticalRed.copy(alpha = 0.15f)
                                        else -> SecondaryAqua.copy(alpha = 0.15f)
                                    }
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = unitObj.status,
                                    color = when (unitObj.status) {
                                        "OCCUPIED" -> MpesaGreen
                                        "UNDER_MAINTENANCE" -> CriticalRed
                                        else -> SecondaryAqua
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Monthly Rent Amount", color = MutedText, fontSize = 11.sp)
                                Text("Ksh ${unitObj.rentAmount}", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            if (unitObj.status == "OCCUPIED") {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Leased Tenant", color = MutedText, fontSize = 10.sp)
                                    Text(unitObj.currentTenantId, color = MpesaGreen, fontSize = 12.sp, overflow = TextOverflow.Ellipsis, maxLines = 1)
                                }
                            }
                        }

                        // ACTIONS FOR MANAGEMENT BOARD
                        if (currentRole == "LANDLORD" || currentRole == "ADMIN" || currentRole == "CARETAKER") {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = SlateBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (unitObj.status == "VACANT") {
                                    Button(
                                        onClick = {
                                            selectedUnitForAssign = unitObj
                                            showAssignTenantDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                        modifier = Modifier.height(34.dp).testTag("assign_tenant_btn")
                                    ) {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Assign Tenant", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else if (unitObj.status == "OCCUPIED") {
                                    OutlinedButton(
                                        onClick = { viewModel.evacuateTenant(unitObj.id) },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CriticalRed),
                                        border = BorderStroke(1.dp, CriticalRed),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Evacuate / Unassign", fontSize = 11.sp)
                                    }
                                }

                                if (currentRole == "LANDLORD" || currentRole == "ADMIN") {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.removeUnitFromProperty(unitObj) },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = MutedText, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(64.dp)) }
    }

    // Assign Tenant Dialog
    if (showAssignTenantDialog && selectedUnitForAssign != null) {
        val targetedUnit = selectedUnitForAssign!!
        AlertDialog(
            onDismissRequest = { showAssignTenantDialog = false },
            containerColor = SlateCard,
            title = { Text("Assign Tenant to Unit ${targetedUnit.unitNumber}", color = PureWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tenantName,
                        onValueChange = { tenantName = it },
                        label = { Text("Tenant Full Name") },
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("assign_tenant_name")
                    )

                    OutlinedTextField(
                        value = tenantEmail,
                        onValueChange = { tenantEmail = it },
                        label = { Text("Email Address") },
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("assign_tenant_email")
                    )

                    OutlinedTextField(
                        value = tenantPhone,
                        onValueChange = { tenantPhone = it },
                        label = { Text("M-Pesa Mobile (+254...)") },
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("assign_tenant_phone")
                    )

                    OutlinedTextField(
                        value = tenantID,
                        onValueChange = { tenantID = it },
                        label = { Text("National ID / Passport Number") },
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("assign_tenant_id")
                    )

                    OutlinedTextField(
                        value = emergencyName,
                        onValueChange = { emergencyName = it },
                        label = { Text("Emergency Contact Name") },
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emergencyPhone,
                        onValueChange = { emergencyPhone = it },
                        label = { Text("Emergency Contact Phone") },
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tenantName.isNotEmpty() && tenantEmail.isNotEmpty() && tenantPhone.isNotEmpty()) {
                            viewModel.assignTenant(
                                unitId = targetedUnit.id,
                                email = tenantEmail,
                                name = tenantName,
                                phone = tenantPhone,
                                nationalId = tenantID,
                                leaseStart = leaseStart,
                                leaseEnd = leaseEnd,
                                emergencyName = emergencyName,
                                emergencyPhone = emergencyPhone
                            )
                            showAssignTenantDialog = false
                            // reset
                            tenantName = ""
                            tenantEmail = ""
                            tenantPhone = ""
                            tenantID = ""
                            emergencyName = ""
                            emergencyPhone = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) {
                    Text("Confirm Lease", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignTenantDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }

    // Add Unit Dialog
    if (showAddUnitDialog) {
        AlertDialog(
            onDismissRequest = { showAddUnitDialog = false },
            containerColor = SlateCard,
            title = { Text("Add New Unit Room", color = PureWhite, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newUnitNo,
                        onValueChange = { newUnitNo = it },
                        label = { Text("Unit Name / Room Number (e.g. Unit 302)") },
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_unit_number")
                    )

                    OutlinedTextField(
                        value = newUnitRent,
                        onValueChange = { newUnitRent = it },
                        label = { Text("Monthly Rent (KES)") },
                        colors = loginInputColors(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("add_unit_rent")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rentAmt = newUnitRent.toDoubleOrNull() ?: 25000.0
                        if (newUnitNo.isNotEmpty()) {
                            viewModel.createUnitInProperty(property.id, newUnitNo, rentAmt)
                            showAddUnitDialog = false
                            newUnitNo = ""
                            newUnitRent = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                ) {
                    Text("Create Unit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUnitDialog = false }) {
                    Text("Cancel", color = MutedText)
                }
            }
        )
    }
}

@Composable
fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: Int) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size.dp)
    )
}
