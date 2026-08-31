package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.data.models.CountryEntity
import com.example.data.models.NumberOrderEntity
import com.example.data.models.OrderStatus
import com.example.data.models.ServiceEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BotViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberOtpScreen(
    viewModel: BotViewModel
) {
    val context = LocalContext.current
    val countries by viewModel.availableCountries.collectAsState()
    val services by viewModel.availableServices.collectAsState()
    val orders by viewModel.userOrders.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCountry by remember { mutableStateOf<CountryEntity?>(null) }
    var selectedService by remember { mutableStateOf<ServiceEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(countries, services) {
        if (selectedCountry == null && countries.isNotEmpty()) {
            selectedCountry = countries.first()
        }
        if (selectedService == null && services.isNotEmpty()) {
            selectedService = services.first()
        }
    }

    val activeOrders = orders.filter { it.status == OrderStatus.WAITING_OTP || it.status == OrderStatus.RECEIVED }
    val historyOrders = orders.filter { it.status != OrderStatus.WAITING_OTP && it.status != OrderStatus.RECEIVED }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramChatBg)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = TelegramSurface,
            contentColor = CyberCyan,
            divider = { HorizontalDivider(color = BorderStroke) }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AddIcCall, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Get Number", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("My Active OTP (${activeOrders.size})", fontWeight = FontWeight.Bold)
                    }
                }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("History", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        when (selectedTabIndex) {
            0 -> GetNumberWizardTab(
                countries = countries,
                services = services,
                selectedCountry = selectedCountry,
                selectedService = selectedService,
                userBalance = user?.withdrawableBalance ?: 0.0,
                onCountrySelect = { selectedCountry = it },
                onServiceSelect = { selectedService = it },
                onBuyClick = { country, service ->
                    viewModel.buyNumber(country, service)
                    selectedTabIndex = 1
                }
            )
            1 -> ActiveOtpTab(
                activeOrders = activeOrders,
                onSimulateOtp = { viewModel.simulateOtpArrival(it) },
                onCancelRefund = { viewModel.cancelAndRefundOrder(it) },
                onCopyCode = { code ->
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("OTP Code", code)
                    clipboard.setPrimaryClip(clip)
                }
            )
            2 -> OrderHistoryTab(orders = historyOrders)
        }
    }
}

