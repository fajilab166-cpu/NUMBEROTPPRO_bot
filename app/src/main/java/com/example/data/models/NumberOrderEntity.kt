package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class OrderStatus {
    WAITING_OTP,
    RECEIVED,
    EXPIRED,
    CANCELLED,
    REFUNDED
}

@Entity(tableName = "number_orders")
data class NumberOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val userId: Long,
    val countryCode: String,
    val countryName: String,
    val countryFlag: String,
    val serviceCode: String,
    val serviceName: String,
    val serviceIcon: String,
    val cost: Double,
    val assignedNumber: String,
    val otpCode: String? = null,
    val senderName: String? = null,
    val fullSmsText: String? = null,
    val status: OrderStatus = OrderStatus.WAITING_OTP,
    val providerName: String = "GlobalSMS-SecurePro",
    val expiresAtTimestamp: Long = System.currentTimeMillis() + (15 * 60 * 1000L),
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
