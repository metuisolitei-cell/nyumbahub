package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(
    private val userDao: UserDao,
    private val propertyDao: PropertyDao,
    private val unitDao: UnitDao,
    private val tenantDao: TenantDao,
    private val paymentDao: PaymentDao,
    private val maintenanceDao: MaintenanceDao,
    private val announcementDao: AnnouncementDao,
    private val auditLogDao: AuditLogDao
) {
    // Reactive Flows
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    val allProperties: Flow<List<PropertyEntity>> = propertyDao.getAllPropertiesFlow()
    val allUnits: Flow<List<UnitEntity>> = unitDao.getAllUnitsFlow()
    val allTenants: Flow<List<TenantEntity>> = tenantDao.getAllTenantsFlow()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPaymentsFlow()
    val allMaintenance: Flow<List<MaintenanceEntity>> = maintenanceDao.getAllMaintenanceFlow()
    val allAnnouncements: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncementsFlow()
    val allAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogsFlow()

    fun getUnitsForProperty(propertyId: Long): Flow<List<UnitEntity>> = unitDao.getUnitsForPropertyFlow(propertyId)
    fun getPaymentsForTenant(email: String): Flow<List<PaymentEntity>> = paymentDao.getPaymentsForTenantFlow(email)
    fun getPaymentsForProperty(propertyId: Long): Flow<List<PaymentEntity>> = paymentDao.getPaymentsForPropertyFlow(propertyId)
    fun getMaintenanceForTenant(email: String): Flow<List<MaintenanceEntity>> = maintenanceDao.getMaintenanceForTenantFlow(email)
    fun getMaintenanceForCaretaker(email: String): Flow<List<MaintenanceEntity>> = maintenanceDao.getMaintenanceForCaretakerFlow(email)

    // User Operations
    suspend fun getUserByEmail(email: String): UserEntity? = userDao.getUserByEmail(email)
    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    // Property Operations
    suspend fun getPropertyById(id: Long) = propertyDao.getPropertyById(id)
    suspend fun insertProperty(property: PropertyEntity) = propertyDao.insertProperty(property)
    suspend fun updateProperty(property: PropertyEntity) = propertyDao.updateProperty(property)
    suspend fun deleteProperty(property: PropertyEntity) = propertyDao.deleteProperty(property)

    // Unit Operations
    suspend fun getUnitById(id: Long) = unitDao.getUnitById(id)
    suspend fun insertUnit(unit: UnitEntity) = unitDao.insertUnit(unit)
    suspend fun updateUnit(unit: UnitEntity) = unitDao.updateUnit(unit)
    suspend fun deleteUnit(unit: UnitEntity) = unitDao.deleteUnit(unit)

    // Tenant Operations
    suspend fun getTenantByEmail(email: String) = tenantDao.getTenantByEmail(email)
    suspend fun insertTenant(tenant: TenantEntity) = tenantDao.insertTenant(tenant)
    suspend fun updateTenant(tenant: TenantEntity) = tenantDao.updateTenant(tenant)
    suspend fun deleteTenant(tenant: TenantEntity) = tenantDao.deleteTenant(tenant)

    // Payment Operations
    suspend fun insertPayment(payment: PaymentEntity) = paymentDao.insertPayment(payment)
    suspend fun updatePayment(payment: PaymentEntity) = paymentDao.updatePayment(payment)
    suspend fun deletePayment(payment: PaymentEntity) = paymentDao.deletePayment(payment)

    // Maintenance Operations
    suspend fun insertMaintenance(maintenance: MaintenanceEntity) = maintenanceDao.insertMaintenance(maintenance)
    suspend fun updateMaintenance(maintenance: MaintenanceEntity) = maintenanceDao.updateMaintenance(maintenance)
    suspend fun deleteMaintenance(maintenance: MaintenanceEntity) = maintenanceDao.deleteMaintenance(maintenance)

    // Announcement Details
    suspend fun insertAnnouncement(announcement: AnnouncementEntity) = announcementDao.insertAnnouncement(announcement)

    // Audit Log Details
    suspend fun logAction(email: String, action: String, details: String) {
        val date = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        auditLogDao.insertAuditLog(AuditLogEntity(actorEmail = email, action = action, details = details, timestamp = date))
    }

    // Prepopulate Sample Data on Initial Launch
    suspend fun prepopulateIfEmpty() {
        val anyUser = userDao.getUserByEmail("landlord@nyumbahub.co.ke")
        if (anyUser == null) {
            // 1. Pre-populate Users
            val landlord = UserEntity(
                email = "landlord@nyumbahub.co.ke",
                name = "James Kamau",
                phoneNumber = "+254712345678",
                role = "LANDLORD",
                agencyName = "Kamau Premium Holdings"
            )
            val tenant = UserEntity(
                email = "tenant@nyumbahub.co.ke",
                name = "Amani Owino",
                phoneNumber = "+254722111222",
                role = "TENANT"
            )
            val caretaker = UserEntity(
                email = "caretaker@nyumbahub.co.ke",
                name = "Mwangi Ndegwa",
                phoneNumber = "+254733444555",
                role = "CARETAKER"
            )
            val admin = UserEntity(
                email = "admin@nyumbahub.co.ke",
                name = "NyumbaHub Admin",
                phoneNumber = "+254799000111",
                role = "ADMIN"
            )

            insertUser(landlord)
            insertUser(tenant)
            insertUser(caretaker)
            insertUser(admin)

            // Other sample tenants for data richness
            insertUser(UserEntity(email = "mercy@example.com", name = "Mercy Jepkosgei", phoneNumber = "+254711223344", role = "TENANT"))
            insertUser(UserEntity(email = "kibet@example.com", name = "Kibet Bett", phoneNumber = "+254799887766", role = "TENANT"))

            // 2. Pre-populate Properties
            val propKilimaniId = propertyDao.insertProperty(PropertyEntity(
                name = "Gold Oasis Apartments",
                address = "Kilimani Road, Nairobi",
                type = "APARTMENT",
                description = "Luxury apartments in Kilimani with a backup generator, high-speed lift, gym, and 24/7 solar security.",
                latitude = -1.2912,
                longitude = 36.7901,
                totalUnits = 4,
                vacantUnits = 1
            ))

            val propSyokimauId = propertyDao.insertProperty(PropertyEntity(
                name = "Imara Ridge Court",
                address = "Syokimau-Mlolongo Rd, Machakos",
                type = "SINGLE_UNIT",
                description = "Peaceful modern gated community courts consisting of private detached townhouses, standard metered water.",
                latitude = -1.3341,
                longitude = 36.8833,
                totalUnits = 2,
                vacantUnits = 1
            ))

            val propRuakaId = propertyDao.insertProperty(PropertyEntity(
                name = "Ruaka Commercial Suites",
                address = "Limuru Road, Ruaka",
                type = "COMMERCIAL",
                description = "Strategically located corporate building, high traffic visibility, private server rooms and ample secure parking.",
                latitude = -1.2052,
                longitude = 36.7725,
                totalUnits = 2,
                vacantUnits = 1
            ))

            // 3. Pre-populate Units
            // Gold Oasis
            val u1 = unitDao.insertUnit(UnitEntity(propertyId = propKilimaniId, unitNumber = "Suite A101", status = "OCCUPIED", rentAmount = 65000.0, currentTenantId = "tenant@nyumbahub.co.ke"))
            val u2 = unitDao.insertUnit(UnitEntity(propertyId = propKilimaniId, unitNumber = "Apartment A102", status = "VACANT", rentAmount = 65000.0))
            val u3 = unitDao.insertUnit(UnitEntity(propertyId = propKilimaniId, unitNumber = "Penthouse B201", status = "OCCUPIED", rentAmount = 90000.0, currentTenantId = "mercy@example.com"))
            val u4 = unitDao.insertUnit(UnitEntity(propertyId = propKilimaniId, unitNumber = "Apartment B202", status = "UNDER_MAINTENANCE", rentAmount = 70000.0))

            // Syokimau
            val u5 = unitDao.insertUnit(UnitEntity(propertyId = propSyokimauId, unitNumber = "House No. 4", status = "OCCUPIED", rentAmount = 45000.0, currentTenantId = "mercy@example.com"))
            val u6 = unitDao.insertUnit(UnitEntity(propertyId = propSyokimauId, unitNumber = "House No. 7", status = "VACANT", rentAmount = 45000.0))

            // Ruaka
            val u7 = unitDao.insertUnit(UnitEntity(propertyId = propRuakaId, unitNumber = "Suite 3B", status = "OCCUPIED", rentAmount = 120000.0, currentTenantId = "kibet@example.com"))
            val u8 = unitDao.insertUnit(UnitEntity(propertyId = propRuakaId, unitNumber = "Office Row 4", status = "VACANT", rentAmount = 85000.0))

            // 4. Pre-populate Tenant details
            insertTenant(TenantEntity(
                email = "tenant@nyumbahub.co.ke",
                name = "Amani Owino",
                phoneNumber = "+254722111222",
                nationalId = "33445566",
                emergencyContactName = "Beatrice Owino (Spouse)",
                emergencyContactPhone = "+254722555666",
                leaseStart = "01-01-2026",
                leaseEnd = "31-12-2026",
                assignedUnitId = u1
            ))

            insertTenant(TenantEntity(
                email = "mercy@example.com",
                name = "Mercy Jepkosgei",
                phoneNumber = "+254711223344",
                nationalId = "32115500",
                emergencyContactName = "John Sang (Brother)",
                emergencyContactPhone = "+254711998877",
                leaseStart = "15-02-2026",
                leaseEnd = "14-02-2027",
                assignedUnitId = u3
            ))

            insertTenant(TenantEntity(
                email = "kibet@example.com",
                name = "Kibet Bett",
                phoneNumber = "+254799887766",
                nationalId = "25660099",
                emergencyContactName = "Sarah Bett (Mother)",
                emergencyContactPhone = "+254799554433",
                leaseStart = "01-03-2026",
                leaseEnd = "28-02-2027",
                assignedUnitId = u7
            ))

            // 5. Pre-populate Payments (Invoices & History)
            val currentDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            
            // Amani - April paid
            paymentDao.insertPayment(PaymentEntity(
                tenantEmail = "tenant@nyumbahub.co.ke",
                unitId = u1,
                propertyId = propKilimaniId,
                invoiceMonth = "April 2026",
                originalAmount = 65000.0,
                amountPaid = 65000.0,
                balanceDue = 0.0,
                billingDate = "01-04-2026",
                paidDate = "03-04-2026",
                paymentMethod = "M-PESA",
                status = "PAID",
                receiptNumber = "MPESA-QRT78YZ"
            ))

            // Amani - May pending
            paymentDao.insertPayment(PaymentEntity(
                tenantEmail = "tenant@nyumbahub.co.ke",
                unitId = u1,
                propertyId = propKilimaniId,
                invoiceMonth = "May 2026",
                originalAmount = 65000.0,
                amountPaid = 0.0,
                balanceDue = 65000.0,
                billingDate = "01-05-2026",
                status = "PENDING"
            ))

            // Mercy - April paid
            paymentDao.insertPayment(PaymentEntity(
                tenantEmail = "mercy@example.com",
                unitId = u3,
                propertyId = propKilimaniId,
                invoiceMonth = "April 2026",
                originalAmount = 90000.0,
                amountPaid = 90000.0,
                balanceDue = 0.0,
                billingDate = "01-04-2026",
                paidDate = "02-04-2026",
                paymentMethod = "M-PESA",
                status = "PAID",
                receiptNumber = "MPESA-ASD99GH"
            ))

            // Mercy - May paid
            paymentDao.insertPayment(PaymentEntity(
                tenantEmail = "mercy@example.com",
                unitId = u3,
                propertyId = propKilimaniId,
                invoiceMonth = "May 2026",
                originalAmount = 90000.0,
                amountPaid = 90000.0,
                balanceDue = 0.0,
                billingDate = "01-05-2026",
                paidDate = "04-05-2026",
                paymentMethod = "M-PESA",
                status = "PAID",
                receiptNumber = "MPESA-BNM88KL"
            ))

            // Kibet - May pending / partial
            paymentDao.insertPayment(PaymentEntity(
                tenantEmail = "kibet@example.com",
                unitId = u7,
                propertyId = propRuakaId,
                invoiceMonth = "May 2026",
                originalAmount = 120000.0,
                amountPaid = 35000.0,
                balanceDue = 85000.0,
                billingDate = "01-05-2026",
                paidDate = "10-05-2026",
                paymentMethod = "BANK",
                status = "PENDING"
            ))

            // 6. Pre-populate Maintenance Requests
            maintenanceDao.insertMaintenance(MaintenanceEntity(
                tenantEmail = "tenant@nyumbahub.co.ke",
                unitId = u1,
                propertyId = propKilimaniId,
                title = "Leaking Bathroom Washbasin Pipe",
                description = "The pipe below the sink in the master bathroom is leaking, filling a bucket every few hours. Please assist urgently.",
                category = "PLUMBING",
                urgency = "HIGH",
                status = "PENDING",
                createdAt = "20-05-2026 14:30:00",
                notesLogs = "Request created. Dispatched notifications to Caretaker Ndegwa."
            ))

            maintenanceDao.insertMaintenance(MaintenanceEntity(
                tenantEmail = "mercy@example.com",
                unitId = u3,
                propertyId = propKilimaniId,
                title = "Kitchen Cooker Socket Malfunction",
                description = "The main electrical socket for the built-in cooker is sparking slightly when switched on. Not currently using cooker for safety.",
                category = "ELECTRICAL",
                urgency = "EMERGENCY",
                status = "IN_PROGRESS",
                createdAt = "21-05-2026 09:12:00",
                assignedCaretakerEmail = "caretaker@nyumbahub.co.ke",
                notesLogs = "Request created. Caretaker Mwangi assigned. Scheduled site visit on 22nd May."
            ))

            maintenanceDao.insertMaintenance(MaintenanceEntity(
                tenantEmail = "kibet@example.com",
                unitId = u7,
                propertyId = propRuakaId,
                title = "Loose door latch",
                description = "Main double-door entrance latch is extremely loose and sometimes blocks locked entry. Requesting screw-tightening or replacement.",
                category = "STRUCTURAL",
                urgency = "LOW",
                status = "COMPLETED",
                createdAt = "10-05-2026 08:30:00",
                completedAt = "12-05-2026 15:45:00",
                assignedCaretakerEmail = "caretaker@nyumbahub.co.ke",
                notesLogs = "Caretaker Mwangi visited. Latch screws tightened and oiled. Verified functional."
            ))

            // 7. Insert Announcement
            announcementDao.insertAnnouncement(AnnouncementEntity(
                senderEmail = "landlord@nyumbahub.co.ke",
                propertyId = 0,
                title = "Water Supply Maintenance Warning",
                message = "Kindly note that the water supply will be intermittently interrupted on Tuesday 26th May between 8:00 AM and 1:00 PM for solar water pump servicing. Please reservoir extra water.",
                sentVia = "BULK_ALL",
                sentTimestamp = "$currentDateStr 10:00"
            ))

            // 8. Log initial action
            logAction("system", "INITIAL_SETUP", "Successfully bootstrapped NyumbaHub with sample landlord resources, Kenyan tenants, and M-Pesa ledger.")
        }
    }
}
