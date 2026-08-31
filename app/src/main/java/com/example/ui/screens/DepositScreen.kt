package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.DepositEntity
import com.example.data.models.DepositStatus
import com.example.data.models.PaymentGateway
import com.example.ui.components.CyberCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.BotViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DepositScreen(
    viewModel: BotViewModel
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val deposits by viewModel.userDeposits.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()

    var selectedGateway by remember { mutableStateOf(PaymentGateway.BKASH) }
    var senderNumber by remember { mutableStateOf("") }
    var transactionTrxId by remember { mutableStateOf("") }
    var depositAmountText by remember { mutableStateOf("100") }

    val depositAmount = depositAmountText.toDoubleOrNull() ?: 0.0
    val bonusPercent = settings?.depositBonusPercent ?: 5.0
    val bonusEarned = if (depositAmount > 0) depositAmount * (bonusPercent / 100.0) else 0.0
    val totalToReceive = depositAmount + bonusEarned

    val targetAdminNumber = when (selectedGateway) {
        PaymentGateway.BKASH -> settings?.bkashNumber ?: "01700000000 (Personal/Send Money)"
        PaymentGateway.NAGAD -> settings?.nagadNumber ?: "01800000000 (Personal/Send Money)"
        PaymentGateway.ROCKET -> settings?.rocketNumber ?: "01900000000 (Personal)"
        PaymentGateway.USDT_TRC20 -> settings?.usdtAddress ?: "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
        else -> "01700000000"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramChatBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            // Header card
            CyberCard(
                borderColor = CyberCyan.copy(alpha = 0.6f),
                backgroundColor = TelegramSurface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💳 Add Money / Deposit System",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instant automatic balance recharge with deposit bonus",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonGreen.copy(alpha = 0.15f))
                            .border(1.dp, NeonGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+${bonusPercent.toInt()}% BONUS",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current Balances
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, BorderStroke, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = TelegramDarkBlue)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "CURRENT BALANCE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "৳${String.format("%.2f", user?.mainBalance ?: 0.0)}",
                                color = CyberCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, BorderStroke, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = TelegramDarkBlue)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "WITHDRAWABLE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "৳${String.format("%.2f", user?.withdrawableBalance ?: 0.0)}",
                                color = NeonGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "1. Select Payment Method",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val gateways = listOf(
                    Triple(PaymentGateway.BKASH, "bKash", Color(0xFFE2136E)),
                    Triple(PaymentGateway.NAGAD, "Nagad", Color(0xFFF7941D)),
                    Triple(PaymentGateway.ROCKET, "Rocket", Color(0xFF8C3494)),
                    Triple(PaymentGateway.USDT_TRC20, "USDT", Color(0xFF26A17B))
                )

                gateways.forEach { (gw, label, accentColor) ->
                    val isSelected = selectedGateway == gw
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedGateway = gw }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accentColor else BorderStroke,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) accentColor.copy(alpha = 0.2f) else TelegramSurface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (isSelected) {
                                Text(
                                    text = "SELECTED",
                                    color = accentColor,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "2. Send Money to Official Account",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            CyberCard(
                borderColor = AmberGold.copy(alpha = 0.6f),
                backgroundColor = Color(0xFF1F1A10)
            ) {
                Text(
                    text = "Official ${selectedGateway.name} Deposit Address:",
                    color = AmberGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TelegramDarkBlue)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = targetAdminNumber,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Deposit Number", targetAdminNumber))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "⚠️ Please Send Money / Cash-in to the number above. Then fill the form below with your Sender number & TrxID.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        item {
            Text(
                text = "3. Deposit Details & Verification",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            CyberCard(
                borderColor = BorderStroke
            ) {
                // Deposit Amount Input
                OutlinedTextField(
                    value = depositAmountText,
                    onValueChange = { depositAmountText = it },
                    label = { Text("Deposit Amount (৳)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = BorderStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Text("৳", color = CyberCyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                )

                // Quick amount chips
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("50", "100", "200", "500", "1000").forEach { quickAmount ->
                        Button(
                            onClick = { depositAmountText = quickAmount },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (depositAmountText == quickAmount) CyberCyan else TelegramDarkBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "৳$quickAmount",
                                fontSize = 11.sp,
                                color = if (depositAmountText == quickAmount) TelegramDarkBlue else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sender number
                OutlinedTextField(
                    value = senderNumber,
                    onValueChange = { senderNumber = it },
                    label = { Text("Your Sender Phone Number / Address") },
                    placeholder = { Text("e.g., 017XXXXXXXX") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_sender_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = BorderStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = CyberCyan)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Transaction ID
                OutlinedTextField(
                    value = transactionTrxId,
                    onValueChange = { transactionTrxId = it },
                    label = { Text("Transaction ID / TrxID / Hash") },
                    placeholder = { Text("e.g., 9J8A7K6L5M") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_trxid_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = BorderStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = CyberCyan)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Calculation summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TelegramDarkBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Base Deposit:", color = TextMuted, fontSize = 12.sp)
                            Text("৳${String.format("%.2f", depositAmount)}", color = TextPrimary, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Deposit Bonus (${bonusPercent.toInt()}%):", color = NeonGreen, fontSize = 12.sp)
                            Text("+৳${String.format("%.2f", bonusEarned)}", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = BorderStroke, modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Credited to Wallet:", color = CyberCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("৳${String.format("%.2f", totalToReceive)}", color = CyberCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        viewModel.submitDeposit(
                            gateway = selectedGateway,
                            senderNumber = senderNumber,
                            trxId = transactionTrxId,
                            amount = depositAmount
                        )
                    },
                    enabled = depositAmount > 0 && senderNumber.isNotBlank() && transactionTrxId.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_deposit_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        disabledContainerColor = TelegramDarkBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TelegramDarkBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Deposit Request",
                        color = TelegramDarkBlue,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Text(
                text = "📊 Deposit History (${deposits.size})",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (deposits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No deposit records found.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(deposits, key = { it.id }) { dep ->
                DepositHistoryCard(deposit = dep)
            }
        }
    }
}

@Composable
fun DepositHistoryCard(deposit: DepositEntity) {
    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(deposit.createdAtTimestamp))

    val statusColor = when (deposit.status) {
        DepositStatus.APPROVED -> NeonGreen
        DepositStatus.PENDING -> AmberGold
        DepositStatus.REJECTED -> CoralRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderStroke, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = TelegramSurface),
        shape = RoundedCornerShape(10.dp)
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (deposit.status) {
                                DepositStatus.APPROVED -> Icons.Default.CheckCircle
                                DepositStatus.PENDING -> Icons.Default.HourglassTop
                                DepositStatus.REJECTED -> Icons.Default.Cancel
                            },
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${deposit.gateway.name} Deposit",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = dateStr, color = TextMuted, fontSize = 10.sp)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "+৳${String.format("%.2f", deposit.amount + deposit.bonusAmount)}",
                        color = if (deposit.status == DepositStatus.APPROVED) NeonGreen else TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = deposit.status.name,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = BorderStroke)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TrxID: ${deposit.transactionTrxId}",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Sender: ${deposit.senderNumber}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            if (deposit.adminNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${deposit.adminNote}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
