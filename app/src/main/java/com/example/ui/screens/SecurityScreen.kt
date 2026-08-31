package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.RiskLevel
import com.example.data.models.SecurityLogEntity
import com.example.data.models.Severity
import com.example.ui.components.CyberCard
import com.example.ui.components.RiskBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BotViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SecurityScreen(
    viewModel: BotViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val securityLogs by viewModel.securityLogs.collectAsState()
    val settings by viewModel.systemSettings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramChatBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Overall Security Shield Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(CyberCyan, NeonGreen)),
                        RoundedCornerShape(18.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = TelegramSurface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F2636), TelegramSurface)
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Shield",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Security Profile",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Protection Level: Maximum",
                                    color = NeonGreen,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        RiskBadge(
                            riskScore = user?.riskScore ?: 12,
                            riskLevel = user?.riskLevel ?: RiskLevel.LOW
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key Security Details
                    SecurityDetailRow("Telegram User ID", "${user?.userId ?: 684920194L}")
                    SecurityDetailRow("Session Token", user?.sessionToken ?: "tg_sec_tok_99182a")
                    SecurityDetailRow("2FA Authentication", if (user?.is2faEnabled == true) "Enabled 🟢" else "Disabled 🔴")
                    SecurityDetailRow("Anti-Spam Sliding Window", "30 req / min • Active 🟢")
                    SecurityDetailRow("Idempotency Engine", "SHA-256 Request Locking 🟢")
                }
            }
        }

        // Threat Mitigation Status Grid
        item {
            Text(
                text = "🛡️ Active Protection Engines",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProtectionEngineRow(
                    name = "Anti-Fraud AI Scoring",
                    desc = "Monitors duplicate referrals, abnormal withdrawals, and timing anomalies.",
                    status = "ONLINE",
                    statusColor = NeonGreen
                )
                ProtectionEngineRow(
                    name = "Server-Side Token Validator",
                    desc = "Prevents client-side script spoofing & forged video task claims.",
                    status = "ONLINE",
                    statusColor = NeonGreen
                )
                ProtectionEngineRow(
                    name = "SMS Provider Encrypted Bridge",
                    desc = "Keeps API keys secret on backend; only delivery OTPs exposed to owner.",
                    status = "ONLINE",
                    statusColor = NeonGreen
                )
            }
        }

        // Live Audit & Security Log Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📝 Real-Time Security Audit Logs",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${securityLogs.size} events",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }

        if (securityLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No security events recorded.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(securityLogs, key = { it.id }) { log ->
                SecurityLogRow(log = log)
            }
        }
    }
}

@Composable
fun SecurityDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 11.sp)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ProtectionEngineRow(
    name: String,
    desc: String,
    status: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderStroke, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = TelegramSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = desc, color = TextMuted, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = status, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SecurityLogRow(log: SecurityLogEntity) {
    val timeStr = remember(log.timestamp) {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    }

    val (sevColor, sevBg) = when (log.severity) {
        Severity.INFO -> Pair(CyberCyan, TelegramDarkBlue)
        Severity.WARNING -> Pair(AmberGold, Color(0xFF451A03))
        Severity.CRITICAL -> Pair(CrimsonRed, Color(0xFF450A0A))
        Severity.EMERGENCY -> Pair(CrimsonRed, Color(0xFF7F1D1D))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderStroke, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = TelegramSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(sevBg)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(text = log.severity.name, color = sevColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.action,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeStr,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = log.details,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}
