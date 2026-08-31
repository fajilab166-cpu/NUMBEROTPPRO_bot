package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountStatus {
    ACTIVE,
    LOCKED,
    BANNED,
    VERIFICATION_REQUIRED
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: Long = 684920194L,
    val username: String = "pro_trader_77",
    val firstName: String = "Alex",
    val referralCode: String = "TGPRO889",
    val referredBy: String? = null,
    val mainBalance: Double = 0.00,
    val pendingBalance: Double = 0.00,
    val bonusBalance: Double = 0.00,
    val withdrawableBalance: Double = 0.00,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val riskScore: Int = 10, // 0-100
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val sessionToken: String = "tg_sec_tok_99182a",
    val is2faEnabled: Boolean = true,
    val dailyStreak: Int = 0,
    val lastBonusClaimTimestamp: Long = 0L,
    val totalReferrals: Int = 0,
    val totalEarned: Double = 0.00,
    val totalWithdrawn: Double = 0.00,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val isLocked: Boolean = false,
    val lockReason: String = ""
)
