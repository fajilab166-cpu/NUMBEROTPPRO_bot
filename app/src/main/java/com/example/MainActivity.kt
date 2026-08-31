package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavigationScreen
import com.example.ui.viewmodel.BotViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TelegramBotTheme {
                val botViewModel: BotViewModel = viewModel()
                MainAppScaffold(viewModel = botViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: BotViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val user by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TelegramChatBg,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 70.dp)
            ) { data ->
                Snackbar(
                    containerColor = TelegramCardBg,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    Text(text = data.visuals.message, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        },
        topBar = {
            if (currentScreen != AppNavigationScreen.TELEGRAM_BOT) {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TelegramSurface
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentScreen) {
                                    AppNavigationScreen.DASHBOARD -> "📊 Dashboard & Wallet"
                                    AppNavigationScreen.DEPOSIT -> "💳 Add Money / Deposit"
                                    AppNavigationScreen.GET_NUMBER, AppNavigationScreen.MY_OTP -> "📞 Number & OTP Hub"
                                    AppNavigationScreen.INCOME_TASKS -> "💰 Earn & Tasks"
                                    AppNavigationScreen.WITHDRAW -> "💸 Instant Withdrawal"
                                    AppNavigationScreen.SECURITY -> "🔐 Security Center"
                                    AppNavigationScreen.ADMIN_PANEL -> "👨‍💼 Admin Panel"
                                    else -> "Telegram OTP Bot Pro"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.navigateTo(AppNavigationScreen.TELEGRAM_BOT) }) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Open Bot Chat",
                                tint = CyberCyan
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = TelegramSurface,
                contentColor = CyberCyan,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(1.dp, BorderStroke)
                    .testTag("main_navigation_bar")
            ) {
                NavBarItem(
                    selected = currentScreen == AppNavigationScreen.TELEGRAM_BOT,
                    onClick = { viewModel.navigateTo(AppNavigationScreen.TELEGRAM_BOT) },
                    icon = Icons.Default.SmartToy,
                    label = "Bot Chat",
                    testTag = "nav_tab_bot"
                )
                NavBarItem(
                    selected = currentScreen == AppNavigationScreen.DASHBOARD || currentScreen == AppNavigationScreen.DEPOSIT,
                    onClick = { viewModel.navigateTo(AppNavigationScreen.DASHBOARD) },
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Wallet",
                    testTag = "nav_tab_wallet"
                )
                NavBarItem(
                    selected = currentScreen == AppNavigationScreen.GET_NUMBER || currentScreen == AppNavigationScreen.MY_OTP,
                    onClick = { viewModel.navigateTo(AppNavigationScreen.GET_NUMBER) },
                    icon = Icons.Default.PhoneIphone,
                    label = "Numbers",
                    testTag = "nav_tab_numbers"
                )
                NavBarItem(
                    selected = currentScreen == AppNavigationScreen.INCOME_TASKS,
                    onClick = { viewModel.navigateTo(AppNavigationScreen.INCOME_TASKS) },
                    icon = Icons.Default.MonetizationOn,
                    label = "Earn",
                    testTag = "nav_tab_earn"
                )
                NavBarItem(
                    selected = currentScreen == AppNavigationScreen.WITHDRAW,
                    onClick = { viewModel.navigateTo(AppNavigationScreen.WITHDRAW) },
                    icon = Icons.Default.Payments,
                    label = "Withdraw",
                    testTag = "nav_tab_withdraw"
                )
                NavBarItem(
                    selected = currentScreen == AppNavigationScreen.ADMIN_PANEL,
                    onClick = { viewModel.navigateTo(AppNavigationScreen.ADMIN_PANEL) },
                    icon = Icons.Default.AdminPanelSettings,
                    label = "Admin",
                    testTag = "nav_tab_admin"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                AppNavigationScreen.TELEGRAM_BOT -> TelegramBotScreen(viewModel = viewModel)
                AppNavigationScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppNavigationScreen.DEPOSIT -> DepositScreen(viewModel = viewModel)
                AppNavigationScreen.GET_NUMBER, AppNavigationScreen.MY_OTP -> NumberOtpScreen(viewModel = viewModel)
                AppNavigationScreen.INCOME_TASKS -> IncomeTasksScreen(viewModel = viewModel)
                AppNavigationScreen.WITHDRAW -> WithdrawScreen(viewModel = viewModel)
                AppNavigationScreen.SECURITY -> SecurityScreen(viewModel = viewModel)
                AppNavigationScreen.ADMIN_PANEL -> AdminPanelScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun RowScope.NavBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    testTag: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(22.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = CyberCyan,
            selectedTextColor = CyberCyan,
            unselectedIconColor = TextMuted,
            unselectedTextColor = TextMuted,
            indicatorColor = TelegramCardBg
        ),
        modifier = Modifier.testTag(testTag)
    )
}
