package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavigationScreen
import com.example.ui.viewmodel.BotViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: BotViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val transactions by viewModel.userTransactions.collectAsState()
    val orders by viewModel.userOrders.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()

    var selectedFilter by remember { mutableStateOf<TransactionType?>(null) }

    val activeOrder = orders.firstOrNull { it.status == OrderStatus.WAITING_OTP || it.status == OrderStatus.RECEIVED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramChatBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // Emergency Alert if active
        item {
            EmergencyModeBanner(
                isEmergency = settings?.emergencyMode == true,
                onDisableClick = { viewModel.adminToggleEmergencyMode(false) }
            )
        }

        // Wallet Balance Header
        item {
            BalanceOverviewHeader(
                user = user,
                onDepositClick = { viewModel.navigateTo(AppNavigationScreen.DEPOSIT) },
                onWithdrawClick = { viewModel.navigateTo(AppNavigationScreen.WITHDRAW) },
                onBonusClick = { viewModel.claimDailyBonus() }
            )
        }

        // Active Order Live Banner if any
        if (activeOrder != null) {
            item {
                ActiveOrderBanner(
                    order = activeOrder,
                    onViewClick = { viewModel.navigateTo(AppNavigationScreen.MY_OTP) }
                )
            }
        }

        // Fast Actions Grid
        item {
            Text(
                text = "⚡ Quick Operations",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "Get Number",
                    subtitle = "Instant SMS",
                    icon = Icons.Default.PhoneIphone,
                    accentColor = TelegramLightBlue,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(AppNavigationScreen.GET_NUMBER)
                }

                ActionTile(
                    title = "My OTP Hub",
                    subtitle = "Live Codes",
                    icon = Icons.Default.Pin,
                    accentColor = CyberCyan,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(AppNavigationScreen.MY_OTP)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ActionTile(
                    title = "Earn via Tasks",
                    subtitle = "Video & Social",
                    icon = Icons.Default.Task,
                    accentColor = NeonGreen,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(AppNavigationScreen.INCOME_TASKS)
                }

                ActionTile(
                    title = "Security & Risk",
                    subtitle = "Anti-Fraud 2FA",
                    icon = Icons.Default.Security,
                    accentColor = PurpleAccent,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.navigateTo(AppNavigationScreen.SECURITY)
                }
            }
        }

        // Anti-Fraud Health Status Card
        item {
            CyberCard(
                borderColor = CyberCyan.copy(alpha = 0.3f),
                backgroundColor = TelegramSurface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(EmeraldDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "System Security Engine",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Rate Limit 30/min • Idempotency Locked",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    RiskBadge(
                        riskScore = user?.riskScore ?: 12,
                        riskLevel = user?.riskLevel ?: RiskLevel.LOW
                    )
                }
            }
        }

        // Transaction History Header & Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 Transaction History",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${transactions.size} records",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChipItem(
                    label = "All",
                    isSelected = selectedFilter == null,
                    onClick = { selectedFilter = null }
                )
                FilterChipItem(
                    label = "Bonus",
                    isSelected = selectedFilter == TransactionType.BONUS,
                    onClick = { selectedFilter = TransactionType.BONUS }
                )
                FilterChipItem(
                    label = "Task",
                    isSelected = selectedFilter == TransactionType.TASK,
                    onClick = { selectedFilter = TransactionType.TASK }
                )
                FilterChipItem(
                    label = "Referral",
                    isSelected = selectedFilter == TransactionType.REFERRAL,
                    onClick = { selectedFilter = TransactionType.REFERRAL }
                )
                FilterChipItem(
                    label = "Number",
                    isSelected = selectedFilter == TransactionType.NUMBER_PURCHASE,
                    onClick = { selectedFilter = TransactionType.NUMBER_PURCHASE }
                )
            }
        }

        // Transactions List
        val filteredList = if (selectedFilter == null) {
            transactions
        } else {
            transactions.filter { it.type == selectedFilter }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found under this category.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { tx ->
                TransactionRowItem(transaction = tx)
            }
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, BorderStroke, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = TelegramSurface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ActiveOrderBanner(
    order: NumberOrderEntity,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onViewClick)
            .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C243B))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = order.countryFlag, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Active ${order.serviceName} Number",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.assignedNumber,
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            StatusBadge(status = order.status.name)
        }
    }
}

@Composable
fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) TelegramBlue else TelegramSurface)
            .border(
                1.dp,
                if (isSelected) CyberCyan else BorderStroke,
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TransactionRowItem(transaction: TransactionEntity) {
    val isPositive = transaction.amount > 0
    val timeStr = remember(transaction.timestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(transaction.timestamp))
    }

    val (icon, iconColor) = when (transaction.type) {
        TransactionType.BONUS -> Pair(Icons.Default.CardGiftcard, AmberGold)
        TransactionType.TASK -> Pair(Icons.Default.TaskAlt, NeonGreen)
        TransactionType.REFERRAL -> Pair(Icons.Default.People, CyberCyan)
        TransactionType.NUMBER_PURCHASE -> Pair(Icons.Default.PhoneIphone, TelegramLightBlue)
        TransactionType.WITHDRAWAL -> Pair(Icons.Default.Payments, CrimsonRed)
        TransactionType.REFUND -> Pair(Icons.Default.RotateLeft, NeonGreen)
        TransactionType.ADMIN_ADD -> Pair(Icons.Default.AddCircle, NeonGreen)
        TransactionType.ADMIN_DEDUCT -> Pair(Icons.Default.RemoveCircle, CrimsonRed)
        else -> Pair(Icons.Default.Receipt, TextSecondary)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = TelegramSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = transaction.type.name,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = transaction.note.ifEmpty { transaction.type.name },
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = "$timeStr • Ref: ${transaction.requestId.take(12)}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isPositive) "+" else ""}৳${String.format("%.2f", transaction.amount)}",
                    color = if (isPositive) NeonGreen else CrimsonRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Bal: ৳${String.format("%.2f", transaction.newBalance)}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
