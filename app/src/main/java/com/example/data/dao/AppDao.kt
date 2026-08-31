package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- USER ---
    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    fun getUserFlow(userId: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUser(userId: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdTimestamp DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("UPDATE users SET mainBalance = :newBalance, withdrawableBalance = :newWithdrawable WHERE userId = :userId")
    suspend fun updateUserBalances(userId: Long, newBalance: Double, newWithdrawable: Double)

    @Query("UPDATE users SET isLocked = :isLocked, lockReason = :reason, accountStatus = :status WHERE userId = :userId")
    suspend fun updateUserLockStatus(userId: Long, isLocked: Boolean, reason: String, status: AccountStatus)

    @Query("UPDATE users SET riskScore = :riskScore, riskLevel = :riskLevel WHERE userId = :userId")
    suspend fun updateUserRisk(userId: Long, riskScore: Int, riskLevel: RiskLevel)

    // --- TRANSACTIONS ---
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getTransactionsForUserFlow(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE requestId = :requestId LIMIT 1")
    suspend fun getTransactionByRequestId(requestId: String): TransactionEntity?

    // --- NUMBER ORDERS ---
    @Query("SELECT * FROM number_orders WHERE userId = :userId ORDER BY createdAtTimestamp DESC")
    fun getOrdersForUserFlow(userId: Long): Flow<List<NumberOrderEntity>>

    @Query("SELECT * FROM number_orders ORDER BY createdAtTimestamp DESC")
    fun getAllOrdersFlow(): Flow<List<NumberOrderEntity>>

    @Query("SELECT * FROM number_orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): NumberOrderEntity?

    @Query("SELECT * FROM number_orders WHERE orderId = :orderRef LIMIT 1")
    suspend fun getOrderByRef(orderRef: String): NumberOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: NumberOrderEntity): Long

    @Update
    suspend fun updateOrder(order: NumberOrderEntity)

    @Query("UPDATE number_orders SET status = :status, otpCode = :otp, fullSmsText = :fullText WHERE id = :orderId")
    suspend fun updateOrderOtp(orderId: Long, status: OrderStatus, otp: String, fullText: String)

    // --- WITHDRAWALS ---
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY createdAtTimestamp DESC")
    fun getWithdrawalsForUserFlow(userId: Long): Flow<List<WithdrawalEntity>>

    @Query("SELECT * FROM withdrawals ORDER BY createdAtTimestamp DESC")
    fun getAllWithdrawalsFlow(): Flow<List<WithdrawalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: WithdrawalEntity): Long

    @Query("UPDATE withdrawals SET status = :status, transactionHash = :txHash, adminNote = :note, processedAtTimestamp = :processedAt WHERE id = :id")
    suspend fun updateWithdrawalStatus(id: Long, status: WithdrawalStatus, txHash: String, note: String, processedAt: Long)

    @Query("SELECT * FROM withdrawals WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Long): WithdrawalEntity?

    // --- DEPOSITS ---
    @Query("SELECT * FROM deposits WHERE userId = :userId ORDER BY createdAtTimestamp DESC")
    fun getDepositsForUserFlow(userId: Long): Flow<List<DepositEntity>>

    @Query("SELECT * FROM deposits ORDER BY createdAtTimestamp DESC")
    fun getAllDepositsFlow(): Flow<List<DepositEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeposit(deposit: DepositEntity): Long

    @Query("UPDATE deposits SET status = :status, adminNote = :note, processedAtTimestamp = :processedAt WHERE id = :id")
    suspend fun updateDepositStatus(id: Long, status: DepositStatus, note: String, processedAt: Long)

    @Query("SELECT * FROM deposits WHERE id = :id LIMIT 1")
    suspend fun getDepositById(id: Long): DepositEntity?

    // --- TASKS ---
    @Query("SELECT * FROM tasks WHERE isActive = 1")
    fun getActiveTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("SELECT * FROM task_completions WHERE userId = :userId AND taskId = :taskId")
    suspend fun getTaskCompletions(userId: Long, taskId: Long): List<TaskCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskCompletion(completion: TaskCompletionEntity): Long

    // --- REFERRALS ---
    @Query("SELECT * FROM referrals WHERE referrerId = :userId ORDER BY createdAtTimestamp DESC")
    fun getReferralsForUserFlow(userId: Long): Flow<List<ReferralEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralEntity): Long

    // --- COUNTRIES & SERVICES ---
    @Query("SELECT * FROM countries WHERE isAvailable = 1")
    fun getAvailableCountriesFlow(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries")
    fun getAllCountriesFlow(): Flow<List<CountryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountries(countries: List<CountryEntity>)

    @Update
    suspend fun updateCountry(country: CountryEntity)

    @Query("SELECT * FROM services WHERE isAvailable = 1")
    fun getAvailableServicesFlow(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services")
    fun getAllServicesFlow(): Flow<List<ServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceEntity): Long

    @Update
    suspend fun updateService(service: ServiceEntity)

    @Query("DELETE FROM services WHERE serviceCode = :serviceCode")
    suspend fun deleteService(serviceCode: String)

    // --- PROVIDERS ---
    @Query("SELECT * FROM providers")
    fun getAllProvidersFlow(): Flow<List<ProviderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ProviderEntity>)

    // --- SECURITY LOGS ---
    @Query("SELECT * FROM security_logs ORDER BY timestamp DESC LIMIT 200")
    fun getSecurityLogsFlow(): Flow<List<SecurityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityLog(log: SecurityLogEntity): Long

    // --- SYSTEM SETTINGS ---
    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    fun getSystemSettingsFlow(): Flow<SystemSettingsEntity?>

    @Query("SELECT * FROM system_settings WHERE id = 1 LIMIT 1")
    suspend fun getSystemSettings(): SystemSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: SystemSettingsEntity)
}
