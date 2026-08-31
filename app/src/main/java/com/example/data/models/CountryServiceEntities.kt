package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val code: String, // e.g. "US", "GB", "BD", "IN", "DE", "ID"
    val name: String,
    val flagEmoji: String,
    val phonePrefix: String,
    val isAvailable: Boolean = true,
    val stockMultiplier: Double = 1.0
)

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val serviceCode: String, // e.g. "tg", "wa", "google", "openai", "fb", "ig", "tiktok", "netflix", "binance"
    val name: String,
    val category: String,
    val basePrice: Double,
    val isAvailable: Boolean = true,
    val stockCount: Int = 150,
    val iconEmoji: String = "📱"
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val providerId: String,
    val name: String,
    val apiEndpoint: String,
    val encryptedApiKey: String,
    val isOnline: Boolean = true,
    val successRate: Double = 98.4,
    val balanceRemaining: Double = 485.50
)
