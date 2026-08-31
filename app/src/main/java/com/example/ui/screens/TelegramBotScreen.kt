package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.BotViewModel
import com.example.ui.viewmodel.TelegramInlineButton
import com.example.ui.viewmodel.TelegramMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramBotScreen(
    viewModel: BotViewModel
) {
    val messages by viewModel.botMessages.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var textInput by remember { mutableStateOf("") }
    var showQuickMenu by remember { mutableStateOf(true) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TelegramSurface
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(TelegramBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Bot Avatar",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Telegram OTP Bot Pro",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Badge",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = "bot • High Security Active 🟢",
                                fontSize = 11.sp,
                                color = NeonGreen
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showQuickMenu = !showQuickMenu },
                        modifier = Modifier.testTag("toggle_keyboard_btn")
                    ) {
                        Icon(
                            imageVector = if (showQuickMenu) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                            contentDescription = "Toggle Keyboard",
                            tint = CyberCyan
                        )
                    }
                }
            )
        },
        containerColor = TelegramChatBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Chat Messages Feed
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        onInlineClick = { callback ->
                            viewModel.handleInlineCallback(callback)
                        }
                    )
                }
            }

            // Telegram Reply Keyboard / Fast Action Grid
            if (showQuickMenu) {
                Surface(
                    color = TelegramSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ReplyKeyButton("💰 My Balance", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/balance")
                            }
                            ReplyKeyButton("📞 Get Number", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/getnumber")
                            }
                            ReplyKeyButton("🔢 My OTP", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/otp")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ReplyKeyButton("🎁 Daily Bonus", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/bonus")
                            }
                            ReplyKeyButton("👥 Refer & Earn", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/refer")
                            }
                            ReplyKeyButton("📋 Tasks", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/tasks")
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ReplyKeyButton("💸 Withdraw", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/withdraw")
                            }
                            ReplyKeyButton("🔐 Security", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/security")
                            }
                            ReplyKeyButton("👨‍💼 Admin Panel", Modifier.weight(1f)) {
                                viewModel.handleTelegramCommand("/admin")
                            }
                        }
                    }
                }
            }

            // Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TelegramSurface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Write a message or /command...", fontSize = 13.sp, color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("bot_message_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TelegramCardBg,
                        unfocusedContainerColor = TelegramCardBg,
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = BorderStroke,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            val cmd = textInput
                            textInput = ""
                            viewModel.handleTelegramCommand(cmd)
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(TelegramBlue)
                        .testTag("send_bot_message_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ReplyKeyButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TelegramCardBg),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun ChatBubbleItem(
    message: TelegramMessage,
    onInlineClick: (String) -> Unit
) {
    val isBot = message.isBot
    val timeStr = remember(message.timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isBot) Arrangement.Start else Arrangement.End
    ) {
        Column(
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isBot) TelegramBubbleIncoming else TelegramBubbleOutgoing
                ),
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isBot) 2.dp else 14.dp,
                    bottomEnd = if (isBot) 14.dp else 2.dp
                ),
                modifier = Modifier
                    .border(
                        1.dp,
                        if (isBot) BorderStroke else TelegramBlue.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Clean HTML formatting tags
                    val cleanText = message.text
                        .replace("<b>", "")
                        .replace("</b>", "")
                        .replace("<i>", "")
                        .replace("</i>", "")
                        .replace("<code>", "")
                        .replace("</code>", "")

                    Text(
                        text = cleanText,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timeStr,
                        color = if (isBot) TextMuted else CyberCyan.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // Inline Buttons
            if (message.inlineButtons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    message.inlineButtons.chunked(2).forEach { rowButtons ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowButtons.forEach { btn ->
                                Button(
                                    onClick = { onInlineClick(btn.callbackData) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TelegramSurface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = btn.label,
                                        color = CyberCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
