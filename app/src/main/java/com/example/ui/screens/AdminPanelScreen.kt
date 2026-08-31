package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.components.CyberCard
import com.example.ui.components.RiskBadge
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BotViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: BotViewModel
) {
    val users by viewModel.allUsers.collectAsState()
    val allWithdrawals by viewModel.allWithdrawals.collectAsState()
    val allDeposits by viewModel.allDeposits.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState()
    val allCountries by viewModel.allCountries.collectAsState()
    val allServices by viewModel.allServices.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()

    val currentRole by viewModel.adminRole.collectAsState()
    var selectedAdminTab by remember { mutableIntStateOf(0) }
    var userSearchQuery by remember { mutableStateOf("") }

    // Dialog States
    var selectedUserForBalance by remember { mutableStateOf<UserEntity?>(null) }
    var balanceAdjustAmount by remember { mutableStateOf("") }
    var balanceAdjustReason by remember { mutableStateOf("") }
    var showCreateTaskDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramChatBg)
    ) {
        // Admin Top Header & Role Switcher
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = TelegramSurface),
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AmberGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin",
                                tint = TelegramDarkBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "👑 Admin Master Panel",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Role: ${currentRole.name} • 2FA Secured",
                                color = CyberCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldDark)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("SYSTEM ONLINE 🟢", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Admin Sub-tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedAdminTab,
                    containerColor = TelegramCardBg,
                    contentColor = CyberCyan,
                    edgePadding = 0.dp,
                    divider = { HorizontalDivider(color = BorderStroke) }
                ) {
                    Tab(
                        selected = selectedAdminTab == 0,
                        onClick = { selectedAdminTab = 0 },
                        text = { Text("📊 Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 1,
                        onClick = { selectedAdminTab = 1 },
                        text = { Text("👥 Users (${users.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 2,
                        onClick = { selectedAdminTab = 2 },
                        text = {
                            val pendingDep = allDeposits.count { it.status == DepositStatus.PENDING }
                            Text("💳 Deposits (${if (pendingDep > 0) "$pendingDep 🔴" else "${allDeposits.size}"})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    )
                    Tab(
                        selected = selectedAdminTab == 3,
                        onClick = { selectedAdminTab = 3 },
                        text = {
                            val pendingWd = allWithdrawals.count { it.status == WithdrawalStatus.PENDING }
                            Text("💸 Withdraws (${if (pendingWd > 0) "$pendingWd 🔴" else "${allWithdrawals.size}"})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    )
                    Tab(
                        selected = selectedAdminTab == 4,
                        onClick = { selectedAdminTab = 4 },
                        text = { Text("📋 Tasks & Ads (${allTasks.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 5,
                        onClick = { selectedAdminTab = 5 },
                        text = { Text("🌍 Pricing & Service", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedAdminTab == 6,
                        onClick = { selectedAdminTab = 6 },
                        text = { Text("⚙️ Settings & Broadcast", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        when (selectedAdminTab) {
            0 -> AdminDashboardTab(
                usersCount = users.size,
                ordersCount = allOrders.size,
                depositsCount = allDeposits.size,
                withdrawalsCount = allWithdrawals.size,
                pendingWithdrawalsCount = allWithdrawals.count { it.status == WithdrawalStatus.PENDING || it.status == WithdrawalStatus.PROCESSING },
                pendingDepositsCount = allDeposits.count { it.status == DepositStatus.PENDING },
                totalUserBalance = users.sumOf { it.mainBalance },
                isEmergency = settings?.emergencyMode == true,
                onEmergencyToggle = { viewModel.adminToggleEmergencyMode(it) },
                onBackupClick = { viewModel.triggerDatabaseBackup() },
                onOpenBroadcast = { showBroadcastDialog = true }
            )
            1 -> AdminUsersTab(
                users = users,
                searchQuery = userSearchQuery,
                onSearchChange = { userSearchQuery = it },
                onAdjustBalanceClick = { selectedUserForBalance = it },
                onToggleLock = { userId, lock -> viewModel.adminToggleUserLock(userId, lock, "Admin security override") },
                onResetUser = { userId -> viewModel.adminResetUserToZero(userId) }
            )
            2 -> AdminDepositsTab(
                deposits = allDeposits,
                onApprove = { id -> viewModel.adminProcessDeposit(id, DepositStatus.APPROVED, "Verified by Admin") },
                onReject = { id -> viewModel.adminProcessDeposit(id, DepositStatus.REJECTED, "Invalid TrxID / Payment not received") }
            )
            3 -> AdminWithdrawalsTab(
                withdrawals = allWithdrawals,
                onApprove = { id -> viewModel.adminProcessWithdrawal(id, WithdrawalStatus.PAID, "Approved by Finance Admin") },
                onReject = { id -> viewModel.adminProcessWithdrawal(id, WithdrawalStatus.REJECTED, "Invalid account details") },
                onHold = { id -> viewModel.adminProcessWithdrawal(id, WithdrawalStatus.SECURITY_HOLD, "Manual review required") }
            )
            4 -> AdminTasksTab(
                tasks = allTasks,
                onCreateTaskClick = { showCreateTaskDialog = true },
                onDeleteTask = { id -> viewModel.adminDeleteTask(id) }
            )
            5 -> AdminCountriesServicesTab(
                countries = allCountries,
                services = allServices,
                onCountryUpdate = { viewModel.adminUpdateCountry(it) },
                onServiceUpdate = { viewModel.adminUpdateService(it) }
            )
            6 -> AdminSettingsTab(
                settings = settings,
                providers = providers,
                onUpdateSettings = { viewModel.adminUpdateSettings(it) },
                onBroadcastClick = { showBroadcastDialog = true },
                onBackupClick = { viewModel.triggerDatabaseBackup() },
                onEmergencyToggle = { viewModel.adminToggleEmergencyMode(it) }
            )
        }
    }

    // Dialog for Admin Balance Adjustment
    if (selectedUserForBalance != null) {
        val user = selectedUserForBalance!!
        AlertDialog(
            onDismissRequest = { selectedUserForBalance = null },
            title = { Text("Adjust User Balance: @${user.username}", fontSize = 15.sp, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Current Balance: ৳${user.mainBalance} • Withdrawable: ৳${user.withdrawableBalance}", color = TextMuted, fontSize = 12.sp)

                    OutlinedTextField(
                        value = balanceAdjustAmount,
                        onValueChange = { balanceAdjustAmount = it },
                        label = { Text("Amount (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = balanceAdjustReason,
                        onValueChange = { balanceAdjustReason = it },
                        label = { Text("Reason for Adjustment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = balanceAdjustAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.adminAdjustBalance(user.userId, amt, isAdd = true, balanceAdjustReason.ifEmpty { "Manual Admin Deposit" })
                        }
                        selectedUserForBalance = null
                        balanceAdjustAmount = ""
                        balanceAdjustReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Text("+ Add Balance", color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        val amt = balanceAdjustAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.adminAdjustBalance(user.userId, amt, isAdd = false, balanceAdjustReason.ifEmpty { "Manual Admin Deduction" })
                        }
                        selectedUserForBalance = null
                        balanceAdjustAmount = ""
                        balanceAdjustReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
                ) {
                    Text("- Deduct", color = Color.White)
                }
            },
            containerColor = TelegramSurface
        )
    }

    // Dialog for Admin Task Creation
    if (showCreateTaskDialog) {
        AdminCreateTaskDialog(
            onDismiss = { showCreateTaskDialog = false },
            onCreateTask = { title, desc, cat, reward, dur, limit, url, plat, zone ->
                viewModel.adminCreateTask(title, desc, cat, reward, dur, limit, url, plat, zone)
                showCreateTaskDialog = false
            }
        )
    }

    // Dialog for Global Broadcast
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("📢 Broadcast to All Telegram Users", fontSize = 15.sp, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Message will be dispatched to all registered Telegram users instantly.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = broadcastText,
                        onValueChange = { broadcastText = it },
                        placeholder = { Text("Enter announcement message...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (broadcastText.isNotBlank()) {
                            viewModel.adminBroadcastMessage(broadcastText)
                            broadcastText = ""
                            showBroadcastDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text("Send Broadcast", color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = TelegramSurface
        )
    }
}

@Composable
fun AdminDashboardTab(
    usersCount: Int,
    ordersCount: Int,
    depositsCount: Int,
    withdrawalsCount: Int,
    pendingWithdrawalsCount: Int,
    pendingDepositsCount: Int,
    totalUserBalance: Double,
    isEmergency: Boolean,
    onEmergencyToggle: (Boolean) -> Unit,
    onBackupClick: () -> Unit,
    onOpenBroadcast: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            // Metrics grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatMetricTile("Total Users", "$usersCount", CyberCyan, Modifier.weight(1f))
                AdminStatMetricTile("Total Balances", "৳${String.format("%.0f", totalUserBalance)}", NeonGreen, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatMetricTile("Pending Deposits", "$pendingDepositsCount", if (pendingDepositsCount > 0) AmberGold else TextMuted, Modifier.weight(1f))
                AdminStatMetricTile("Pending Withdrawals", "$pendingWithdrawalsCount", if (pendingWithdrawalsCount > 0) CoralRed else TextMuted, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminStatMetricTile("OTP Orders", "$ordersCount", TelegramLightBlue, Modifier.weight(1f))
                AdminStatMetricTile("Total Records", "${depositsCount + withdrawalsCount}", CyberCyan, Modifier.weight(1f))
            }
        }

        item {
            // Quick action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenBroadcast,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = TelegramDarkBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Broadcast", color = TelegramDarkBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onBackupClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Backup DB", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        item {
            // Emergency Lockdown Controller
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (isEmergency) CrimsonRed else BorderStroke, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = if (isEmergency) Color(0xFF450A0A) else TelegramSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🚨 Emergency Security Killswitch",
                                color = if (isEmergency) CrimsonRed else TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEmergency) "LOCKDOWN ACTIVE: Deposits & Withdrawals suspended." else "NORMAL: All financial operations active.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        Switch(
                            checked = isEmergency,
                            onCheckedChange = { onEmergencyToggle(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CrimsonRed,
                                checkedTrackColor = Color(0xFF7F1D1D)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDepositsTab(
    deposits: List<DepositEntity>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit
) {
    if (deposits.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No deposit records found.", color = TextMuted, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(deposits, key = { it.id }) { dep ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = TelegramSurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "DEP-${dep.id} • ${dep.gateway.name} (৳${dep.amount})",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sender: ${dep.senderNumber} • User ID: ${dep.userId}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "TrxID: ${dep.transactionTrxId}",
                                    color = CyberCyan,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            StatusBadge(status = dep.status.name)
                        }

                        if (dep.status == DepositStatus.PENDING) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onApprove(dep.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Approve & Credit", fontSize = 11.sp, color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onReject(dep.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Reject", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTasksTab(
    tasks: List<TaskEntity>,
    onCreateTaskClick: () -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Button(
                onClick = onCreateTaskClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = TelegramDarkBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Monetag Ad / Social Task", color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
            }
        }

        items(tasks, key = { it.id }) { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderStroke, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = TelegramSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = task.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Category: ${task.category.name} • Platform: ${task.platformName}", color = CyberCyan, fontSize = 10.sp)
                            if (task.adZoneId.isNotBlank()) {
                                Text(text = "Zone ID: ${task.adZoneId}", color = AmberGold, fontSize = 10.sp)
                            }
                        }

                        IconButton(onClick = { onDeleteTask(task.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Reward: ৳${task.rewardAmount}", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Duration: ${task.durationSeconds}s", color = TextMuted, fontSize = 11.sp)
                        Text(text = "Daily Limit: ${task.dailyLimit}", color = TextMuted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCreateTaskDialog(
    onDismiss: () -> Unit,
    onCreateTask: (String, String, TaskCategory, Double, Int, Int, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TaskCategory.MONETAG_AD) }
    var rewardText by remember { mutableStateOf("0.50") }
    var durationText by remember { mutableStateOf("15") }
    var dailyLimitText by remember { mutableStateOf("10") }
    var adZoneId by remember { mutableStateOf("11693755") }
    var platformName by remember { mutableStateOf("Monetag") }
    var actionUrl by remember { mutableStateOf("//libtl.com/sdk.js") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Earn Task / Monetag Ad", fontSize = 15.sp, color = TextPrimary) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Select Category:", color = TextMuted, fontSize = 11.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = category == TaskCategory.MONETAG_AD,
                            onClick = {
                                category = TaskCategory.MONETAG_AD
                                platformName = "Monetag"
                                adZoneId = "11693755"
                                actionUrl = "//libtl.com/sdk.js"
                                if (title.isEmpty()) title = "Monetag Ad Impression #11693755"
                            },
                            label = { Text("Monetag Ad", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = category == TaskCategory.TELEGRAM_CHANNEL,
                            onClick = {
                                category = TaskCategory.TELEGRAM_CHANNEL
                                platformName = "Telegram"
                                adZoneId = ""
                                actionUrl = "https://t.me/example"
                            },
                            label = { Text("Telegram", fontSize = 10.sp) }
                        )
                        FilterChip(
                            selected = category == TaskCategory.YOUTUBE_SUBSCRIBE,
                            onClick = {
                                category = TaskCategory.YOUTUBE_SUBSCRIBE
                                platformName = "YouTube"
                                adZoneId = ""
                                actionUrl = "https://youtube.com/@example"
                            },
                            label = { Text("YouTube", fontSize = 10.sp) }
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (category == TaskCategory.MONETAG_AD) {
                    item {
                        OutlinedTextField(
                            value = adZoneId,
                            onValueChange = { adZoneId = it },
                            label = { Text("Monetag Zone ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = rewardText,
                            onValueChange = { rewardText = it },
                            label = { Text("Reward (৳)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it },
                            label = { Text("Duration (s)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rew = rewardText.toDoubleOrNull() ?: 0.50
                    val dur = durationText.toIntOrNull() ?: 15
                    val lim = dailyLimitText.toIntOrNull() ?: 10
                    if (title.isNotBlank()) {
                        onCreateTask(title, description, category, rew, dur, lim, actionUrl, platformName, adZoneId)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Text("Create Task", color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = TelegramSurface
    )
}

@Composable
fun AdminCountriesServicesTab(
    countries: List<CountryEntity>,
    services: List<ServiceEntity>,
    onCountryUpdate: (CountryEntity) -> Unit,
    onServiceUpdate: (ServiceEntity) -> Unit
) {
    var editingService by remember { mutableStateOf<ServiceEntity?>(null) }
    var editingCountry by remember { mutableStateOf<CountryEntity?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // High Security Auto-Checking Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkGreenBg.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🛡️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "হাই সিকিউরিটি অটোমেটিক সিস্টেম চেকিং",
                                color = NeonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "বাংলাদেশ সহ সকল দেশ: ১০০% রিয়েল নাম্বার ও ওটিপি অটো গ্যারান্টি 🟢",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• বিডি অপারেটর: Grameenphone, Robi, Banglalink, Teletalk, Airtel (100% Active)\n• ইউএসএ/গ্লোবাল: Tier-1 Carrier Bypass Route Active\n• কোনো রিপোর্ট নেই - আনলিমিটেড অটোমেটিক ওটিপি ডেলিভারি ইঞ্জিন চালু আছে।",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📦 সার্ভিস রেট কমানো / বাড়ানো (Services Pricing)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("টোটাল: ${services.size}", color = TextSecondary, fontSize = 11.sp)
            }
        }

        items(services, key = { it.serviceCode }) { s ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = TelegramSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = s.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = s.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Code: ${s.serviceCode} • ক্যাটাগরি: ${s.category}", color = TextSecondary, fontSize = 10.sp)
                            }
                        }

                        Switch(
                            checked = s.isAvailable,
                            onCheckedChange = { onServiceUpdate(s.copy(isAvailable = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderStroke.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Price Increase/Decrease Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "বেস রেট (Base Price):", color = TextSecondary, fontSize = 11.sp)
                            Text(text = "৳${String.format(Locale.US, "%.2f", s.basePrice)}", color = NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // -৳5
                            FilledTonalButton(
                                onClick = {
                                    val newPrice = maxOf(1.0, s.basePrice - 5.0)
                                    onServiceUpdate(s.copy(basePrice = newPrice))
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TelegramChatBg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("-৳5", color = CoralRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // -৳1
                            FilledTonalButton(
                                onClick = {
                                    val newPrice = maxOf(1.0, s.basePrice - 1.0)
                                    onServiceUpdate(s.copy(basePrice = newPrice))
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TelegramChatBg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("-৳1", color = CoralRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // +৳1
                            FilledTonalButton(
                                onClick = {
                                    val newPrice = s.basePrice + 1.0
                                    onServiceUpdate(s.copy(basePrice = newPrice))
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TelegramChatBg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+৳1", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // +৳5
                            FilledTonalButton(
                                onClick = {
                                    val newPrice = s.basePrice + 5.0
                                    onServiceUpdate(s.copy(basePrice = newPrice))
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TelegramChatBg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+৳5", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Custom Edit
                            IconButton(
                                onClick = { editingService = s },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Price", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🌍 দেশের রেট মাল্টিপ্লায়ার (Country Multipliers)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("টোটাল: ${countries.size}", color = TextSecondary, fontSize = 11.sp)
            }
        }

        items(countries, key = { it.code }) { c ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = TelegramSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = c.flagEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "${c.name} (${c.code})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = "Phone Prefix: ${c.phonePrefix} • 100% Real Carrier Route", color = CyberCyan, fontSize = 11.sp)
                            }
                        }

                        Switch(
                            checked = c.isAvailable,
                            onCheckedChange = { onCountryUpdate(c.copy(isAvailable = it)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BorderStroke.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "মাল্টিপ্লায়ার (Rate Multiplier):", color = TextSecondary, fontSize = 11.sp)
                            Text(text = "${String.format(Locale.US, "%.2f", c.stockMultiplier)}x", color = AmberGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // -0.1x
                            FilledTonalButton(
                                onClick = {
                                    val newMult = maxOf(0.1, c.stockMultiplier - 0.1)
                                    onCountryUpdate(c.copy(stockMultiplier = (newMult * 100).toInt() / 100.0))
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TelegramChatBg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("-0.1x", color = CoralRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // +0.1x
                            FilledTonalButton(
                                onClick = {
                                    val newMult = c.stockMultiplier + 0.1
                                    onCountryUpdate(c.copy(stockMultiplier = (newMult * 100).toInt() / 100.0))
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TelegramChatBg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+0.1x", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // +0.5x
                            FilledTonalButton(
                                onClick = {
                                    val newMult = c.stockMultiplier + 0.5
                                    onCountryUpdate(c.copy(stockMultiplier = (newMult * 100).toInt() / 100.0))
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = TelegramChatBg),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+0.5x", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { editingCountry = c },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Multiplier", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Service Price Dialog
    editingService?.let { service ->
        var tempPrice by remember { mutableStateOf(service.basePrice.toString()) }
        AlertDialog(
            onDismissRequest = { editingService = null },
            title = { Text("রেট পরিবর্তন: ${service.name}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("সার্ভিস বেস রেট (৳) সেট করুন:", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempPrice,
                        onValueChange = { tempPrice = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempPrice.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            onServiceUpdate(service.copy(basePrice = parsed))
                        }
                        editingService = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text("সেভ করুন (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingService = null }) {
                    Text("বাতিল (Cancel)", color = TextSecondary)
                }
            },
            containerColor = TelegramSurface
        )
    }

    // Edit Country Multiplier Dialog
    editingCountry?.let { country ->
        var tempMult by remember { mutableStateOf(country.stockMultiplier.toString()) }
        AlertDialog(
            onDismissRequest = { editingCountry = null },
            title = { Text("মাল্টিপ্লায়ার পরিবর্তন: ${country.name}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("দেশের জন্য রেট মাল্টিপ্লায়ার সেট করুন (যেমন 1.0, 1.25, 2.0):", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempMult,
                        onValueChange = { tempMult = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = BorderStroke,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = tempMult.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            onCountryUpdate(country.copy(stockMultiplier = parsed))
                        }
                        editingCountry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black)
                ) {
                    Text("সেভ করুন (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCountry = null }) {
                    Text("বাতিল (Cancel)", color = TextSecondary)
                }
            },
            containerColor = TelegramSurface
        )
    }
}

@Composable
fun AdminSettingsTab(
    settings: SystemSettingsEntity?,
    providers: List<ProviderEntity>,
    onUpdateSettings: (SystemSettingsEntity) -> Unit,
    onBroadcastClick: () -> Unit,
    onBackupClick: () -> Unit,
    onEmergencyToggle: (Boolean) -> Unit
) {
    if (settings == null) return

    var bkashNum by remember { mutableStateOf(settings.bkashNumber) }
    var nagadNum by remember { mutableStateOf(settings.nagadNumber) }
    var usdtAddr by remember { mutableStateOf(settings.usdtAddress) }
    var bonusPct by remember { mutableStateOf(settings.depositBonusPercent.toString()) }
    var minWithdraw by remember { mutableStateOf(settings.minWithdrawalAmount.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text("⚙️ Financial Gateway Accounts", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        item {
            CyberCard(borderColor = BorderStroke) {
                OutlinedTextField(
                    value = bkashNum,
                    onValueChange = { bkashNum = it },
                    label = { Text("bKash Deposit Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nagadNum,
                    onValueChange = { nagadNum = it },
                    label = { Text("Nagad Deposit Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = usdtAddr,
                    onValueChange = { usdtAddr = it },
                    label = { Text("USDT (TRC-20) Deposit Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bonusPct,
                        onValueChange = { bonusPct = it },
                        label = { Text("Deposit Bonus %") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minWithdraw,
                        onValueChange = { minWithdraw = it },
                        label = { Text("Min Withdraw (৳)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onUpdateSettings(
                            settings.copy(
                                bkashNumber = bkashNum,
                                nagadNumber = nagadNum,
                                usdtAddress = usdtAddr,
                                depositBonusPercent = bonusPct.toDoubleOrNull() ?: 5.0,
                                minWithdrawalAmount = minWithdraw.toDoubleOrNull() ?: 50.0
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Settings", color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text("📡 SMS Provider APIs (${providers.size})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        items(providers, key = { it.providerId }) { p ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderStroke, RoundedCornerShape(10.dp)),
                colors = CardDefaults.cardColors(containerColor = TelegramSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = p.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = if (p.isOnline) "ONLINE 🟢" else "OFFLINE 🔴", color = if (p.isOnline) NeonGreen else CoralRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "Endpoint: ${p.apiEndpoint}", color = TextMuted, fontSize = 10.sp)
                    Text(text = "Success Rate: ${p.successRate}% • Balance: $${p.balanceRemaining}", color = CyberCyan, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun AdminStatMetricTile(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = TelegramSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = label, color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = accentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminUsersTab(
    users: List<UserEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAdjustBalanceClick: (UserEntity) -> Unit,
    onToggleLock: (Long, Boolean) -> Unit,
    onResetUser: (Long) -> Unit
) {
    val filteredUsers = if (searchQuery.isEmpty()) users else {
        users.filter { it.username.contains(searchQuery, ignoreCase = true) || it.userId.toString().contains(searchQuery) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search by username or user ID...", fontSize = 12.sp, color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_search_users_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = BorderStroke,
                    focusedContainerColor = TelegramSurface,
                    unfocusedContainerColor = TelegramSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        items(filteredUsers, key = { it.userId }) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = TelegramSurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TelegramBlue.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "@${user.username}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ID: ${user.userId}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        RiskBadge(riskScore = user.riskScore, riskLevel = user.riskLevel)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Main: ৳${user.mainBalance}", color = TextSecondary, fontSize = 11.sp)
                        Text(text = "Withdrawable: ৳${user.withdrawableBalance}", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Earned: ৳${user.totalEarned}", color = AmberGold, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { onAdjustBalanceClick(user) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramCardBg),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Adjust Bal", fontSize = 10.sp, color = CyberCyan)
                        }

                        Button(
                            onClick = { onToggleLock(user.userId, !user.isLocked) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = if (user.isLocked) NeonGreen else CrimsonRed),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = if (user.isLocked) "Unlock" else "Lock",
                                fontSize = 10.sp,
                                color = if (user.isLocked) TelegramDarkBlue else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onResetUser(user.userId) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B1E1E)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Reset Zero", fontSize = 10.sp, color = CoralRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminWithdrawalsTab(
    withdrawals: List<WithdrawalEntity>,
    onApprove: (Long) -> Unit,
    onReject: (Long) -> Unit,
    onHold: (Long) -> Unit
) {
    if (withdrawals.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No withdrawal requests in queue.", color = TextMuted, fontSize = 13.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(withdrawals, key = { it.id }) { wd ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = TelegramSurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "#${wd.id} • ${wd.gateway.name} (৳${wd.amount})",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Account: ${wd.accountAddress} (User ${wd.userId})",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            StatusBadge(status = wd.status.name)
                        }

                        if (wd.status == WithdrawalStatus.PENDING || wd.status == WithdrawalStatus.PROCESSING || wd.status == WithdrawalStatus.SECURITY_HOLD) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = { onApprove(wd.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Approve", fontSize = 11.sp, color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onHold(wd.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Hold", fontSize = 11.sp, color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onReject(wd.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Reject & Refund", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