@Composable
fun GetNumberWizardTab(
    countries: List<CountryEntity>,
    services: List<ServiceEntity>,
    selectedCountry: CountryEntity?,
    selectedService: ServiceEntity?,
    userBalance: Double,
    onCountrySelect: (CountryEntity) -> Unit,
    onServiceSelect: (ServiceEntity) -> Unit,
    onBuyClick: (CountryEntity, ServiceEntity) -> Unit
) {
    val finalPrice = remember(selectedCountry, selectedService) {
        if (selectedCountry != null && selectedService != null) {
            selectedService.basePrice * selectedCountry.stockMultiplier
        } else {
            0.0
        }
    }
    val hasEnoughBalance = userBalance >= finalPrice

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Step 1: Country Picker
        item {
            Text(
                text = "1️⃣ Select Country",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(countries, key = { it.code }) { c ->
                    val isSelected = selectedCountry?.code == c.code
                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCountrySelect(c) }
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
                            Text(text = c.flagEmoji, fontSize = 26.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = c.name,
                                color = if (isSelected) CyberCyan else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = c.phonePrefix,
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Step 2: Service Selector
        item {
            Text(
                text = "2️⃣ Select Service / App",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(services.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                pair.forEach { s ->
                    val isSelected = selectedService?.serviceCode == s.serviceCode
                    val price = if (selectedCountry != null) s.basePrice * selectedCountry.stockMultiplier else s.basePrice

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onServiceSelect(s) }
                            .border(
                                1.5.dp,
                                if (isSelected) CyberCyan else BorderStroke,
                                RoundedCornerShape(14.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TelegramCardBg else TelegramSurface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = s.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = s.name,
                                    color = if (isSelected) CyberCyan else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "৳${String.format("%.2f", price)}",
                                    color = NeonGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Step 3: Purchase Summary & Action
        item {
            Spacer(modifier = Modifier.height(6.dp))
            CyberCard(
                borderColor = if (hasEnoughBalance) CyberCyan.copy(alpha = 0.5f) else CrimsonRed.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Order Summary",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${selectedService?.name ?: "Service"} (${selectedCountry?.flagEmoji ?: ""} ${selectedCountry?.name ?: "Country"})",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Price",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "৳ ${String.format("%.2f", finalPrice)}",
                            color = NeonGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your Balance: ৳${String.format("%.2f", userBalance)}",
                        color = if (hasEnoughBalance) TextSecondary else CrimsonRed,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = {
                            if (selectedCountry != null && selectedService != null) {
                                onBuyClick(selectedCountry, selectedService)
                            }
                        },
                        enabled = hasEnoughBalance && selectedCountry != null && selectedService != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TelegramBlue,
                            disabledContainerColor = BorderStroke
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("buy_number_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Buy Number",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Instant Buy", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveOtpTab(
    activeOrders: List<NumberOrderEntity>,
    onSimulateOtp: (Long) -> Unit,
    onCancelRefund: (Long) -> Unit,
    onCopyCode: (String) -> Unit
) {
    if (activeOrders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PhoneDisabled,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No Active Numbers Waiting for OTP",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Purchase a number from the 'Get Number' tab to receive instant SMS.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(activeOrders, key = { it.id }) { order ->
                ActiveOrderCard(
                    order = order,
                    onSimulateOtp = { onSimulateOtp(order.id) },
                    onCancelRefund = { onCancelRefund(order.id) },
                    onCopyCode = onCopyCode
                )
            }
        }
    }
}

@Composable
fun ActiveOrderCard(
    order: NumberOrderEntity,
    onSimulateOtp: () -> Unit,
    onCancelRefund: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    val isOtpReceived = order.status == OrderStatus.RECEIVED && order.otpCode != null

    CyberCard(
        borderColor = if (isOtpReceived) NeonGreen else CyberCyan,
        backgroundColor = if (isOtpReceived) Color(0xFF0D2818) else TelegramSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = order.countryFlag, fontSize = 24.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "${order.serviceName} • ${order.countryName}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ref: ${order.orderId}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
            StatusBadge(status = order.status.name)
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phone number box
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
                Column {
                    Text(text = "ASSIGNED PHONE NUMBER", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = order.assignedNumber,
                        color = CyberCyan,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(
                    onClick = { onCopyCode(order.assignedNumber) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Number",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // OTP Display or Waiting indicator
        if (isOtpReceived) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF052E16))
                    .border(1.dp, NeonGreen, RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RECEIVED VERIFICATION OTP",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.otpCode ?: "------",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 6.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (!order.fullSmsText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📩 \"${order.fullSmsText}\"",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onCopyCode(order.otpCode ?: "") },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TelegramDarkBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy OTP Code", color = TelegramDarkBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Waiting pulse indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TelegramDarkBlue.copy(alpha = 0.6f))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = CyberCyan,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "100% রিয়েল ক্যারিয়ার রুট অনলাইন: অটো ওটিপি চেক চালু আছে...",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Real-time carrier route auto-delivery in progress",
                        color = CyberCyan,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSimulateOtp,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TelegramBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Trigger OTP (Simulate)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCancelRefund,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel & Refund", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryTab(orders: List<NumberOrderEntity>) {
    if (orders.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No past orders in history.",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(orders, key = { it.id }) { order ->
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = order.countryFlag, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "${order.serviceName} (${order.assignedNumber})",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Cost: ৳${order.cost} • Code: ${order.otpCode ?: "None"}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        StatusBadge(status = order.status.name)
                    }
                }
            }
        }
    }
}
