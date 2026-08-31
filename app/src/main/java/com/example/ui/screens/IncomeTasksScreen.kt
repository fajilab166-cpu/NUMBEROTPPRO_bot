package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.models.ReferralEntity
import com.example.data.models.TaskCategory
import com.example.data.models.TaskEntity
import com.example.ui.components.CyberCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveMonetagTaskState
import com.example.ui.viewmodel.BotViewModel

@Composable
fun IncomeTasksScreen(
    viewModel: BotViewModel
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val tasks by viewModel.activeTasks.collectAsState()
    val referrals by viewModel.userReferrals.collectAsState()
    val activeMonetagState by viewModel.activeMonetagTask.collectAsState()

    var selectedTab by remember { mutableIntStateOf(1) } // Default to tasks
    var selectedCategoryFilter by remember { mutableStateOf<TaskCategory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramChatBg)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TelegramSurface,
            contentColor = CyberCyan,
            divider = { HorizontalDivider(color = BorderStroke) }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Daily Bonus", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Task, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tasks (${tasks.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refer & Earn", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            )
        }

        when (selectedTab) {
            0 -> DailyBonusTab(
                userStreak = user?.dailyStreak ?: 1,
                onClaimBonus = { viewModel.claimDailyBonus() }
            )
            1 -> TasksTab(
                tasks = if (selectedCategoryFilter != null) tasks.filter { it.category == selectedCategoryFilter } else tasks,
                allTasksCount = tasks.size,
                selectedFilter = selectedCategoryFilter,
                onFilterSelected = { selectedCategoryFilter = it },
                activeMonetagState = activeMonetagState,
                onStartMonetagTask = { task ->
                    viewModel.startMonetagAdTask(task)
                },
                onCompleteSocialTask = { task ->
                    viewModel.completeSocialTask(task)
                },
                onDismissAd = {
                    viewModel.dismissMonetagTask()
                }
            )
            2 -> ReferralsTab(
                referralCode = user?.referralCode ?: "TGPRO889",
                totalReferrals = user?.totalReferrals ?: 0,
                referrals = referrals,
                onCopyLink = { link ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Referral Link", link))
                },
                onSimulateReferral = { viewModel.simulateFriendReferral() }
            )
        }
    }
}

