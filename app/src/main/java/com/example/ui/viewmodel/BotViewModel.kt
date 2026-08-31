package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.models.*
import com.example.data.repository.BotRepository
import com.example.data.repository.OperationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

enum class AppNavigationScreen {
    TELEGRAM_BOT,
    DASHBOARD,
    GET_NUMBER,
    MY_OTP,
    INCOME_TASKS,
    DEPOSIT,
    WITHDRAW,
    SECURITY,
    ADMIN_PANEL
}

data class TelegramMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String = "Telegram OTP Bot Pro 🤖",
    val text: String,
    val isBot: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val inlineButtons: List<TelegramInlineButton> = emptyList()
)

data class TelegramInlineButton(
    val label: String,
    val callbackData: String,
    val iconName: String = ""
)

data class ActiveMonetagTaskState(
    val taskId: Long,
    val taskTitle: String,
    val zoneId: String,
    val scriptUrl: String,
    val totalDuration: Int,
    val remainingSeconds: Int,
    val isViewing: Boolean,
    val isCompleted: Boolean,
    val verificationToken: String = ""
)

class BotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BotRepository
    val currentUserId: Long = 684920194L

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = BotRepository(database.appDao())
    }

    // Active Navigation
    private val _currentScreen = MutableStateFlow(AppNavigationScreen.TELEGRAM_BOT)
    val currentScreen: StateFlow<AppNavigationScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppNavigationScreen) {
        _currentScreen.value = screen
    }

    // Database Flows
    val currentUser = repository.getUserFlow(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userTransactions = repository.getTransactionsForUserFlow(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions = repository.getAllTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userOrders = repository.getOrdersForUserFlow(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders = repository.getAllOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWithdrawals = repository.getWithdrawalsForUserFlow(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWithdrawals = repository.getAllWithdrawalsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userDeposits = repository.getDepositsForUserFlow(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeposits = repository.getAllDepositsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks = repository.getActiveTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks = repository.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userReferrals = repository.getReferralsForUserFlow(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCountries = repository.getAvailableCountriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCountries = repository.getAllCountriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableServices = repository.getAvailableServicesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices = repository.getAllServicesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providers = repository.getAllProvidersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val securityLogs = repository.getSecurityLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemSettings = repository.getSystemSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI Feedback
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // Active Monetag Ad Task State
    private val _activeMonetagTask = MutableStateFlow<ActiveMonetagTaskState?>(null)
    val activeMonetagTask: StateFlow<ActiveMonetagTaskState?> = _activeMonetagTask.asSharedFlow().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )
    private var adTimerJob: Job? = null

    // Selected country & service for number purchase wizard
    val selectedCountry = MutableStateFlow<CountryEntity?>(null)
    val selectedService = MutableStateFlow<ServiceEntity?>(null)

    // Admin State
    val adminRole = MutableStateFlow(AdminRole.OWNER)
    val adminSearchQuery = MutableStateFlow("")
    val is2FAVerified = MutableStateFlow(true)

    // Telegram Bot Messages Stream
    private val _botMessages = MutableStateFlow<List<TelegramMessage>>(emptyList())
    val botMessages: StateFlow<List<TelegramMessage>> = _botMessages.asStateFlow()

    init {
        // Post Initial Telegram Bot Start Welcome Message
        postInitialTelegramWelcome()
    }

    private fun postInitialTelegramWelcome() {
        val welcomeMsg = TelegramMessage(
            text = """
                👋 <b>স্বাগতম Telegram Number OTP Bot Pro-তে!</b>
                
                🔐 <b>High-Security Automated Legal Number & Income System</b>
                
                🛡️ <b>User ID:</b> <code>$currentUserId</code>
                ⚡ <b>Security Status:</b> 🟢 ACTIVE (Risk Score: 12 LOW)
                🔑 <b>Session Token:</b> <code>tg_sec_tok_99182a</code>
                
                নিচের যেকোনো অপশন নির্বাচন করুন:
            """.trimIndent(),
            inlineButtons = listOf(
                TelegramInlineButton("💰 My Balance", "CMD_BALANCE"),
                TelegramInlineButton("📞 Get Number", "CMD_GET_NUMBER"),
                TelegramInlineButton("🔢 My OTP", "CMD_MY_OTP"),
                TelegramInlineButton("🎁 Daily Bonus", "CMD_DAILY_BONUS"),
                TelegramInlineButton("👥 Refer & Earn", "CMD_REFER"),
                TelegramInlineButton("📋 Tasks", "CMD_TASKS"),
                TelegramInlineButton("💸 Withdraw", "CMD_WITHDRAW"),
                TelegramInlineButton("📊 History", "CMD_HISTORY"),
                TelegramInlineButton("🔐 Security", "CMD_SECURITY"),
                TelegramInlineButton("👨‍💼 Admin Panel", "CMD_ADMIN")
            )
        )
        _botMessages.value = listOf(welcomeMsg)
    }

    fun handleTelegramCommand(commandInput: String) {
        val cmd = commandInput.trim().lowercase()
        // Add user outgoing message
        val userMsg = TelegramMessage(
            senderName = "You",
            text = commandInput,
            isBot = false
        )
        _botMessages.value = _botMessages.value + userMsg

        viewModelScope.launch {
            delay(350) // authentic typing feel
            when {
                cmd == "/start" || cmd == "start" -> {
                    val user = currentUser.value
                    val reply = TelegramMessage(
                        text = """
                            🤖 <b>Telegram Number OTP Bot Pro</b>
                            
                            👤 <b>Account:</b> @${user?.username ?: "pro_trader_77"}
                            💰 <b>Balance:</b> ৳${user?.mainBalance ?: 280.00}
                            🎁 <b>Bonus Balance:</b> ৳${user?.bonusBalance ?: 40.00}
                            🛡️ <b>Risk Score:</b> ${user?.riskScore ?: 12} (🟢 Low Risk)
                            
                            Please select an action from the menu:
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("📞 Buy Number", "CMD_GET_NUMBER"),
                            TelegramInlineButton("🎁 Daily Bonus", "CMD_DAILY_BONUS"),
                            TelegramInlineButton("📋 Earn via Tasks", "CMD_TASKS"),
                            TelegramInlineButton("💸 Withdraw Money", "CMD_WITHDRAW"),
                            TelegramInlineButton("🔐 Security Hub", "CMD_SECURITY")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                cmd == "/balance" || cmd == "balance" || cmd == "💰 my balance" -> {
                    val user = currentUser.value
                    val reply = TelegramMessage(
                        text = """
                            💰 <b>Your Secure Wallet Overview:</b>
                            
                            💳 <b>Main Balance:</b> ৳${user?.mainBalance ?: 0.0}
                            💸 <b>Withdrawable:</b> ৳${user?.withdrawableBalance ?: 0.0}
                            ⏳ <b>Pending Balance:</b> ৳${user?.pendingBalance ?: 0.0}
                            🎁 <b>Bonus Balance:</b> ৳${user?.bonusBalance ?: 0.0}
                            
                            📊 <b>Total Earned:</b> ৳${user?.totalEarned ?: 0.0}
                            💸 <b>Total Withdrawn:</b> ৳${user?.totalWithdrawn ?: 0.0}
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("💸 Withdraw Now", "CMD_WITHDRAW"),
                            TelegramInlineButton("📞 Buy SMS Number", "CMD_GET_NUMBER"),
                            TelegramInlineButton("📊 View History", "CMD_HISTORY")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                cmd == "/getnumber" || cmd == "getnumber" || cmd == "📞 get number" -> {
                    navigateTo(AppNavigationScreen.GET_NUMBER)
                    val reply = TelegramMessage(
                        text = """
                            📞 <b>Number Service System</b>
                            
                            🌍 Select Country & Service from the Number Hub.
                            ✅ Fast Auto-Provisioning
                            ✅ 15 Min Waiting Window
                            ✅ Auto-Refund if OTP not delivered
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("Open Number Wizard 🚀", "NAV_NUMBER_WIZARD")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                cmd == "/otp" || cmd == "my otp" || cmd == "🔢 my otp" -> {
                    val orders = userOrders.value.filter { it.status == OrderStatus.WAITING_OTP || it.status == OrderStatus.RECEIVED }
                    val text = if (orders.isEmpty()) {
                        "ℹ️ You have no active number orders. Click 'Buy Number' to get a temporary SMS number."
                    } else {
                        val first = orders.first()
                        """
                            🔢 <b>Active Number Order:</b>
                            
                            📱 <b>Number:</b> <code>${first.assignedNumber}</code>
                            📦 <b>Service:</b> ${first.serviceName} (${first.countryFlag} ${first.countryName})
                            ⏳ <b>Status:</b> ${first.status.name}
                            ${if (first.otpCode != null) "🎉 <b>OTP Code:</b> <code>${first.otpCode}</code>" else "⏳ <i>Waiting for SMS from provider...</i>"}
                        """.trimIndent()
                    }
                    val reply = TelegramMessage(
                        text = text,
                        inlineButtons = listOf(
                            TelegramInlineButton("Open Full OTP Monitor", "NAV_MY_OTP"),
                            TelegramInlineButton("Buy New Number", "CMD_GET_NUMBER")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                cmd == "/bonus" || cmd == "daily bonus" || cmd == "🎁 daily bonus" -> {
                    claimDailyBonus()
                }

                cmd == "/refer" || cmd == "refer & earn" || cmd == "👥 refer & earn" -> {
                    val user = currentUser.value
                    val refCode = user?.referralCode ?: "TGPRO889"
                    val reply = TelegramMessage(
                        text = """
                            👥 <b>Refer & Earn Program</b>
                            
                            🔗 <b>Your Link:</b> <code>https://t.me/TelegramOtpBotPro?start=$refCode</code>
                            🏷️ <b>Referral Code:</b> <code>$refCode</code>
                            
                            💰 <b>Commission:</b> ৳20.00 per active friend + 10% lifetime income!
                            👥 <b>Total Invited:</b> ${user?.totalReferrals ?: 0} friends
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("Simulate Friend Referral 🤝", "SIMULATE_REFERRAL"),
                            TelegramInlineButton("View Referral Dashboard", "NAV_INCOME")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                cmd == "/tasks" || cmd == "tasks" || cmd == "📋 tasks" -> {
                    navigateTo(AppNavigationScreen.INCOME_TASKS)
                    val reply = TelegramMessage(
                        text = """
                            📋 <b>Available Tasks & Income Center</b>
                            
                            🎥 <b>Video Ad:</b> ৳15.00 (Server-Verified 15s)
                            ✈️ <b>Join VIP Signals:</b> ৳25.00
                            🛡️ <b>Security Channel:</b> ৳20.00
                            📝 <b>Daily Survey:</b> ৳12.50
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("Start Video Task 🎥", "START_VIDEO_TASK"),
                            TelegramInlineButton("Open Tasks Screen", "NAV_INCOME")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                cmd == "/withdraw" || cmd == "withdraw" || cmd == "💸 withdraw" -> {
                    navigateTo(AppNavigationScreen.WITHDRAW)
                    val reply = TelegramMessage(
                        text = """
                            💸 <b>Withdrawal Portal</b>
                            
                            Gateways: <b>bKash, Nagad, Rocket, USDT TRC-20, Perfect Money, Bank Transfer</b>
                            Min: ৳50.00 | Instant Auto Payout for Low Risk
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("Open Withdraw Center 💸", "NAV_WITHDRAW")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                cmd == "/security" || cmd == "security" || cmd == "🔐 security" -> {
                    navigateTo(AppNavigationScreen.SECURITY)
                    val user = currentUser.value
                    val reply = TelegramMessage(
                        text = """
                            🔐 <b>High-Security & Fraud Center</b>
                            
                            🛡️ <b>Risk Score:</b> ${user?.riskScore ?: 12} / 100 (🟢 LOW)
                            🔑 <b>Session:</b> <code>${user?.sessionToken ?: "tg_sec_tok_99182a"}</code>
                            ⚡ <b>Anti-Spam Filter:</b> ACTIVE
                            🔒 <b>Idempotency Lock:</b> ENABLED
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("View Security Dashboard", "NAV_SECURITY")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                cmd == "/admin" || cmd == "admin" || cmd == "👨‍💼 admin panel" -> {
                    navigateTo(AppNavigationScreen.ADMIN_PANEL)
                    val reply = TelegramMessage(
                        text = "👨‍💼 <b>Admin Control Panel Opened</b>\nRole: ${adminRole.value.name}. Full control over users, stock, withdrawals, and security.",
                        inlineButtons = listOf(
                            TelegramInlineButton("Open Admin Center", "NAV_ADMIN")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }

                else -> {
                    val reply = TelegramMessage(
                        text = "❓ Unknown command: <code>$commandInput</code>\nUse the quick buttons or type /start for menu.",
                        inlineButtons = listOf(
                            TelegramInlineButton("🏠 Main Menu", "CMD_START")
                        )
                    )
                    _botMessages.value = _botMessages.value + reply
                }
            }
        }
    }

    fun handleInlineCallback(callbackData: String) {
        when (callbackData) {
            "CMD_START" -> handleTelegramCommand("/start")
            "CMD_BALANCE" -> handleTelegramCommand("/balance")
            "CMD_GET_NUMBER", "NAV_NUMBER_WIZARD" -> navigateTo(AppNavigationScreen.GET_NUMBER)
            "CMD_MY_OTP", "NAV_MY_OTP" -> navigateTo(AppNavigationScreen.MY_OTP)
            "CMD_DAILY_BONUS" -> claimDailyBonus()
            "CMD_REFER", "NAV_INCOME" -> navigateTo(AppNavigationScreen.INCOME_TASKS)
            "CMD_TASKS" -> navigateTo(AppNavigationScreen.INCOME_TASKS)
            "CMD_WITHDRAW", "NAV_WITHDRAW" -> navigateTo(AppNavigationScreen.WITHDRAW)
            "CMD_HISTORY" -> navigateTo(AppNavigationScreen.DASHBOARD)
            "CMD_SECURITY", "NAV_SECURITY" -> navigateTo(AppNavigationScreen.SECURITY)
            "CMD_ADMIN", "NAV_ADMIN" -> navigateTo(AppNavigationScreen.ADMIN_PANEL)
            "START_VIDEO_TASK" -> navigateTo(AppNavigationScreen.INCOME_TASKS)
            "SIMULATE_REFERRAL" -> simulateFriendReferral()
        }
    }

    // --- ACTIONS ---

    fun claimDailyBonus() {
        viewModelScope.launch {
            when (val result = repository.claimDailyBonus(currentUserId)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit("🎉 ${result.message}")
                    val msg = TelegramMessage(
                        text = "🎁 <b>Daily Bonus Claimed!</b>\n\nCredited: <b>+৳${result.data}</b> to your wallet.\nCome back tomorrow for your next streak bonus! 🔥"
                    )
                    _botMessages.value = _botMessages.value + msg
                }
                is OperationResult.Error -> {
                    _snackbarMessage.emit(result.message)
                    val msg = TelegramMessage(
                        text = "⚠️ <b>Daily Bonus Info:</b>\n${result.message}"
                    )
                    _botMessages.value = _botMessages.value + msg
                }
            }
        }
    }

    fun buyNumber(country: CountryEntity, service: ServiceEntity) {
        viewModelScope.launch {
            when (val result = repository.purchaseNumber(currentUserId, country, service)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit("✅ Number purchased! Assigned: ${result.data.assignedNumber}")
                    navigateTo(AppNavigationScreen.MY_OTP)
                    val msg = TelegramMessage(
                        text = """
                            📞 <b>Number Assigned Successfully!</b>
                            
                            📱 <b>Phone:</b> <code>${result.data.assignedNumber}</code>
                            📦 <b>Service:</b> ${result.data.serviceName} (${result.data.countryFlag})
                            💰 <b>Cost:</b> ৳${result.data.cost}
                            ⏳ <b>Status:</b> Waiting for SMS... (15m window)
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("Simulate OTP Arrival 📩", "SIMULATE_OTP_${result.data.id}"),
                            TelegramInlineButton("View in OTP Hub", "NAV_MY_OTP")
                        )
                    )
                    _botMessages.value = _botMessages.value + msg

                    // Auto trigger simulated OTP arrival in 6 seconds for realistic demo if not manually clicked
                    delay(5000)
                    simulateOtpArrival(result.data.id)
                }
                is OperationResult.Error -> {
                    _snackbarMessage.emit("❌ ${result.message}")
                }
            }
        }
    }

    fun simulateOtpArrival(orderId: Long) {
        viewModelScope.launch {
            when (val result = repository.simulateOtpArrival(orderId)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit("🎉 SMS OTP Delivered: ${result.data}")
                    val msg = TelegramMessage(
                        text = """
                            🔢 <b>OTP RECEIVED!</b>
                            
                            🔑 <b>Verification Code:</b> <code>${result.data}</code>
                            🔒 <b>Access Control:</b> Verified Owner Check PASSED
                            Ref: <code>ORD-$orderId</code>
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("View in OTP Hub 📋", "NAV_MY_OTP")
                        )
                    )
                    _botMessages.value = _botMessages.value + msg
                }
                is OperationResult.Error -> {
                    // silently handle or toast
                }
            }
        }
    }

    fun cancelAndRefundOrder(orderId: Long) {
        viewModelScope.launch {
            when (val result = repository.cancelAndRefundOrder(orderId)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit(result.message)
                }
                is OperationResult.Error -> {
                    _snackbarMessage.emit(result.message)
                }
            }
        }
    }

    // --- MONETAG AD TASK FLOW ---
    fun startMonetagAdTask(task: TaskEntity) {
        adTimerJob?.cancel()
        val duration = if (task.durationSeconds > 0) task.durationSeconds else 15
        _activeMonetagTask.value = ActiveMonetagTaskState(
            taskId = task.id,
            taskTitle = task.title,
            zoneId = task.adZoneId,
            scriptUrl = task.actionUrl,
            totalDuration = duration,
            remainingSeconds = duration,
            isViewing = true,
            isCompleted = false
        )

        adTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _activeMonetagTask.value ?: break
                if (current.remainingSeconds <= 1) {
                    val token = "monetag_val_" + UUID.randomUUID().toString().take(12)
                    _activeMonetagTask.value = current.copy(
                        remainingSeconds = 0,
                        isViewing = false,
                        isCompleted = true,
                        verificationToken = token
                    )
                    completeTaskVerification(current.taskId, token)
                    break
                } else {
                    _activeMonetagTask.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                }
            }
        }
    }

    fun dismissMonetagTask() {
        adTimerJob?.cancel()
        _activeMonetagTask.value = null
    }

    fun completeSocialTask(task: TaskEntity) {
        viewModelScope.launch {
            val token = "soc_verify_" + UUID.randomUUID().toString().take(10)
            completeTaskVerification(task.id, token)
        }
    }

    fun completeTaskVerification(taskId: Long, token: String) {
        viewModelScope.launch {
            when (val result = repository.completeTask(currentUserId, taskId, token)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit("🎉 ${result.message}")
                    _activeMonetagTask.value = null
                    val msg = TelegramMessage(
                        text = "📋 <b>টাস্ক সম্পন্ন হয়েছে!</b>\n\nপুরস্কার: <b>+৳${result.data}</b> আপনার ওয়ালেটে জমা হয়েছে।"
                    )
                    _botMessages.value = _botMessages.value + msg
                }
                is OperationResult.Error -> {
                    _snackbarMessage.emit("❌ ${result.message}")
                }
            }
        }
    }

    // --- DEPOSIT SUBMISSION ---
    fun submitDeposit(gateway: PaymentGateway, senderNumber: String, trxId: String, amount: Double) {
        viewModelScope.launch {
            when (val result = repository.requestDeposit(currentUserId, gateway, senderNumber, trxId, amount)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit("✅ ${result.message}")
                    val msg = TelegramMessage(
                        text = """
                            💳 <b>Deposit Request Received!</b>
                            
                            🆔 <b>Request ID:</b> <code>${result.data.requestId}</code>
                            🏦 <b>Gateway:</b> ${result.data.gateway.name}
                            📱 <b>Sender:</b> <code>${result.data.senderNumber}</code>
                            🔑 <b>TrxID:</b> <code>${result.data.transactionTrxId}</code>
                            💰 <b>Amount:</b> ৳${result.data.amount} ${if (result.data.bonusAmount > 0) "(+৳${result.data.bonusAmount} Bonus)" else ""}
                            ⏳ <b>Status:</b> ${result.data.status.name}
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("View Deposit History", "NAV_DEPOSIT")
                        )
                    )
                    _botMessages.value = _botMessages.value + msg
                }
                is OperationResult.Error -> {
                    _snackbarMessage.emit("❌ ${result.message}")
                }
            }
        }
    }

    fun simulateFriendReferral() {
        viewModelScope.launch {
            val randomNames = listOf("tariq_dev", "sumon_pro", "rashed_crypto", "nadim_vip", "hasan_tk")
            val name = randomNames.random() + "_" + Random.nextInt(10, 99)
            when (val result = repository.registerReferral(currentUserId, name)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit(result.message)
                    val msg = TelegramMessage(
                        text = "👥 <b>New Referral Joined!</b>\n\nUser @$name joined using your referral code!\nCommission <b>+৳${result.data}</b> credited to your wallet."
                    )
                    _botMessages.value = _botMessages.value + msg
                }
                is OperationResult.Error -> {
                    _snackbarMessage.emit(result.message)
                }
            }
        }
    }

    // --- WITHDRAWAL SUBMISSION ---
    fun submitWithdrawal(gateway: PaymentGateway, accountAddress: String, amount: Double) {
        viewModelScope.launch {
            when (val result = repository.requestWithdrawal(currentUserId, gateway, accountAddress, amount)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit("✅ ${result.message}")
                    val msg = TelegramMessage(
                        text = """
                            💸 <b>Withdrawal Request Created!</b>
                            
                            🆔 <b>ID:</b> <code>${result.data.requestId}</code>
                            🏦 <b>Gateway:</b> ${result.data.gateway.name}
                            📱 <b>Account:</b> <code>${result.data.accountAddress}</code>
                            💰 <b>Amount:</b> ৳${result.data.amount} (Net: ৳${result.data.finalAmount})
                            ⏳ <b>Status:</b> ${result.data.status.name}
                        """.trimIndent(),
                        inlineButtons = listOf(
                            TelegramInlineButton("View Withdrawal History", "NAV_WITHDRAW")
                        )
                    )
                    _botMessages.value = _botMessages.value + msg
                }
                is OperationResult.Error -> {
                    _snackbarMessage.emit("❌ ${result.message}")
                }
            }
        }
    }

    // --- ADMIN CONTROLS ---
    fun adminProcessWithdrawal(withdrawalId: Long, status: WithdrawalStatus, note: String) {
        viewModelScope.launch {
            when (val result = repository.adminProcessWithdrawal(withdrawalId, status, note)) {
                is OperationResult.Success -> _snackbarMessage.emit("Admin: ${result.message}")
                is OperationResult.Error -> _snackbarMessage.emit("Admin Error: ${result.message}")
            }
        }
    }

    fun adminProcessDeposit(depositId: Long, status: DepositStatus, note: String) {
        viewModelScope.launch {
            when (val result = repository.adminProcessDeposit(depositId, status, note)) {
                is OperationResult.Success -> _snackbarMessage.emit("Admin: ${result.message}")
                is OperationResult.Error -> _snackbarMessage.emit("Admin Error: ${result.message}")
            }
        }
    }

    fun adminAdjustBalance(userId: Long, amount: Double, isAdd: Boolean, reason: String) {
        viewModelScope.launch {
            when (val result = repository.adminAdjustBalance(userId, amount, isAdd, reason)) {
                is OperationResult.Success -> _snackbarMessage.emit("Admin: ${result.message}")
                is OperationResult.Error -> _snackbarMessage.emit("Admin Error: ${result.message}")
            }
        }
    }

    fun adminToggleUserLock(userId: Long, lock: Boolean, reason: String) {
        viewModelScope.launch {
            when (val result = repository.adminToggleUserLock(userId, lock, reason)) {
                is OperationResult.Success -> _snackbarMessage.emit("Admin: ${result.message}")
                is OperationResult.Error -> _snackbarMessage.emit("Admin Error: ${result.message}")
            }
        }
    }

    fun adminCreateTask(
        title: String,
        description: String,
        category: TaskCategory,
        rewardAmount: Double,
        durationSeconds: Int,
        dailyLimit: Int,
        actionUrl: String,
        platformName: String,
        adZoneId: String
    ) {
        viewModelScope.launch {
            when (val result = repository.adminCreateTask(
                title, description, category, rewardAmount, durationSeconds, dailyLimit, actionUrl, platformName, adZoneId
            )) {
                is OperationResult.Success -> _snackbarMessage.emit("✅ ${result.message}")
                is OperationResult.Error -> _snackbarMessage.emit("❌ ${result.message}")
            }
        }
    }

    fun adminDeleteTask(taskId: Long) {
        viewModelScope.launch {
            when (val result = repository.adminDeleteTask(taskId)) {
                is OperationResult.Success -> _snackbarMessage.emit(result.message)
                is OperationResult.Error -> _snackbarMessage.emit(result.message)
            }
        }
    }

    fun adminUpdateCountry(country: CountryEntity) {
        viewModelScope.launch {
            repository.adminUpdateCountry(country)
            _snackbarMessage.emit("Updated ${country.name} multiplier")
        }
    }

    fun adminUpdateService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.adminUpdateService(service)
            _snackbarMessage.emit("Updated ${service.name} pricing")
        }
    }

    fun adminCreateService(service: ServiceEntity) {
        viewModelScope.launch {
            repository.adminCreateService(service)
            _snackbarMessage.emit("Created service ${service.name}")
        }
    }

    fun adminDeleteService(serviceCode: String) {
        viewModelScope.launch {
            repository.adminDeleteService(serviceCode)
            _snackbarMessage.emit("Service deleted")
        }
    }

    fun adminUpdateSettings(settings: SystemSettingsEntity) {
        viewModelScope.launch {
            when (val result = repository.adminUpdateSettings(settings)) {
                is OperationResult.Success -> _snackbarMessage.emit(result.message)
                is OperationResult.Error -> _snackbarMessage.emit(result.message)
            }
        }
    }

    fun adminBroadcastMessage(message: String) {
        viewModelScope.launch {
            when (val result = repository.adminBroadcastMessage(message)) {
                is OperationResult.Success -> {
                    _snackbarMessage.emit("📢 Broadcast sent to all users!")
                    val msg = TelegramMessage(
                        text = "📢 <b>ADMIN BROADCAST:</b>\n\n$message"
                    )
                    _botMessages.value = _botMessages.value + msg
                }
                is OperationResult.Error -> _snackbarMessage.emit(result.message)
            }
        }
    }

    fun adminResetUserToZero(userId: Long) {
        viewModelScope.launch {
            when (val result = repository.resetUserToCleanZero(userId)) {
                is OperationResult.Success -> _snackbarMessage.emit(result.message)
                is OperationResult.Error -> _snackbarMessage.emit(result.message)
            }
        }
    }

    fun adminToggleEmergencyMode(enable: Boolean) {
        viewModelScope.launch {
            when (val result = repository.adminToggleEmergencyMode(enable)) {
                is OperationResult.Success -> _snackbarMessage.emit(result.message)
                is OperationResult.Error -> _snackbarMessage.emit(result.message)
            }
        }
    }

    fun triggerDatabaseBackup() {
        viewModelScope.launch {
            when (val result = repository.triggerBackupSnapshot()) {
                is OperationResult.Success -> _snackbarMessage.emit("🗄️ Backup verified! Hash: ${result.data.take(16)}...")
                is OperationResult.Error -> _snackbarMessage.emit(result.message)
            }
        }
    }
}
