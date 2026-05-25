package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    // Session State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentRole = MutableStateFlow<String>("LANDLORD") // Default role for dashboard exploration
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Interactive M-Pesa Simulation State
    private val _mpesaSimulationState = MutableStateFlow<MpesaSimulation?>(null)
    val mpesaSimulationState: StateFlow<MpesaSimulation?> = _mpesaSimulationState.asStateFlow()

    // Simulated Banner Notification State
    private val _simulatedNotification = MutableStateFlow<SimulatedNotification?>(null)
    val simulatedNotification: StateFlow<SimulatedNotification?> = _simulatedNotification.asStateFlow()

    // Navigation UI States
    private val _selectedPropertyId = MutableStateFlow<Long?>(null)
    val selectedPropertyId: StateFlow<Long?> = _selectedPropertyId.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(
            userDao = db.userDao(),
            propertyDao = db.propertyDao(),
            unitDao = db.unitDao(),
            tenantDao = db.tenantDao(),
            paymentDao = db.paymentDao(),
            maintenanceDao = db.maintenanceDao(),
            announcementDao = db.announcementDao(),
            auditLogDao = db.auditLogDao()
        )

        // Prepopulate asynchronously
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            // Automagic login/session for ease of initial demo
            val defaultUser = repository.getUserByEmail("landlord@nyumbahub.co.ke")
            _currentUser.value = defaultUser
            _currentRole.value = defaultUser?.role ?: "LANDLORD"
        }
    }

    // Live Flow Collections from Repository
    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val properties: StateFlow<List<PropertyEntity>> = repository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val units: StateFlow<List<UnitEntity>> = repository.allUnits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tenants: StateFlow<List<TenantEntity>> = repository.allTenants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenanceRequests: StateFlow<List<MaintenanceEntity>> = repository.allMaintenance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter flows dynamically based on selected states
    val activePropertyUnits: Flow<List<UnitEntity>> = selectedPropertyId.flatMapLatest { id ->
        if (id != null) repository.getUnitsForProperty(id) else flowOf(emptyList())
    }

    // Authentication Functions
    fun login(email: String, pin: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email)
            if (user != null && user.pinHash == pin) {
                _currentUser.value = user
                _currentRole.value = user.role
                repository.logAction(user.email, "LOGIN", "Successfully logged in as ${user.name} (Role: ${user.role})")
                onResult(true, "Welcome back, ${user.name}!")
            } else {
                onResult(false, "Invalid credentials or email address.")
            }
        }
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            viewModelScope.launch {
                repository.logAction(user.email, "LOGOUT", "${user.name} logged out from the system.")
            }
        }
        _currentUser.value = null
        _selectedPropertyId.value = null
    }

    fun signup(email: String, name: String, phoneNumber: String, role: String, agencyName: String = "", pin: String = "1234") {
        viewModelScope.launch {
            val newUser = UserEntity(
                email = email,
                name = name,
                phoneNumber = phoneNumber,
                role = role,
                agencyName = agencyName,
                pinHash = pin
            )
            repository.insertUser(newUser)
            repository.logAction(email, "SIGN_UP", "Created new account with Role: $role")
            
            // Auto login as newly signed up user
            _currentUser.value = newUser
            _currentRole.value = role

            // If signup as tenant, let's also register a dummy tenant schema
            if (role == "TENANT") {
                repository.insertTenant(TenantEntity(
                    email = email,
                    name = name,
                    phoneNumber = phoneNumber,
                    nationalId = "AUTO-GEN-${(1000..9999).random()}"
                ))
            }
        }
    }

    fun switchRole(role: String) {
        _currentRole.value = role
        // Keep session aligned if we can find a matching pre-populated user for the design flow!
        viewModelScope.launch {
            val matchedEmail = when (role) {
                "LANDLORD" -> "landlord@nyumbahub.co.ke"
                "TENANT" -> "tenant@nyumbahub.co.ke"
                "CARETAKER" -> "caretaker@nyumbahub.co.ke"
                "ADMIN" -> "admin@nyumbahub.co.ke"
                else -> ""
            }
            if (matchedEmail.isNotEmpty()) {
                val user = repository.getUserByEmail(matchedEmail)
                if (user != null) {
                    _currentUser.value = user
                }
            }
        }
    }

    fun setSelectedProperty(id: Long?) {
        _selectedPropertyId.value = id
    }

    // Property Operations
    fun removeProperty(property: PropertyEntity) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.deleteProperty(property)
                repository.logAction(user.email, "DELETE_PROPERTY", "Deleted property: ${property.name}")
            }
        }
    }

    fun createProperty(name: String, address: String, type: String, description: String, totalUnits: Int = 0) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                val newProp = PropertyEntity(
                    name = name,
                    address = address,
                    type = type,
                    description = description,
                    totalUnits = totalUnits,
                    vacantUnits = totalUnits
                )
                val newId = repository.insertProperty(newProp)
                
                // Automatically generate empty units under it for administrative ease
                for (i in 1..totalUnits) {
                    repository.insertUnit(UnitEntity(
                        propertyId = newId,
                        unitNumber = "A-0$i",
                        status = "VACANT",
                        rentAmount = when (type) {
                            "COMMERCIAL" -> 85000.0
                            "PLOT" -> 15000.0
                            else -> 35000.0
                        }
                    ))
                }
                
                repository.logAction(user.email, "CREATE_PROPERTY", "Added property: $name with $totalUnits vacancies.")
            }
        }
    }

    // Unit & Tenant Coordination
    fun removeUnitFromProperty(unit: UnitEntity) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.deleteUnit(unit)
                repository.logAction(user.email, "DELETE_UNIT", "Removed unit ${unit.unitNumber} from property id ${unit.propertyId}")
                
                // Recalculate property unit counts
                updatePropertyUnitCounters(unit.propertyId)
            }
        }
    }

    fun createUnitInProperty(propertyId: Long, unitNumber: String, rentAmount: Double) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.insertUnit(UnitEntity(
                    propertyId = propertyId,
                    unitNumber = unitNumber,
                    status = "VACANT",
                    rentAmount = rentAmount
                ))
                repository.logAction(user.email, "CREATE_UNIT", "Created Unit $unitNumber under Property $propertyId")
                
                // Re-calculate property counters
                updatePropertyUnitCounters(propertyId)
            }
        }
    }

    fun assignTenant(
        unitId: Long,
        email: String,
        name: String,
        phone: String,
        nationalId: String,
        leaseStart: String,
        leaseEnd: String,
        emergencyName: String,
        emergencyPhone: String
    ) {
        viewModelScope.launch {
            val activeUser = _currentUser.value ?: return@launch
            
            // 1. Create Tenant Entity details
            val tenant = TenantEntity(
                email = email,
                name = name,
                phoneNumber = phone,
                nationalId = nationalId,
                leaseStart = leaseStart,
                leaseEnd = leaseEnd,
                assignedUnitId = unitId,
                emergencyContactName = emergencyName,
                emergencyContactPhone = emergencyPhone
            )
            repository.insertTenant(tenant)

            // 2. Insert User entity for signin credentials if it doesn't exist
            var existingUser = repository.getUserByEmail(email)
            if (existingUser == null) {
                existingUser = UserEntity(
                    email = email,
                    name = name,
                    phoneNumber = phone,
                    role = "TENANT"
                )
                repository.insertUser(existingUser)
            }

            // 3. Mark Unit as occupied & set currentTenantId
            val unit = repository.getUnitById(unitId)
            if (unit != null) {
                val updatedUnit = unit.copy(status = "OCCUPIED", currentTenantId = email)
                repository.updateUnit(updatedUnit)
                
                // Auto generate initial rent invoice for the new tenant
                val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                val monthStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                repository.insertPayment(PaymentEntity(
                    tenantEmail = email,
                    unitId = unitId,
                    propertyId = unit.propertyId,
                    invoiceMonth = monthStr,
                    originalAmount = unit.rentAmount,
                    amountPaid = 0.0,
                    balanceDue = unit.rentAmount,
                    billingDate = dateStr,
                    status = "PENDING"
                ))

                // Re-calculate property counts
                updatePropertyUnitCounters(unit.propertyId)
            }

            repository.logAction(activeUser.email, "TENANT_ASSIGN", "Assigned tenant $name to Unit ID $unitId")
            triggerNotificationSimulation(
                title = "Welcome to NyumbaHub!",
                message = "Hello $name, your rent invoice for unit is generated. Pay seamlessly via Kenyan M-Pesa STK push. Contact landlord James.",
                channel = "WHATSAPP"
            )
        }
    }

    fun evacuateTenant(unitId: Long) {
        viewModelScope.launch {
            val activeUser = _currentUser.value ?: return@launch
            val unit = repository.getUnitById(unitId)
            if (unit != null) {
                val tenantEmail = unit.currentTenantId
                if (tenantEmail.isNotEmpty()) {
                    val tenant = repository.getTenantByEmail(tenantEmail)
                    if (tenant != null) {
                        repository.deleteTenant(tenant)
                    }
                }
                
                val updatedUnit = unit.copy(status = "VACANT", currentTenantId = "")
                repository.updateUnit(updatedUnit)
                
                updatePropertyUnitCounters(unit.propertyId)
                repository.logAction(activeUser.email, "TENANT_EVACUATE", "Unassigned/removed tenant from Unit: ${unit.unitNumber}")
            }
        }
    }

    private suspend fun updatePropertyUnitCounters(propertyId: Long) {
        val property = repository.getPropertyById(propertyId) ?: return
        val unitsList = repository.getUnitsForProperty(propertyId).firstOrNull() ?: emptyList()
        val total = unitsList.size
        val vacant = unitsList.count { it.status == "VACANT" }
        repository.updateProperty(property.copy(totalUnits = total, vacantUnits = vacant))
    }

    // Rent, Bills, Invoicing & M-Pesa Simulations
    fun generateInvoicesForAll() {
        viewModelScope.launch {
            val currentList = units.value
            val billingDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val monthStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
            var count = 0
            
            for (unit in currentList) {
                if (unit.status == "OCCUPIED" && unit.currentTenantId.isNotEmpty()) {
                    repository.insertPayment(PaymentEntity(
                        tenantEmail = unit.currentTenantId,
                        unitId = unit.id,
                        propertyId = unit.propertyId,
                        invoiceMonth = monthStr,
                        originalAmount = unit.rentAmount,
                        amountPaid = 0.0,
                        balanceDue = unit.rentAmount,
                        billingDate = billingDateStr,
                        status = "PENDING"
                    ))
                    count++
                }
            }
            if (count > 0) {
                _currentUser.value?.email?.let {
                    repository.logAction(it, "BULK_INVOICING", "Generated $count rent invoices electronically for the month of $monthStr.")
                }
                triggerNotificationSimulation(
                    title = "Monthly Invoicing Completed",
                    message = "SMS announcements sent in bulk to $count tenants regarding their new rents for $monthStr.",
                    channel = "SMS"
                )
            }
        }
    }

    fun initiateMpesaPaymentFlow(paymentId: Long, phoneNumber: String, amount: Double) {
        _mpesaSimulationState.value = MpesaSimulation(
            paymentId = paymentId,
            phoneNumber = phoneNumber,
            amount = amount,
            status = MpesaStatus.PROMPTED
        )
    }

    fun submitMpesaPin(pin: String) {
        val state = _mpesaSimulationState.value ?: return
        viewModelScope.launch {
            // Put status into loading
            _mpesaSimulationState.value = state.copy(status = MpesaStatus.PROCESSING)
            kotlinx.coroutines.delay(2000) // Delay to mimic M-Pesa network latency

            // Now update the payment record as PAID!
            val pList = payments.value
            val match = pList.firstOrNull { it.id == state.paymentId }
            if (match != null) {
                val receiptNo = "MPESA-${generateMpesaReceiptCode()}"
                val currentDateStr = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val updatedMatch = match.copy(
                    amountPaid = match.originalAmount,
                    balanceDue = 0.0,
                    paidDate = currentDateStr,
                    paymentMethod = "M-PESA",
                    status = "PAID",
                    receiptNumber = receiptNo
                )
                repository.updatePayment(updatedMatch)
                repository.logAction(match.tenantEmail, "MPESA_STK_SUCCESS", "Successfully paid rent bill of Kshs ${match.originalAmount} using M-Pesa. Receipt: $receiptNo")
                
                // Pre-fill simulation status as completed
                _mpesaSimulationState.value = state.copy(status = MpesaStatus.SUCCESS, receiptNumber = receiptNo)
                
                triggerNotificationSimulation(
                    title = "Payment Confirmed!",
                    message = "Simulated SMS: Confirmed! Ksh ${match.originalAmount} received for Rent by James Kamau. Ref: $receiptNo. Download receipt inside portal.",
                    channel = "SMS"
                )
            } else {
                _mpesaSimulationState.value = state.copy(status = MpesaStatus.FAILED)
            }
        }
    }

    fun closeMpesaSimulation() {
        _mpesaSimulationState.value = null
    }

    private fun generateMpesaReceiptCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..10).map { chars.random() }.joinToString("")
    }

    // Maintenance request Coordination
    fun lodgeMaintenance(title: String, description: String, category: String, urgency: String, unitId: Long, propertyId: Long) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val newReq = MaintenanceEntity(
                tenantEmail = user.email,
                unitId = unitId,
                propertyId = propertyId,
                title = title,
                description = description,
                category = category,
                urgency = urgency,
                status = "PENDING",
                createdAt = dateStr
            )
            repository.insertMaintenance(newReq)
            repository.logAction(user.email, "CREATE_MAINTENANCE", "Lodged maintenance ticket: $title")
            triggerNotificationSimulation(
                title = "Service Ticket Received",
                message = "Caretaker Mwangi Ndegwa has been notified about $title. Track status live in app.",
                channel = "IN_APP"
            )
        }
    }

    fun assignCaretaker(maintenanceId: Long, caretakerEmail: String) {
        viewModelScope.launch {
            val activeUser = _currentUser.value ?: return@launch
            val list = maintenanceRequests.value
            val match = list.firstOrNull { it.id == maintenanceId }
            if (match != null) {
                val updated = match.copy(
                    assignedCaretakerEmail = caretakerEmail,
                    status = "IN_PROGRESS",
                    notesLogs = match.notesLogs + "\n- Assigned to caretaker $caretakerEmail."
                )
                repository.updateMaintenance(updated)
                repository.logAction(activeUser.email, "MAINTENANCE_ASSIGN", "Assigned caretaker $caretakerEmail to Maintenance Ticket #${maintenanceId}")
            }
        }
    }

    fun resolveMaintenance(maintenanceId: Long, comments: String) {
        viewModelScope.launch {
            val activeUser = _currentUser.value ?: return@launch
            val list = maintenanceRequests.value
            val match = list.firstOrNull { it.id == maintenanceId }
            if (match != null) {
                val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
                val updated = match.copy(
                    status = "COMPLETED",
                    completedAt = dateStr,
                    notesLogs = match.notesLogs + "\n- Resolved: $comments \n- Completed on $dateStr."
                )
                repository.updateMaintenance(updated)
                repository.logAction(activeUser.email, "MAINTENANCE_RESOLVED", "Completed Maintenance Ticket #${maintenanceId}: $comments")
            }
        }
    }

    fun sendAnnouncement(propertyId: Long, title: String, message: String, method: String) {
        viewModelScope.launch {
            val activeUser = _currentUser.value ?: return@launch
            val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            repository.insertAnnouncement(AnnouncementEntity(
                senderEmail = activeUser.email,
                propertyId = propertyId,
                title = title,
                message = message,
                sentVia = method,
                sentTimestamp = dateStr
            ))
            repository.logAction(activeUser.email, "SEND_ANNOUNCEMENT", "Published announcement: $title to Property ID $propertyId.")
            
            triggerNotificationSimulation(
                title = "Announcement Shared!",
                message = "Broadcasted '$title' to property tenants via $method.",
                channel = when(method) {
                    "WHATSAPP" -> "WHATSAPP"
                    "SMS" -> "SMS"
                    else -> "IN_APP"
                }
            )
        }
    }

    // Help simulate visual feedback
    private fun triggerNotificationSimulation(title: String, message: String, channel: String) {
        _simulatedNotification.value = SimulatedNotification(
            title = title,
            message = message,
            channel = channel
        )
    }

    fun dismissNotificationSimulation() {
        _simulatedNotification.value = null
    }
}

// Support definitions for simulated items
data class SimulatedNotification(
    val title: String,
    val message: String,
    val channel: String // WHATSAPP, SMS, IN_APP
)

data class MpesaSimulation(
    val paymentId: Long,
    val phoneNumber: String,
    val amount: Double,
    val status: MpesaStatus,
    val receiptNumber: String = ""
)

enum class MpesaStatus {
    PROMPTED,
    PROCESSING,
    SUCCESS,
    FAILED
}
