package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class WithdrawalStatus {
    PENDING,
    PROCESSING,
    PAID,
    FAILED,
    REJECTED,
    SECURITY_HOLD
}

enum class PaymentGateway {
    BKASH,
    NAGAD,
    ROCKET,
    USDT_TRC20,
    PERFECT_MONEY,
    BANK_TRANSFER
}

@Entity(tableName = "withdrawals")
data class WithdrawalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: String,
    val userId: Long,
    val gateway: PaymentGateway,
    val accountAddress: String,
    val amount: Double,
    val fee: Double = 0.0,
    val finalAmount: Double,
    val status: WithdrawalStatus = WithdrawalStatus.PENDING,
    val transactionHash: String = "",
    val fraudRiskScore: Int = 10,
    val adminNote: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val processedAtTimestamp: Long? = null
)
