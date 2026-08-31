package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppDao
import com.example.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        DepositEntity::class,
        NumberOrderEntity::class,
        WithdrawalEntity::class,
        TaskEntity::class,
        TaskCompletionEntity::class,
        ReferralEntity::class,
        CountryEntity::class,
        ServiceEntity::class,
        ProviderEntity::class,
        SecurityLogEntity::class,
        SystemSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "telegram_bot_pro_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.appDao())
                }
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            // Seed Clean Default User (Zero fake balances)
            val defaultUser = UserEntity(
                userId = 684920194L,
                username = "telegram_user",
                firstName = "User",
                referralCode = "TGPRO889",
                referredBy = null,
                mainBalance = 0.00,
                pendingBalance = 0.00,
                bonusBalance = 0.00,
                withdrawableBalance = 0.00,
                accountStatus = AccountStatus.ACTIVE,
                riskScore = 5,
                riskLevel = RiskLevel.LOW,
                sessionToken = "sec_tok_init_9981",
                is2faEnabled = true,
                dailyStreak = 0,
                lastBonusClaimTimestamp = 0L,
                totalReferrals = 0,
                totalEarned = 0.00,
                totalWithdrawn = 0.00,
                createdTimestamp = System.currentTimeMillis(),
                isLocked = false
            )
            dao.insertOrUpdateUser(defaultUser)

            // Seed Countries
            val countries = listOf(
                CountryEntity("US", "United States", "🇺🇸", "+1", true, 1.2),
                CountryEntity("GB", "United Kingdom", "🇬🇧", "+44", true, 1.1),
                CountryEntity("BD", "Bangladesh", "🇧🇩", "+880", true, 0.9),
                CountryEntity("IN", "India", "🇮🇳", "+91", true, 0.85),
                CountryEntity("DE", "Germany", "🇩🇪", "+49", true, 1.3),
                CountryEntity("ID", "Indonesia", "🇮🇩", "+62", true, 0.8),
                CountryEntity("BR", "Brazil", "🇧🇷", "+55", true, 0.95),
                CountryEntity("PH", "Philippines", "🇵🇭", "+63", true, 0.85),
                CountryEntity("CA", "Canada", "🇨🇦", "+1", true, 1.25),
                CountryEntity("MY", "Malaysia", "🇲🇾", "+60", true, 1.0)
            )
            dao.insertCountries(countries)

            // Seed Services
            val services = listOf(
                ServiceEntity("tg", "Telegram", "Messaging", 18.50, true, 240, "✈️"),
                ServiceEntity("wa", "WhatsApp", "Messaging", 24.00, true, 180, "💬"),
                ServiceEntity("openai", "OpenAI / ChatGPT", "AI & Tools", 22.00, true, 95, "🤖"),
                ServiceEntity("google", "Google / Gmail", "Email & Social", 15.00, true, 310, "🌐"),
                ServiceEntity("fb", "Facebook", "Social Media", 14.50, true, 220, "👤"),
                ServiceEntity("ig", "Instagram", "Social Media", 16.00, true, 190, "📷"),
                ServiceEntity("tiktok", "TikTok", "Social Media", 19.50, true, 130, "🎵"),
                ServiceEntity("binance", "Binance Crypto", "Finance", 32.00, true, 75, "🪙"),
                ServiceEntity("netflix", "Netflix", "Entertainment", 25.00, true, 80, "🎬"),
                ServiceEntity("discord", "Discord", "Gaming & Voice", 12.00, true, 150, "🎮")
            )
            dao.insertServices(services)

            // Seed Providers
            val providers = listOf(
                ProviderEntity("prov_global_1", "GlobalSMS-SecurePro", "https://api.globalsms.secure/v3", "enc_aes256_k998124b", true, 99.1, 840.0),
                ProviderEntity("prov_tele_2", "FastOTP-Tier1", "https://api.fastotp-tier1.net/rest", "enc_aes256_t882319c", true, 97.8, 412.5),
                ProviderEntity("prov_cloud_3", "SimCloud-Direct", "https://api.simcloud.io/auth", "enc_aes256_s771209a", true, 98.6, 620.0)
            )
            dao.insertProviders(providers)

            // Seed Monetag Ad & Social Media Tasks
            val tasks = listOf(
                TaskEntity(
                    title = "Monetag High-CPM Sponsored Ad",
                    description = "Watch Monetag sponsor ad impression and complete interactive verification to earn direct wallet balance.",
                    category = TaskCategory.MONETAG_AD,
                    rewardAmount = 15.00,
                    durationSeconds = 15,
                    dailyLimit = 15,
                    verificationType = VerificationType.MONETAG_IMPRESSION_TOKEN,
                    actionUrl = "//libtl.com/sdk.js",
                    platformName = "Monetag Ads",
                    adZoneId = "11693755",
                    icon = "ads_click"
                ),
                TaskEntity(
                    title = "Join Official Telegram Channel",
                    description = "Join our main official announcements channel for real-time bot updates and promo codes.",
                    category = TaskCategory.TELEGRAM_CHANNEL,
                    rewardAmount = 20.00,
                    durationSeconds = 5,
                    dailyLimit = 1,
                    verificationType = VerificationType.TELEGRAM_BOT_MEMBERSHIP_CHECK,
                    actionUrl = "https://t.me/telegram_otp_official",
                    platformName = "Telegram",
                    icon = "send"
                ),
                TaskEntity(
                    title = "Subscribe to YouTube Channel",
                    description = "Subscribe to official channel, hit the bell notification icon, and claim instant income.",
                    category = TaskCategory.YOUTUBE_SUBSCRIBE,
                    rewardAmount = 25.00,
                    durationSeconds = 8,
                    dailyLimit = 1,
                    verificationType = VerificationType.AUTOMATIC_ACTION_VERIFY,
                    actionUrl = "https://youtube.com/@OfficialIncomeBot",
                    platformName = "YouTube",
                    icon = "smart_display"
                ),
                TaskEntity(
                    title = "Follow Facebook Official Page",
                    description = "Like and follow our verified Facebook page for daily giveaways and number restocking alerts.",
                    category = TaskCategory.FACEBOOK_PAGE,
                    rewardAmount = 18.00,
                    durationSeconds = 6,
                    dailyLimit = 1,
                    verificationType = VerificationType.AUTOMATIC_ACTION_VERIFY,
                    actionUrl = "https://facebook.com/TelegramIncomeOfficial",
                    platformName = "Facebook",
                    icon = "thumb_up"
                ),
                TaskEntity(
                    title = "Follow on Instagram",
                    description = "Follow our official Instagram profile for payment proofs and service updates.",
                    category = TaskCategory.INSTAGRAM_FOLLOW,
                    rewardAmount = 18.00,
                    durationSeconds = 6,
                    dailyLimit = 1,
                    verificationType = VerificationType.AUTOMATIC_ACTION_VERIFY,
                    actionUrl = "https://instagram.com/telegram_income_pro",
                    platformName = "Instagram",
                    icon = "photo_camera"
                ),
                TaskEntity(
                    title = "Follow on Twitter / X",
                    description = "Follow our official X handle and retweet the pinned tweet for bonus credits.",
                    category = TaskCategory.TWITTER_FOLLOW,
                    rewardAmount = 20.00,
                    durationSeconds = 5,
                    dailyLimit = 1,
                    verificationType = VerificationType.AUTOMATIC_ACTION_VERIFY,
                    actionUrl = "https://x.com/TelegramOTPBot",
                    platformName = "Twitter (X)",
                    icon = "tag"
                ),
                TaskEntity(
                    title = "Visit Partner Sponsor Website",
                    description = "Visit sponsor site for 15 seconds to receive verified visitor earnings.",
                    category = TaskCategory.WEBSITE_VISIT,
                    rewardAmount = 12.00,
                    durationSeconds = 15,
                    dailyLimit = 5,
                    verificationType = VerificationType.URL_VISIT_TOKEN,
                    actionUrl = "https://google.com",
                    platformName = "Web",
                    icon = "language"
                )
            )
            for (t in tasks) {
                dao.insertTask(t)
            }

            // Seed Security Log
            dao.insertSecurityLog(
                SecurityLogEntity(
                    userId = defaultUser.userId,
                    adminUsername = "system_core",
                    action = "INITIAL_SYSTEM_BOOT",
                    severity = Severity.INFO,
                    details = "Clean state database initialized with 0.00 fake balance. High security engine activated.",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Seed Settings
            dao.insertOrUpdateSettings(
                SystemSettingsEntity(
                    id = 1,
                    emergencyMode = false,
                    minWithdrawalAmount = 50.0,
                    maxDailyWithdrawalAmount = 1000.0,
                    minDepositAmount = 50.0,
                    maxDepositAmount = 25000.0,
                    depositBonusPercent = 5.0,
                    bkashNumber = "01712-345678 (Personal / Send Money)",
                    nagadNumber = "01812-345678 (Personal / Send Money)",
                    rocketNumber = "01912-345678 (Personal)",
                    usdtAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t (TRC20)",
                    dailyBonusBaseAmount = 10.0,
                    referralCommissionAmount = 20.0,
                    referralCommissionPercent = 10.0,
                    autoApproveLowRiskWithdrawals = true,
                    autoApproveDeposits = false,
                    rateLimitPerMinute = 30,
                    activeSmsProvider = "GlobalSMS-SecurePro",
                    monetagZoneId = "11693755",
                    monetagSdkTag = "show_11693755",
                    monetagRewardAmount = 15.0
                )
            )
        }
    }
}
