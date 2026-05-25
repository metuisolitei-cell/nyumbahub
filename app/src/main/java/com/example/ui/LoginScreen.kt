package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onLoginSuccess: () -> Unit
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("LANDLORD") } // LANDLORD, TENANT, CARETAKER, ADMIN
    var agencyName by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Brand Logo / Icon
            Card(
                colors = CardDefaults.cardColors(containerColor = MpesaGreen.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HomeWork,
                        contentDescription = "NyumbaHub App Logo",
                        tint = MpesaGreen,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NyumbaHub",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
            )

            Text(
                text = "Smart Kenyan Property Management System",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MutedText,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = AssistChipDefaults.assistChipBorder(borderColor = SlateBorder, enabled = true),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Account Authenticate",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = PureWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CriticalRed.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = CriticalRed,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (successMessage.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MpesaGreen.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = successMessage,
                                color = MpesaGreen,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Mmuted) },
                            colors = loginInputColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("signup_name_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number (+254...)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Mmuted) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = loginInputColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("signup_phone_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Role Selector for Signup
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Register Role profile",
                                color = MutedText,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("LANDLORD", "TENANT", "CARETAKER").forEach { r ->
                                    FilterChip(
                                        selected = role == r,
                                        onClick = { role = r },
                                        label = { Text(r, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MpesaGreen.copy(alpha = 0.2f),
                                            selectedLabelColor = MpesaGreen,
                                            containerColor = SlateBackground,
                                            labelColor = MutedText
                                        )
                                    )
                                }
                            }
                        }

                        if (role == "LANDLORD") {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = agencyName,
                                onValueChange = { agencyName = it },
                                label = { Text("Agency / Company Name") },
                                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = Mmuted) },
                                colors = loginInputColors(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("signup_agency_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Mmuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { pin = it },
                        label = { Text("Passcode PIN") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Mmuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = loginInputColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_pin_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            errorMessage = ""
                            successMessage = ""
                            if (email.isEmpty() || pin.isEmpty()) {
                                errorMessage = "Please enter both Email and PIN."
                                return@Button
                            }

                            if (isSignUp) {
                                if (name.isEmpty() || phone.isEmpty()) {
                                    errorMessage = "Please enter Name and Phone."
                                    return@Button
                                }
                                viewModel.signup(email, name, phone, role, agencyName, pin)
                                successMessage = "Registration successful! Loading..."
                                onLoginSuccess()
                            } else {
                                viewModel.login(email, pin) { success, msg ->
                                    if (success) {
                                        successMessage = msg
                                        onLoginSuccess()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MpesaGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_button")
                    ) {
                        Text(
                            text = if (isSignUp) "Register System Account" else "Sign In",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isSignUp) "Already have an account? Sign In" else "Create a new Manager/Tenant Account",
                        color = MpesaGreen,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier
                            .clickable { isSignUp = !isSignUp }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DEMO SHORTCUT PRESETS (Absolutely spectacular for testing!)
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateCard),
                shape = RoundedCornerShape(16.dp),
                border = AssistChipDefaults.assistChipBorder(borderColor = SlateBorder, enabled = true),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Demo Sandbox Quick Access",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = WarmGold),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetDemoLoginRow(
                            name = "James Kamau (Landlord Premium)",
                            desc = "View property graphs, send bulletins & inspect balances",
                            icon = Icons.Default.Business,
                            color = MpesaGreen
                        ) {
                            email = "landlord@nyumbahub.co.ke"
                            pin = "1234"
                            isSignUp = false
                            viewModel.login(email, pin) { _, _ -> onLoginSuccess() }
                        }

                        PresetDemoLoginRow(
                            name = "Amani Owino (Tenant / Rent Bills)",
                            desc = "Lodge leaking sink maintenance, pay Kshs 65,000 rent",
                            icon = Icons.Default.Person,
                            color = SecondaryAqua
                        ) {
                            email = "tenant@nyumbahub.co.ke"
                            pin = "1234"
                            isSignUp = false
                            viewModel.login(email, pin) { _, _ -> onLoginSuccess() }
                        }

                        PresetDemoLoginRow(
                            name = "Mwangi Ndegwa (Caretaker Staff)",
                            desc = "Receive repair jobs, change statuses, inspect units",
                            icon = Icons.Default.Construction,
                            color = WarmGold
                        ) {
                            email = "caretaker@nyumbahub.co.ke"
                            pin = "1234"
                            isSignUp = false
                            viewModel.login(email, pin) { _, _ -> onLoginSuccess() }
                        }

                        PresetDemoLoginRow(
                            name = "NyumbaHub Administrator",
                            desc = "Audit logs tracker, subscription licensing counters",
                            icon = Icons.Default.AdminPanelSettings,
                            color = CriticalRed
                        ) {
                            email = "admin@nyumbahub.co.ke"
                            pin = "1234"
                            isSignUp = false
                            viewModel.login(email, pin) { _, _ -> onLoginSuccess() }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun PresetDemoLoginRow(
    name: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SlateBackground)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(name, color = PureWhite, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(desc, color = MutedText, fontSize = 10.sp)
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MutedText,
            modifier = Modifier.size(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun loginInputColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = PureWhite,
    unfocusedTextColor = PureWhite,
    focusedBorderColor = MpesaGreen,
    unfocusedBorderColor = SlateBorder,
    focusedLabelColor = MpesaGreen,
    unfocusedLabelColor = MutedText,
    focusedLeadingIconColor = MpesaGreen,
    unfocusedLeadingIconColor = MutedText
)

val Mmuted = MutedText
val Mwhite = PureWhite