@Composable
fun DailyBonusTab(
    userStreak: Int,
    onClaimBonus: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(AmberGold, Color(0xFFF97316))),
                        RoundedCornerShape(18.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = TelegramSurface),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF261D10), TelegramSurface)
                            )
                        )
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(AmberGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🎁", fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Daily Login Reward",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Claim your free wallet reward every 24 hours. Consecutive daily streaks increase your bonus multiplier!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Streak day markers (1 to 7)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..7).forEach { day ->
                            val isCompleted = day <= userStreak
                            val isCurrent = day == userStreak
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCompleted) AmberGold else TelegramCardBg
                                        )
                                        .border(
                                            1.dp,
                                            if (isCurrent) CyberCyan else BorderStroke,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isCompleted) "✓" else "Day",
                                        color = if (isCompleted) TelegramDarkBlue else TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "D$day",
                                    color = if (isCompleted) AmberGold else TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onClaimBonus,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("claim_daily_bonus_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = TelegramDarkBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Claim Daily Bonus (৳10.00+)",
                            color = TelegramDarkBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TasksTab(
    tasks: List<TaskEntity>,
    allTasksCount: Int,
    selectedFilter: TaskCategory?,
    onFilterSelected: (TaskCategory?) -> Unit,
    activeMonetagState: ActiveMonetagTaskState?,
    onStartMonetagTask: (TaskEntity) -> Unit,
    onCompleteSocialTask: (TaskEntity) -> Unit,
    onDismissAd: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Active Monetag Ad Live Viewer
        if (activeMonetagState != null) {
            item {
                MonetagAdRunnerCard(
                    activeState = activeMonetagState,
                    onDismiss = onDismissAd
                )
            }
        }

        // Category Filter Chips
        item {
            Column {
                Text(
                    text = "📋 Task Categories",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { onFilterSelected(null) },
                        label = { Text("All ($allTasksCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = TelegramDarkBlue,
                            containerColor = TelegramSurface,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == TaskCategory.MONETAG_AD,
                        onClick = { onFilterSelected(TaskCategory.MONETAG_AD) },
                        label = { Text("Monetag Ads ⚡", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = TelegramDarkBlue,
                            containerColor = TelegramSurface,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == TaskCategory.TELEGRAM_CHANNEL,
                        onClick = { onFilterSelected(TaskCategory.TELEGRAM_CHANNEL) },
                        label = { Text("Telegram ✈️", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = TelegramDarkBlue,
                            containerColor = TelegramSurface,
                            labelColor = TextSecondary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == TaskCategory.YOUTUBE_SUBSCRIBE,
                        onClick = { onFilterSelected(TaskCategory.YOUTUBE_SUBSCRIBE) },
                        label = { Text("YouTube ▶️", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = TelegramDarkBlue,
                            containerColor = TelegramSurface,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == TaskCategory.FACEBOOK_PAGE,
                        onClick = { onFilterSelected(TaskCategory.FACEBOOK_PAGE) },
                        label = { Text("Facebook 📘", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = TelegramDarkBlue,
                            containerColor = TelegramSurface,
                            labelColor = TextSecondary
                        )
                    )
                    FilterChip(
                        selected = selectedFilter == TaskCategory.TWITTER_FOLLOW,
                        onClick = { onFilterSelected(TaskCategory.TWITTER_FOLLOW) },
                        label = { Text("Twitter / X 🐦", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CyberCyan,
                            selectedLabelColor = TelegramDarkBlue,
                            containerColor = TelegramSurface,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        if (tasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tasks available in this category.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            items(tasks, key = { it.id }) { task ->
                TaskItemCard(
                    task = task,
                    onStart = {
                        if (task.category == TaskCategory.MONETAG_AD) {
                            onStartMonetagTask(task)
                        } else {
                            onCompleteSocialTask(task)
                        }
                    }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MonetagAdRunnerCard(
    activeState: ActiveMonetagTaskState,
    onDismiss: () -> Unit
) {
    CyberCard(
        borderColor = CyberCyan,
        backgroundColor = Color(0xFF0C182A)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SmartDisplay, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "MONETAG AD ZONE #${activeState.zoneId}",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "${activeState.remainingSeconds}s remaining",
                color = NeonGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress bar
        val progress = 1f - (activeState.remainingSeconds.toFloat() / activeState.totalDuration.coerceAtLeast(1).toFloat())
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CyberCyan,
            trackColor = TelegramDarkBlue
        )

        Spacer(modifier = Modifier.height(10.dp))

        // WebView hosting Monetag SDK
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, BorderStroke, RoundedCornerShape(8.dp))
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        webChromeClient = WebChromeClient()
                        webViewClient = WebViewClient()

                        val htmlData = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <style>
                                    body {
                                        background-color: #0b141f;
                                        color: #00d2ff;
                                        font-family: sans-serif;
                                        display: flex;
                                        flex-direction: column;
                                        justify-content: center;
                                        align-items: center;
                                        height: 100vh;
                                        margin: 0;
                                        padding: 10px;
                                        box-sizing: border-box;
                                        text-align: center;
                                    }
                                    .badge {
                                        background: #1e3a5f;
                                        color: #00ff88;
                                        padding: 4px 10px;
                                        border-radius: 6px;
                                        font-size: 11px;
                                        font-weight: bold;
                                        margin-bottom: 8px;
                                    }
                                    .info {
                                        color: #8da5be;
                                        font-size: 11px;
                                    }
                                </style>
                                <script src='//libtl.com/sdk.js' data-zone='${activeState.zoneId}' data-sdk='show_${activeState.zoneId}'></script>
                            </head>
                            <body>
                                <div class="badge">MONETAG OFFICIAL AD SDK</div>
                                <div style="font-size: 13px; font-weight: bold; color: #fff;">Impression Zone: ${activeState.zoneId}</div>
                                <p class="info">Ad is rendering securely. Reward auto-credits when timer reaches 0s.</p>
                            </body>
                            </html>
                        """.trimIndent()

                        loadDataWithBaseURL("https://libtl.com", htmlData, "text/html", "UTF-8", null)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Secure Monetag Ad Impression Verification Active",
                color = TextSecondary,
                fontSize = 10.sp
            )

            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("Cancel", color = CoralRed, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onStart: () -> Unit
) {
    CyberCard(
        borderColor = BorderStroke
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when (task.category) {
                                TaskCategory.MONETAG_AD -> CyberCyan.copy(alpha = 0.2f)
                                TaskCategory.TELEGRAM_CHANNEL -> TelegramBlue.copy(alpha = 0.2f)
                                TaskCategory.YOUTUBE_SUBSCRIBE -> CoralRed.copy(alpha = 0.2f)
                                TaskCategory.FACEBOOK_PAGE -> Color(0xFF1877F2).copy(alpha = 0.2f)
                                TaskCategory.TWITTER_FOLLOW -> Color(0xFF1DA1F2).copy(alpha = 0.2f)
                                else -> TelegramBlue.copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (task.category) {
                            TaskCategory.MONETAG_AD -> Icons.Default.SmartDisplay
                            TaskCategory.TELEGRAM_CHANNEL -> Icons.Default.Send
                            TaskCategory.YOUTUBE_SUBSCRIBE -> Icons.Default.PlayArrow
                            TaskCategory.FACEBOOK_PAGE -> Icons.Default.ThumbUp
                            TaskCategory.TWITTER_FOLLOW -> Icons.Default.ChatBubble
                            TaskCategory.INSTAGRAM_FOLLOW, TaskCategory.TIKTOK_FOLLOW -> Icons.Default.VerifiedUser
                            TaskCategory.WEBSITE_VISIT -> Icons.Default.Language
                            else -> Icons.Default.Task
                        },
                        contentDescription = null,
                        tint = when (task.category) {
                            TaskCategory.MONETAG_AD -> CyberCyan
                            TaskCategory.YOUTUBE_SUBSCRIBE -> CoralRed
                            TaskCategory.FACEBOOK_PAGE -> Color(0xFF1877F2)
                            TaskCategory.TWITTER_FOLLOW -> Color(0xFF1DA1F2)
                            else -> CyberCyan
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.title,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(TelegramDarkBlue)
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = task.platformName,
                                color = CyberCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = task.description,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+৳${String.format("%.2f", task.rewardAmount)}",
                    color = NeonGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Limit: ${task.currentCompletions}/${task.dailyLimit}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (task.category == TaskCategory.MONETAG_AD) CyberCyan else TelegramBlue
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (task.category == TaskCategory.MONETAG_AD) "View Ad ⚡" else "Complete Task",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.category == TaskCategory.MONETAG_AD) TelegramDarkBlue else Color.White
                )
            }
        }
    }
}

@Composable
fun ReferralsTab(
    referralCode: String,
    totalReferrals: Int,
    referrals: List<ReferralEntity>,
    onCopyLink: (String) -> Unit,
    onSimulateReferral: () -> Unit
) {
    val referralLink = "https://t.me/TelegramOtpBotPro?start=$referralCode"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            CyberCard(
                borderColor = CyberCyan.copy(alpha = 0.5f),
                backgroundColor = TelegramSurface
            ) {
                Text(
                    text = "👥 Refer & Earn Program",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Earn ৳20.00 instantly for every active friend you invite, plus 10% lifetime commission from their OTP orders!",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Link copy box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TelegramDarkBlue)
                        .border(1.dp, BorderStroke, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "YOUR REFERRAL LINK", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = referralLink,
                                color = CyberCyan,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = { onCopyLink(referralLink) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Link",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onCopyLink(referralCode) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TelegramCardBg),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Code: $referralCode", color = TextPrimary, fontSize = 12.sp)
                    }

                    Button(
                        onClick = onSimulateReferral,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Simulate Invite 🤝", color = TelegramDarkBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text(
                text = "👥 Referred Users (${referrals.size})",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (referrals.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No referred users yet. Share your link to start earning!",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            items(referrals, key = { it.id }) { ref ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderStroke, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = TelegramSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
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
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "@${ref.refereeUsername}",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "ID: ${ref.refereeId}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "+৳${ref.bonusEarned}",
                            color = NeonGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

