package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Severity {
    INFO,
    WARNING,
    CRITICAL,
    EMERGENCY
}

enum class AdminRole {
    OWNER,
    ADMIN,
    FINANCE,
    SECURITY,
    SUPPORT
}

@Entity(tableName = "security_logs")
data class SecurityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long? = null,
    val adminUsername: String? = null,
    val action: String,
    val severity: Severity = Severity.INFO,
    val details: String,
    val ipAddress: String = "192.168.1.101",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val emergencyMode: Boolean = false,
    val minWithdrawalAmount: Double = 50.0,
    val maxDailyWithdrawalAmount: Double = 1000.0,
    val minDepositAmount: Double = 50.0,
    val maxDepositAmount: Double = 25000.0,
    val depositBonusPercent: Double = 5.0,
    val bkashNumber: String = "01700000000 (Personal/Send Money)",
    val nagadNumber: String = "01800000000 (Personal/Send Money)",
    val rocketNumber: String = "01900000000 (Personal)",
    val usdtAddress: String = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t (TRC20)",
    val dailyBonusBaseAmount: Double = 10.0,
    val referralCommissionAmount: Double = 20.0,
    val referralCommissionPercent: Double = 10.0,
    val autoApproveLowRiskWithdrawals: Boolean = true,
    val autoApproveDeposits: Boolean = false,
    val rateLimitPerMinute: Int = 30,
    val activeSmsProvider: String = "GlobalSMS-SecurePro",
    val monetagZoneId: String = "11693755",
    val monetagSdkTag: String = "show_11693755",
    val monetagRewardAmount: Double = 15.0,
    val backupLastCreatedTimestamp: Long = System.currentTimeMillis() - 3600000L
)
