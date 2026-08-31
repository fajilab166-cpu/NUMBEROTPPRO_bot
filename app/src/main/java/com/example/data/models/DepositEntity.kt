package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DepositStatus {
    PENDING,
    APPROVED,
    REJECTED
}

@Entity(tableName = "deposits")
data class DepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: String,
    val userId: Long,
    val gateway: PaymentGateway,
    val senderNumber: String,
    val transactionTrxId: String,
    val amount: Double,
    val bonusAmount: Double = 0.0,
    val status: DepositStatus = DepositStatus.PENDING,
    val adminNote: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val processedAtTimestamp: Long? = null
)
