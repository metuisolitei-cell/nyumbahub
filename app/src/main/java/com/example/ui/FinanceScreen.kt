package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val units by viewModel.units.collectAsState()
    val properties by viewModel.properties.collectAsState()

    val mpesaSimState by viewModel.mpesaSimulationState.collectAsState()

    // Form states for manual M-Pesa triggers
    var inputPhone by remember { mutableStateOf("") }
    var selectedPaymentForMpesa by remember { mutableStateOf<PaymentEntity?>(null) }
    var showMpesaTriggerSheet by remember { mutableStateOf(false) }

    // State for viewing Receipt Details
    var selectedPaymentForReceipt by remember { mutableStateOf<PaymentEntity?>(null) }

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

    // Role filtering
    val activePayments = remember(payments, currentRole, currentUser) {
        if (currentRole == "TENANT" && currentUser != null) {
            payments.filter { it.tenantEmail == currentUser!!.email }
        } else {
            payments
        }
    }

    val totalIncome = activePayments.filter { it.status == "PAID" }.sumOf { it.amountPaid }
    val totalOutstanding = activePayments.filter { it.status == "PENDING" || it.status == "OVERDUE" }.sumOf { it.balanceDue }

    LaunchedEffect(currentUser) {
        if (currentUser != null && inputPhone.isEmpty()) {
            inputPhone = currentUser!!.phoneNumber
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
                        text = "Finance LEDGER Tracker",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    )
                    Text(
                        text = "Real-time rental invoices, M-Pesa receipts & collections",
                        style = MaterialTheme.typography.bodySmall.copy(color = MutedText)
                    )
                }
            }

            // Summary row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val incForm = formatPrice(totalIncome)
                val outForm = formatPrice(totalOutstanding)

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("COLLECTED REVENUE", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(incForm, color = MpesaGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Paid Invoices", color = MutedText, fontSize = 9.sp)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = SlateCard),
                    border = BorderStroke(1.dp, SlateBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ARREARS BALANCE", color = MutedText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(outForm, color = if (totalOutstanding > 0) CriticalRed else MutedText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Outstanding Rents", color = MutedText, fontSize = 9.sp)
                    }
                }
            }

            // Invoices title
            Text(
                text = "Invoices & Receipts History",
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (activePayments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Receipt, contentDescription = null, tint = MutedText, modifier = Modifier.size(52.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No billing invoices logged yet.", color = PureWhite, fontSize = 14.sp)
                        Text("Click Generate in command actions to trigger monthly invoicing.", color = MutedText, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().weight(1f)
                ) {
                    items(activePayments) { payment ->
                        val u = units.firstOrNull { it.id == payment.unitId }
                        val p = properties.firstOrNull { it.id == payment.propertyId }

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
                                    Column {
                                        Text(
                                            text = payment.invoiceMonth,
                                            color = PureWhite,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = "${p?.name ?: "Estate"} - ${u?.unitNumber ?: "Room"}",
                                            color = MutedText,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Status Badge
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (payment.status) {
                                                "PAID" -> MpesaGreen.copy(alpha = 0.15f)
                                                else -> AccentAmber.copy(alpha = 0.15f)
                                            }
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = payment.status,
                                            color = when (payment.status) {
                                                "PAID" -> MpesaGreen
                                                else -> AccentAmber
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text("Invoice Amount", color = MutedText, fontSize = 10.sp)
                                        Text("Ksh ${payment.originalAmount}", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    if (payment.status == "PENDING" && currentRole == "TENANT") {
                                        Button(
                                            onClick = {
                                                selectedPaymentForMpesa = payment
                                                showMpesaTriggerSheet = true
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(34.dp).testTag("lipa_mpesa_btn")
                                        ) {
                                            Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Lipa na M-Pesa", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (payment.status == "PAID") {
                                        TextButton(
                                            onClick = { selectedPaymentForReceipt = payment },
                                            modifier = Modifier.height(34.dp).testTag("view_receipt_btn")
                                        ) {
                                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp), tint = MpesaGreen)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("View Receipt", color = MpesaGreen, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // TRIGGER M-PESA POPUP DIALOG FOR PHONE NUMBER COLLECTION
        if (showMpesaTriggerSheet && selectedPaymentForMpesa != null) {
            val pay = selectedPaymentForMpesa!!
            AlertDialog(
                onDismissRequest = { showMpesaTriggerSheet = false },
                containerColor = SlateCard,
                title = { Text("Lipa Na M-Pesa Online", color = PureWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("You are paying a bill of Ksh ${pay.originalAmount} for ${pay.invoiceMonth} rent.", color = MutedText)
                        
                        OutlinedTextField(
                            value = inputPhone,
                            onValueChange = { inputPhone = it },
                            label = { Text("M-Pesa Mobile (+254...)") },
                            colors = loginInputColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("mpesa_trigger_phone")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputPhone.isNotEmpty()) {
                                showMpesaTriggerSheet = false
                                viewModel.initiateMpesaPaymentFlow(pay.id, inputPhone, pay.originalAmount)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                    ) {
                        Text("Trigger STK Push", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showMpesaTriggerSheet = false }) {
                        Text("Cancel", color = MutedText)
                    }
                }
            )
        }

        // MPESA STK SIMULATOR OVERLAY DIALOG (SIMULATES INTERACTIVE PHONE OVERLAY)
        if (mpesaSimState != null) {
            val sim = mpesaSimState!!
            var mpesaPin by remember { mutableStateOf("") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                if (sim.status == MpesaStatus.PROMPTED) {
                    // Actual Retro Safaricom STK Push Style Card!
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(300.dp)
                            .padding(16.dp)
                            .border(2.dp, Color.Gray, RoundedCornerShape(8.dp)),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "SIM TOOLKIT",
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Pay KES ${sim.amount} to NYUMBAHUB MANAGER?\n\nEnter 4-digit M-PESA PIN:",
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = mpesaPin,
                                onValueChange = { if (it.length <= 4) mpesaPin = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.width(160.dp).testTag("mpesa_pin_box"),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(onClick = { viewModel.closeMpesaSimulation() }) {
                                    Text("CANCEL", color = Color.DarkGray, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                                Button(
                                    onClick = {
                                        if (mpesaPin.length == 4) {
                                            viewModel.submitMpesaPin(mpesaPin)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.testTag("mpesa_pin_submit")
                                ) {
                                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                } else if (sim.status == MpesaStatus.PROCESSING) {
                    // Processing network transmission feedback
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.width(280.dp),
                        border = BorderStroke(1.dp, SlateBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MpesaGreen, modifier = Modifier.testTag("mpesa_loader"))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Safe Connection Secured...", color = PureWhite, fontWeight = FontWeight.Medium)
                            Text("Processing Safaricom network transaction loop...", color = MutedText, fontSize = 11.sp, textAlign = TextAlign.Center)
                        }
                    }
                } else if (sim.status == MpesaStatus.SUCCESS) {
                    // Successful result and Receipt presentation
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SlateCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .width(340.dp)
                            .padding(16.dp),
                        border = BorderStroke(1.dp, SlateBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MpesaGreen, modifier = Modifier.size(54.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Transaction Approved", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Rent Ledger cleared successfully with M-Pesa", color = MutedText, fontSize = 11.sp, textAlign = TextAlign.Center)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Visual Receipt docket box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SlateBackground)
                                    .border(1.dp, SlateBorder, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ReceiptRow(label = "Ref Receipt No:", value = sim.receiptNumber, color = WarmGold)
                                    ReceiptRow(label = "Cleared Amount:", value = "Ksh ${sim.amount}", color = MpesaGreen)
                                    ReceiptRow(label = "Mobile Channel:", value = sim.phoneNumber, color = PureWhite)
                                    ReceiptRow(label = "Status Code:", value = "Kshs 0.00 Bal", color = MpesaGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Receipt PDF downloaded successfully!", Toast.LENGTH_SHORT).show()
                                    viewModel.closeMpesaSimulation()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("download_receipt_action")
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download PDF Receipt", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // STAND-ALONE DETAILED RECEIPT MODAL (FOR PAST PAID INVOICES)
        if (selectedPaymentForReceipt != null) {
            val rec = selectedPaymentForReceipt!!
            AlertDialog(
                onDismissRequest = { selectedPaymentForReceipt = null },
                containerColor = SlateCard,
                title = { Text("Electronic Tax Invoice", color = WarmGold, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("NYUMBAHUB SYSTEMS LTD", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Text("Licensed Landlord Manager Ledger POS", color = MutedText, fontSize = 10.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = SlateBorder)
                        Spacer(modifier = Modifier.height(6.dp))

                        ReceiptDetailSection(title = "Billing Profile Details", details = listOf(
                            "Tenant Email" to rec.tenantEmail,
                            "Invoice Month" to rec.invoiceMonth,
                            "Issued Date" to rec.billingDate
                        ))

                        Spacer(modifier = Modifier.height(6.dp))

                        ReceiptDetailSection(title = "Clearing Transaction Record", details = listOf(
                            "Receipt Ref Code" to rec.receiptNumber,
                            "Paid Timestamp" to rec.paidDate,
                            "Paid Method" to rec.paymentMethod,
                            "Settled aggregate" to "Ksh ${rec.amountPaid}",
                            "Invoice Balance" to "Ksh ${rec.balanceDue}"
                        ))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Invoice invoice-${rec.id}.pdf stored to internal downloads.", Toast.LENGTH_SHORT).show()
                            selectedPaymentForReceipt = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export PDF Document", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedPaymentForReceipt = null }) {
                        Text("Close", color = MutedText)
                    }
                }
            )
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MutedText, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun ReceiptDetailSection(title: String, details: List<Pair<String, String>>) {
    Column {
        Text(text = title.uppercase(), color = WarmGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            details.forEach { (lbl, valStr) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = lbl, color = MutedText, fontSize = 10.sp)
                    Text(text = valStr, color = PureWhite, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
