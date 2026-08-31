package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskCategory {
    MONETAG_AD,
    TELEGRAM_CHANNEL,
    YOUTUBE_SUBSCRIBE,
    FACEBOOK_PAGE,
    INSTAGRAM_FOLLOW,
    TWITTER_FOLLOW,
    TIKTOK_FOLLOW,
    WEBSITE_VISIT
}

enum class VerificationType {
    MONETAG_IMPRESSION_TOKEN,
    TELEGRAM_BOT_MEMBERSHIP_CHECK,
    URL_VISIT_TOKEN,
    AUTOMATIC_ACTION_VERIFY
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: TaskCategory,
    val rewardAmount: Double,
    val durationSeconds: Int = 15,
    val dailyLimit: Int = 5,
    val maxCompletions: Int = 1000,
    val currentCompletions: Int = 0,
    val verificationType: VerificationType = VerificationType.AUTOMATIC_ACTION_VERIFY,
    val actionUrl: String = "",
    val platformName: String = "Social",
    val adZoneId: String = "11693755",
    val isActive: Boolean = true,
    val icon: String = "task_alt"
)

@Entity(tableName = "task_completions")
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val userId: Long,
    val rewardGranted: Double,
    val verificationToken: String,
    val completedAtTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referrerId: Long,
    val refereeId: Long,
    val refereeUsername: String,
    val bonusEarned: Double = 15.0,
    val status: String = "ACTIVE",
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
