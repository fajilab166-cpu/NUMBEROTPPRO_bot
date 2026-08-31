package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    DEPOSIT,
    INCOME,
    REFERRAL,
    BONUS,
    TASK,
    NUMBER_PURCHASE,
    WITHDRAWAL,
    ADMIN_ADD,
    ADMIN_DEDUCT,
    REFUND
}

enum class TransactionStatus {
    SUCCESS,
    PENDING,
    FAILED,
    ROLLBACK
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requestId: String, // Unique idempotency key
    val userId: Long,
    val type: TransactionType,
    val amount: Double,
    val oldBalance: Double,
    val newBalance: Double,
    val status: TransactionStatus = TransactionStatus.SUCCESS,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val securityHash: String = ""
)
