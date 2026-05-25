package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val phoneNumber: String,
    val role: String, // ADMIN, LANDLORD, CARETAKER, TENANT
    val agencyName: String = "",
    val pinHash: String = "1234",
    val subscriptionPlan: String = "Premium Developer Plan"
)

@Entity(tableName = "properties")
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String,
    val type: String, // APARTMENT, SINGLE_UNIT, COMMERCIAL, PLOT
    val description: String = "",
    val latitude: Double = -1.2921, // Defaults near Nairobi
    val longitude: Double = 36.8219,
    val totalUnits: Int = 0,
    val vacantUnits: Int = 0,
    val documentUrl: String = "",
    val photoUrl: String = ""
)

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val unitNumber: String,
    val status: String, // VACANT, OCCUPIED, UNDER_MAINTENANCE
    val rentAmount: Double,
    val currentTenantId: String = "" // References tenant email/ID
)

@Entity(tableName = "tenants")
data class TenantEntity(
    @PrimaryKey val email: String,
    val name: String,
    val phoneNumber: String,
    val nationalId: String,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val leaseStart: String = "",
    val leaseEnd: String = "",
    val assignedUnitId: Long = 0
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tenantEmail: String,
    val unitId: Long,
    val propertyId: Long,
    val invoiceMonth: String, // e.g. "May 2026"
    val originalAmount: Double,
    val amountPaid: Double,
    val balanceDue: Double,
    val billingDate: String,
    val paidDate: String = "",
    val paymentMethod: String = "", // M-PESA, BANK, CASH
    val status: String, // PENDING, PAID, OVERDUE, CANCELLED
    val receiptNumber: String = ""
)

@Entity(tableName = "maintenance")
data class MaintenanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tenantEmail: String,
    val unitId: Long,
    val propertyId: Long,
    val title: String,
    val description: String,
    val category: String, // PLUMBING, ELECTRICAL, APPLIANCE, CARPENTRY, STRUCTURAL
    val urgency: String, // LOW, MEDIUM, HIGH, EMERGENCY
    val status: String, // PENDING, IN_PROGRESS, COMPLETED
    val assignedCaretakerEmail: String = "",
    val createdAt: String,
    val completedAt: String = "",
    val notesLogs: String = "Request created."
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderEmail: String,
    val propertyId: Long, // 0 means "All Properties"
    val title: String,
    val message: String,
    val sentVia: String, // IN_APP, SMS, WHATSAPP, BULK_ALL
    val sentTimestamp: String
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actorEmail: String,
    val action: String,
    val details: String,
    val timestamp: String
)
