package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.models.OrderStatus
import com.example.data.models.RiskLevel
import com.example.data.models.UserEntity
import com.example.data.models.WithdrawalStatus
import com.example.ui.theme.*

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = TelegramCardBg,
    borderColor: Color = BorderStroke,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun RiskBadge(riskScore: Int, riskLevel: RiskLevel) {
    val (bgColor, textColor, label) = when {
        riskScore < 30 -> Triple(EmeraldDark.copy(alpha = 0.6f), NeonGreen, "🟢 LOW RISK ($riskScore)")
        riskScore < 70 -> Triple(Color(0xFF78350F), AmberGold, "🟡 MEDIUM RISK ($riskScore)")
        else -> Triple(Color(0xFF7F1D1D), CrimsonRed, "🔴 HIGH RISK ($riskScore)")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.RECEIVED.name, WithdrawalStatus.PAID.name, "ACTIVE", "SUCCESS" ->
            Pair(NeonGreen.copy(alpha = 0.2f), NeonGreen)
        OrderStatus.WAITING_OTP.name, WithdrawalStatus.PENDING.name, WithdrawalStatus.PROCESSING.name ->
            Pair(AmberGold.copy(alpha = 0.2f), AmberGold)
        OrderStatus.EXPIRED.name, OrderStatus.CANCELLED.name, WithdrawalStatus.REJECTED.name, "LOCKED", "BANNED" ->
            Pair(CrimsonRed.copy(alpha = 0.2f), CrimsonRed)
        WithdrawalStatus.SECURITY_HOLD.name ->
            Pair(PurpleAccent.copy(alpha = 0.2f), PurpleAccent)
        OrderStatus.REFUNDED.name ->
            Pair(TelegramLightBlue.copy(alpha = 0.2f), TelegramLightBlue)
        else -> Pair(TextMuted.copy(alpha = 0.2f), TextSecondary)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmergencyModeBanner(
    isEmergency: Boolean,
    onDisableClick: (() -> Unit)? = null
) {
    AnimatedVisibility(visible = isEmergency) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("emergency_banner"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency Warning",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "🚨 EMERGENCY LOCKDOWN ACTIVE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Withdrawals and new number orders are paused by Security Ops.",
                            color = Color(0xFFFECACA),
                            fontSize = 11.sp
                        )
                    }
                }

                if (onDisableClick != null) {
                    Button(
                        onClick = onDisableClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Resume", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun BalanceOverviewHeader(
    user: UserEntity?,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onBonusClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(TelegramLightBlue, CyberCyan, NeonGreen)),
                RoundedCornerShape(20.dp)
            )
            .testTag("balance_overview_header"),
        colors = CardDefaults.cardColors(containerColor = TelegramSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(TelegramSurface, TelegramDarkBlue)
                    )
                )
                .padding(18.dp)
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
                            .background(TelegramBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Main Balance",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "৳ ${String.format("%.2f", user?.mainBalance ?: 0.0)}",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                RiskBadge(
                    riskScore = user?.riskScore ?: 12,
                    riskLevel = user?.riskLevel ?: RiskLevel.LOW
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Balance breakdown pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BalancePill(
                    label = "Withdrawable",
                    amount = "৳${String.format("%.2f", user?.withdrawableBalance ?: 0.0)}",
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                BalancePill(
                    label = "Bonus Balance",
                    amount = "৳${String.format("%.2f", user?.bonusBalance ?: 0.0)}",
                    color = AmberGold,
                    modifier = Modifier.weight(1f)
                )
                BalancePill(
                    label = "Pending",
                    amount = "৳${String.format("%.2f", user?.pendingBalance ?: 0.0)}",
                    color = TelegramLightBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onDepositClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_deposit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Deposit",
                        tint = TelegramDarkBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Money", color = TelegramDarkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onWithdrawClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_withdraw_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Withdraw",
                        tint = TelegramDarkBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Withdraw", color = TelegramDarkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBonusClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_bonus_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Bonus",
                        tint = TelegramDarkBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Bonus", color = TelegramDarkBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BalancePill(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TelegramDarkBlue.copy(alpha = 0.7f))
            .border(1.dp, BorderStroke, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column {
            Text(text = label, color = TextMuted, fontSize = 10.sp)
            Text(text = amount, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
