package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY name ASC")
    fun getAllPropertiesFlow(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties")
    suspend fun getAllProperties(): List<PropertyEntity>

    @Query("SELECT * FROM properties WHERE id = :id LIMIT 1")
    suspend fun getPropertyById(id: Long): PropertyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity): Long

    @Update
    suspend fun updateProperty(property: PropertyEntity)

    @Delete
    suspend fun deleteProperty(property: PropertyEntity)
}

@Dao
interface UnitDao {
    @Query("SELECT * FROM units")
    fun getAllUnitsFlow(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE propertyId = :propertyId")
    fun getUnitsForPropertyFlow(propertyId: Long): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE propertyId = :propertyId")
    suspend fun getUnitsForProperty(propertyId: Long): List<UnitEntity>

    @Query("SELECT * FROM units WHERE id = :id LIMIT 1")
    suspend fun getUnitById(id: Long): UnitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity): Long

    @Update
    suspend fun updateUnit(unit: UnitEntity)

    @Delete
    suspend fun deleteUnit(unit: UnitEntity)
}

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants")
    fun getAllTenantsFlow(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants")
    suspend fun getAllTenants(): List<TenantEntity>

    @Query("SELECT * FROM tenants WHERE email = :email LIMIT 1")
    suspend fun getTenantByEmail(email: String): TenantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: TenantEntity)

    @Update
    suspend fun updateTenant(tenant: TenantEntity)

    @Delete
    suspend fun deleteTenant(tenant: TenantEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPaymentsFlow(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE tenantEmail = :email")
    fun getPaymentsForTenantFlow(email: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE propertyId = :propertyId")
    fun getPaymentsForPropertyFlow(propertyId: Long): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)
}

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance ORDER BY id DESC")
    fun getAllMaintenanceFlow(): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenance WHERE tenantEmail = :email")
    fun getMaintenanceForTenantFlow(email: String): Flow<List<MaintenanceEntity>>

    @Query("SELECT * FROM maintenance WHERE assignedCaretakerEmail = :email")
    fun getMaintenanceForCaretakerFlow(email: String): Flow<List<MaintenanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenance(maintenance: MaintenanceEntity): Long

    @Update
    suspend fun updateMaintenance(maintenance: MaintenanceEntity)

    @Delete
    suspend fun deleteMaintenance(maintenance: MaintenanceEntity)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncementsFlow(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY id DESC")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long
}
