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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.PaymentGateway
import com.example.data.models.WithdrawalEntity
import com.example.data.models.WithdrawalStatus
import com.example.ui.components.CyberCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BotViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WithdrawScreen(
    viewModel: BotViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val withdrawals by viewModel.userWithdrawals.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()

    var selectedGateway by remember { mutableStateOf(PaymentGateway.BKASH) }
    var accountAddress by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }

    val withdrawableBalance = user?.withdrawableBalance ?: 0.0
    val minWithdrawal = settings?.minWithdrawalAmount ?: 50.0
    val maxDaily = settings?.maxDailyWithdrawalAmount ?: 1000.0

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val fee = when (selectedGateway) {
        PaymentGateway.USDT_TRC20 -> 10.0
        PaymentGateway.BANK_TRANSFER -> 15.0
        else -> 0.0
    }
    val netPayout = (parsedAmount - fee).coerceAtLeast(0.0)

    val isValidAmount = parsedAmount >= minWithdrawal && parsedAmount <= withdrawableBalance && parsedAmount <= maxDaily
    val isValidAddress = accountAddress.trim().length >= 5
    val canSubmit = isValidAmount && isValidAddress && (settings?.emergencyMode != true)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramChatBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Balance Overview Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonGreen.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = TelegramSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Withdrawable Balance", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = "৳ ${String.format("%.2f", withdrawableBalance)}",
                            color = NeonGreen,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Daily Limit", color = TextMuted, fontSize = 11.sp)
                        Text(
                            text = "Max ৳$maxDaily",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 1. Gateway Picker
        item {
            Text(
                text = "1️⃣ Select Payment Gateway",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val gateways = PaymentGateway.values()
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(gateways) { gw ->
                    val isSelected = selectedGateway == gw
                    val (label, icon, color) = when (gw) {
                        PaymentGateway.BKASH -> Triple("bKash", "📱", Color(0xFFE2136E))
                        PaymentGateway.NAGAD -> Triple("Nagad", "⚡", Color(0xFFF7941D))
                        PaymentGateway.ROCKET -> Triple("Rocket", "🚀", Color(0xFF8C3494))
                        PaymentGateway.USDT_TRC20 -> Triple("USDT (TRC20)", "🪙", NeonGreen)
                        PaymentGateway.PERFECT_MONEY -> Triple("Perfect Money", "💳", CrimsonRed)
                        PaymentGateway.BANK_TRANSFER -> Triple("Bank Transfer", "🏦", TelegramLightBlue)
                    }

                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedGateway = gw }
                            .border(
                                1.5.dp,
                                if (isSelected) CyberCyan else BorderStroke,
                                RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TelegramCardBg else TelegramSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                color = if (isSelected) CyberCyan else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Account Input & Amount
        item {
            CyberCard {
                Text(
                    text = "2️⃣ Payout Destination & Amount",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = accountAddress,
                    onValueChange = { accountAddress = it },
                    label = { Text("Account Number / Wallet Address") },
                    placeholder = {
                        Text(
                            when (selectedGateway) {
                                PaymentGateway.BKASH, PaymentGateway.NAGAD, PaymentGateway.ROCKET -> "017XXXXXXXX"
                                PaymentGateway.USDT_TRC20 -> "T..."
                                else -> "Account details"
                            },
                            color = TextMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_account_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = BorderStroke,
                        focusedContainerColor = TelegramDarkBlue,
                        unfocusedContainerColor = TelegramDarkBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Withdrawal Amount (৳)") },
                    placeholder = { Text("Min ৳$minWithdrawal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = BorderStroke,
                        focusedContainerColor = TelegramDarkBlue,
                        unfocusedContainerColor = TelegramDarkBlue,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Amount Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(50.0, 100.0, 200.0, 500.0).forEach { amt ->
                        Button(
                            onClick = { amountInput = amt.toInt().toString() },
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TelegramDarkBlue),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("৳${amt.toInt()}", fontSize = 11.sp, color = CyberCyan)
                        }
                    }
                    Button(
                        onClick = { amountInput = withdrawableBalance.toInt().toString() },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("MAX", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculation breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Gateway Fee:", color = TextMuted, fontSize = 11.sp)
                    Text(text = "৳${String.format("%.2f", fee)}", color = TextSecondary, fontSize = 11.sp)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Net Amount to Receive:", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "৳${String.format("%.2f", netPayout)}",
                        color = NeonGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.submitWithdrawal(selectedGateway, accountAddress, parsedAmount)
                        accountAddress = ""
                        amountInput = ""
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("submit_withdrawal_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, disabledContainerColor = BorderStroke),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = TelegramDarkBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Submit Withdrawal Request",
                        color = TelegramDarkBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // 3. Withdrawal History
        item {
            Text(
                text = "📜 Withdrawal History (${withdrawals.size})",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (withdrawals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No withdrawal requests yet.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(withdrawals, key = { it.id }) { wd ->
                WithdrawalHistoryCard(withdrawal = wd)
            }
        }
    }
}

@Composable
fun WithdrawalHistoryCard(withdrawal: WithdrawalEntity) {
    val timeStr = remember(withdrawal.createdAtTimestamp) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(withdrawal.createdAtTimestamp))
    }

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
                        text = "${withdrawal.gateway.name} (${withdrawal.accountAddress})",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$timeStr • Ref: ${withdrawal.requestId.take(12)}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                StatusBadge(status = withdrawal.status.name)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Amount: ৳${withdrawal.amount} (Fee: ৳${withdrawal.fee})",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Net: ৳${withdrawal.finalAmount}",
                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (withdrawal.transactionHash.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "TXID: ${withdrawal.transactionHash}",
                    color = CyberCyan,
                    fontSize = 10.sp
                )
            }
        }
    }
}
