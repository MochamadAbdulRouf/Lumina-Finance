package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.BudgetSettings
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Formatter for Indonesian Rupiah
fun formatRupiah(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount).replace("Rp", "Rp ")
}

// Convert timestamp to readable text
fun formatDateTime(timestamp: Long, format: String = "dd MMM yyyy, HH:mm"): String {
    val sdf = SimpleDateFormat(format, Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}

@Composable
fun FinanceApp(viewModel: FinanceViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val budgetSettings by viewModel.budgetSettings.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var currentScreen by remember { mutableStateOf("auth") }
    var currentTab by remember { mutableStateOf("home") }

    // Navigation logic based on login state
    LaunchedEffect(userProfile.isLoggedIn) {
        if (userProfile.isLoggedIn) {
            currentScreen = "main"
        } else {
            currentScreen = "auth"
        }
    }

    Scaffold(
        containerColor = BrandNavyBlack,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "auth" -> AuthScreen(viewModel)
                "main" -> MainShell(
                    viewModel = viewModel,
                    currentTab = currentTab,
                    onTabChanged = { currentTab = it },
                    onNavigateToSubscreen = { currentScreen = it }
                )
                "edit_account" -> EditAccountScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "main"; currentTab = "profile" }
                )
                "level_progress" -> LevelProgressScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "main"; currentTab = "profile" }
                )
                "security" -> SecurityScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "main"; currentTab = "profile" }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: FinanceViewModel) {
    val email by viewModel.authEmail.collectAsState()
    val password by viewModel.authPassword.collectAsState()
    val isSignUp by viewModel.isSignUpMode.collectAsState()
    var isPasswordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Abstract Top Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BrandLime)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Wallet,
                    contentDescription = "Wallet Logo",
                    tint = BrandNavyBlack,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Finance",
                color = BrandLime,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.02).sp
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Titles
        Text(
            text = "Welcome Back",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = (-0.01).sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Sign in to access your trading floor",
            color = TextGrayMuted,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Tab Swapper Sign In / Sign Up
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF121C2A))
                .border(1.dp, Color(0xFF1F2937).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isSignUp) Color(0xFF212B39) else Color.Transparent)
                    .clickable { viewModel.isSignUpMode.value = false }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign In",
                    color = if (!isSignUp) Color.White else TextGrayMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSignUp) Color(0xFF212B39) else Color.Transparent)
                    .clickable { viewModel.isSignUpMode.value = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign Up",
                    color = if (isSignUp) Color.White else TextGrayMuted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Fields
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Email Input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Email Address",
                    color = TextGrayMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.05.sp
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.authEmail.value = it },
                    placeholder = { Text("name@domain.com", color = TextGrayMuted.copy(alpha = 0.5f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Email,
                            contentDescription = "Email",
                            tint = TextGrayMuted
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = BrandSurfaceNavy,
                        unfocusedContainerColor = BrandSurfaceNavy,
                        focusedBorderColor = BrandLime,
                        unfocusedBorderColor = BrandBorderSlate
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Password Input
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Password",
                        color = TextGrayMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp
                    )
                    Text(
                        text = "Forgot Password?",
                        color = TextGrayMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.clickable {
                            Toast.makeText(context, "Password recovery is disabled in sandbox.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.authPassword.value = it },
                    placeholder = { Text("••••••••", color = TextGrayMuted.copy(alpha = 0.5f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "Password",
                            tint = TextGrayMuted
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Toggle password",
                                tint = TextGrayMuted
                            )
                        }
                    },
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = BrandSurfaceNavy,
                        unfocusedContainerColor = BrandSurfaceNavy,
                        focusedBorderColor = BrandLime,
                        unfocusedBorderColor = BrandBorderSlate
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Login Button
        Button(
            onClick = {
                if (isSignUp) {
                    viewModel.registerUser(
                        onSuccess = {
                            Toast.makeText(context, "Registered successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                } else {
                    viewModel.loginUser(
                        onSuccess = {
                            Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandLime,
                contentColor = BrandNavyBlack
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isSignUp) "Create Account" else "Sign In",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        // Abstract visual footer lines representing trading volume
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C1726))
                .border(1.dp, Color(0xFF1F2937).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.height(48.dp)
            ) {
                val waveHeights = listOf(0.3f, 0.6f, 0.4f, 0.8f, 0.5f, 1f, 0.6f, 0.7f)
                waveHeights.forEachIndexed { idx, height ->
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight(height)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (idx == 5) BrandLime else BrandLime.copy(alpha = 0.3f + (idx * 0.05f))
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun MainShell(
    viewModel: FinanceViewModel,
    currentTab: String,
    onTabChanged: (String) -> Unit,
    onNavigateToSubscreen: (String) -> Unit
) {
    var showLogExpenseDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Tab display
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    "home" -> HomeScreen(
                        viewModel = viewModel,
                        onOpenLogExpense = { showLogExpenseDialog = true }
                    )
                    "analytics" -> SpendingAnalyticsScreen(viewModel)
                    "wallet" -> MyWalletScreen(viewModel)
                    "profile" -> MyProfileScreen(viewModel, onNavigateToSubscreen)
                }
            }

            // Bottom Navigation Layout
            Surface(
                color = BrandSurfaceNavy,
                border = BorderStroke(1.dp, BrandBorderSlate.copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavTabItem(
                        icon = if (currentTab == "home") Icons.Filled.Home else Icons.Outlined.Home,
                        label = "Home",
                        isActive = currentTab == "home",
                        onClick = { onTabChanged("home") }
                    )
                    BottomNavTabItem(
                        icon = if (currentTab == "analytics") Icons.Filled.Insights else Icons.Outlined.Insights,
                        label = "Analytics",
                        isActive = currentTab == "analytics",
                        onClick = { onTabChanged("analytics") }
                    )
                    BottomNavTabItem(
                        icon = if (currentTab == "wallet") Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                        label = "Wallet",
                        isActive = currentTab == "wallet",
                        onClick = { onTabChanged("wallet") }
                    )
                    BottomNavTabItem(
                        icon = if (currentTab == "profile") Icons.Filled.Person else Icons.Outlined.Person,
                        label = "Profile",
                        isActive = currentTab == "profile",
                        onClick = { onTabChanged("profile") }
                    )
                }
            }
        }

        // Floating Action Button for Home
        if (currentTab == "home") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = { showLogExpenseDialog = true },
                    containerColor = BrandLime,
                    contentColor = Color(0xFF381E72),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("floating_add_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Log Expense",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Animated Log Expense dialog
        if (showLogExpenseDialog) {
            LogExpenseDialog(
                viewModel = viewModel,
                onDismiss = { showLogExpenseDialog = false }
            )
        }
    }
}

@Composable
fun RowScope.BottomNavTabItem(
    icon: ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (isActive) Color(0xFF4A4458) else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color(0xFFE8DEF8) else TextGrayMuted
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) Color(0xFFE8DEF8) else TextGrayMuted,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: FinanceViewModel,
    onOpenLogExpense: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val budgetSettings by viewModel.budgetSettings.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf<Transaction?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BrandLime)
                        .clickable { onOpenLogExpense() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.name.take(2).uppercase(),
                        color = BrandNavyBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hello, ${userProfile.name}!",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                    Text(
                        text = formatDateTime(System.currentTimeMillis(), "EEEE, dd MMMM yyyy"),
                        color = TextGrayMuted,
                        fontSize = 10.sp,
                        letterSpacing = 0.05.sp
                    )
                }
            }

            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Budget Card Block
            item {
                Surface(
                    color = BrandLimeLight,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp)) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFF21005D))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "SAFE",
                                    color = BrandLime,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.sp
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circular gauge
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                Canvas(modifier = Modifier.size(100.dp)) {
                                    drawCircle(
                                        color = Color(0xFF21005D).copy(alpha = 0.15f),
                                        style = Stroke(width = 8.dp.toPx())
                                    )
                                    // 75% indicator
                                    drawArc(
                                        color = Color(0xFF21005D),
                                        startAngle = -90f,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Text(
                                    text = "75%",
                                    color = Color(0xFF21005D),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Total Balance",
                                    color = Color(0xFF21005D).copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.05.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatRupiah(budgetSettings.balance),
                                    color = Color(0xFF21005D),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    lineHeight = 38.sp,
                                    letterSpacing = (-0.5).sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "From daily quota ${formatRupiah(budgetSettings.dailyLimit)}",
                                    color = Color(0xFF21005D).copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Categories horizontal slider
            item {
                Column {
                    Text(
                        text = "Categories",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val categoriesList = listOf(
                        Triple("Food", Icons.Filled.Restaurant, "40%"),
                        Triple("Transport", Icons.Filled.DirectionsCar, "15%"),
                        Triple("Bills", Icons.Filled.ReceiptLong, "10%"),
                        Triple("Shopping", Icons.Filled.ShoppingBag, "10%")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categoriesList) { item ->
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF161B26))
                                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (item.first) {
                                                    "Food" -> BrandOrange.copy(alpha = 0.2f)
                                                    "Transport" -> BrandBlue.copy(alpha = 0.2f)
                                                    "Bills" -> BrandPurple.copy(alpha = 0.2f)
                                                    else -> BrandGreen.copy(alpha = 0.2f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = item.second,
                                            contentDescription = item.first,
                                            tint = when (item.first) {
                                                "Food" -> BrandOrange
                                                "Transport" -> BrandBlue
                                                "Bills" -> BrandPurple
                                                else -> BrandGreen
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = item.first,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.third,
                                        color = TextGrayMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "See all",
                        color = BrandLime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {}
                    )
                }
            }

            // Transactions list
            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Empty",
                            tint = TextGrayMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recorded expenses yet. Click the + button below to log one!",
                            textAlign = TextAlign.Center,
                            color = TextGrayMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(transactions) { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeleteConfirm = tx }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF161B26))
                                .border(1.dp, Color(0xFF1F2937), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (tx.category) {
                                    "Food" -> Icons.Filled.Restaurant
                                    "Transport" -> Icons.Filled.DirectionsCar
                                    "Bills" -> Icons.Filled.ReceiptLong
                                    "Shopping" -> Icons.Filled.ShoppingBag
                                    "Entertainment" -> Icons.Filled.Celebration
                                    else -> Icons.Filled.MoreHoriz
                                },
                                contentDescription = tx.category,
                                tint = when (tx.category) {
                                    "Food" -> BrandOrange
                                    "Transport" -> BrandBlue
                                    "Bills" -> BrandPurple
                                    "Shopping" -> BrandGreen
                                    "Entertainment" -> BrandLime
                                    else -> TextGrayMuted
                                }
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.merchant,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${formatDateTime(tx.dateTime, "EEEE, HH:mm")} • ${tx.category}",
                                color = TextGrayMuted,
                                fontSize = 11.sp
                            )
                        }

                        Text(
                            text = "-${formatRupiah(tx.amount)}",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(color = Color(0xFF1F2937).copy(alpha = 0.5f))
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Confirmation delete transaction Dialog
    showDeleteConfirm?.let { tx ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Logged Expense?") },
            text = { Text("Are you sure you want to delete this expense data (${tx.merchant} - ${formatRupiah(tx.amount)})?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(tx)
                    showDeleteConfirm = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF161B26),
            titleContentColor = Color.White,
            textContentColor = TextGrayMuted
        )
    }
}

@Composable
fun SpendingAnalyticsScreen(viewModel: FinanceViewModel) {
    val transactions by viewModel.transactions.collectAsState()

    // Aggregate totals
    val totalExpense = transactions.sumOf { it.amount }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sticky Top bar for Analytics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Spending Analytics",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFF16202E))
                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(100.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Cal",
                        tint = BrandLime,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "This Month",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // High-End Area Spending Card
            item {
                Surface(
                    color = Color(0xFF161B26),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Spending",
                            color = TextGrayMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.05.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatRupiah(totalExpense),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 36.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Custom drawing Canvas area chart with peaks
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height

                                // Draw vertical grid lines asynchronously
                                val cols = 7
                                for (i in 0 until cols) {
                                    val x = (width / (cols - 1)) * i
                                    drawLine(
                                        color = Color(0xFF1F2937).copy(alpha = 0.4f),
                                        start = Offset(x, 0f),
                                        end = Offset(x, height),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                }

                                // Create line coordinates
                                val path = Path().apply {
                                    moveTo(0f, height * 0.9f)
                                    quadraticTo(width * 0.2f, height * 0.82f, width * 0.35f, height * 0.86f)
                                    quadraticTo(width * 0.55f, height * 0.68f, width * 0.65f, height * 0.6f)
                                    quadraticTo(width * 0.76f, height * 0.4f, width * 0.83f, height * 0.25f)
                                    quadraticTo(width * 0.92f, height * 0.55f, width, height * 0.48f)
                                }

                                val fillPath = Path().apply {
                                    addPath(path)
                                    lineTo(width, height)
                                    lineTo(0f, height)
                                    close()
                                }

                                // Fill Area with sleek gradient
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(BrandLime.copy(alpha = 0.35f), Color.Transparent),
                                        startY = 0f,
                                        endY = height
                                    )
                                )

                                // Draw Path Outlines
                                drawPath(
                                    path = path,
                                    color = BrandLime,
                                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Peak Target Point Highlight
                                val peakX = width * 0.83f
                                val peakY = height * 0.25f

                                drawCircle(
                                    color = BrandLime,
                                    radius = 4.dp.toPx(),
                                    center = Offset(peakX, peakY)
                                )
                                drawCircle(
                                    color = BrandLime.copy(alpha = 0.2f),
                                    radius = 10.dp.toPx(),
                                    center = Offset(peakX, peakY)
                                )
                            }

                            // Tooltip positioned exactly near peak coordinates
                            Box(
                                modifier = Modifier
                                    .padding(start = 135.dp, top = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BrandLime)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Rp 320.000",
                                    color = Color(0xFF131f00),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day Labels row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            days.forEach { day ->
                                Text(
                                    text = day,
                                    color = if (day == "S") BrandLime else TextGrayMuted,
                                    fontWeight = if (day == "S") FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Budget vs Spent Limit status
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Budget Status",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "65% Used",
                            color = BrandLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        color = Color(0xFF161B26),
                        border = BorderStroke(1.dp, Color(0xFF1F2937)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { 0.65f },
                                color = BrandLime,
                                trackColor = Color(0xFF2B3544),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(100.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "You have used 65% of your monthly budget. You're still on track!",
                                color = TextGrayMuted,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // Categories Breakdown
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Top Categories",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        color = TextGrayMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Static visual list of Categories
            val list = listOf(
                Triple("Food", 382500.0, 0.45f),
                Triple("Transport", 170000.0, 0.20f),
                Triple("Shopping", 127500.0, 0.15f)
            )

            items(list) { category ->
                Surface(
                    color = Color(0xFF16202E),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (category.first) {
                                        "Food" -> BrandOrange.copy(alpha = 0.2f)
                                        "Transport" -> BrandBlue.copy(alpha = 0.2f)
                                        else -> BrandPurple.copy(alpha = 0.2f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (category.first) {
                                    "Food" -> Icons.Filled.Restaurant
                                    "Transport" -> Icons.Filled.DirectionsCar
                                    else -> Icons.Filled.ShoppingBag
                                },
                                contentDescription = category.first,
                                tint = when (category.first) {
                                    "Food" -> BrandOrange
                                    "Transport" -> BrandBlue
                                    else -> BrandPurple
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.first,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = formatRupiah(category.second),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { category.third },
                                    color = when (category.first) {
                                        "Food" -> BrandOrange
                                        "Transport" -> BrandBlue
                                        else -> BrandPurple
                                    },
                                    trackColor = Color(0xFF2B3544),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(100.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "${(category.third * 100).toInt()}%",
                                    color = TextGrayMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun MyWalletScreen(viewModel: FinanceViewModel) {
    val budgetSettings by viewModel.budgetSettings.collectAsState()
    val context = LocalContext.current

    val balanceText by viewModel.walletBalance.collectAsState()
    val mBudgetText by viewModel.monthlySpendingBudget.collectAsState()
    val dLimitText by viewModel.dailySafeLimit.collectAsState()
    val isAutoCalcOn by viewModel.isAutoCalcOn.collectAsState()

    // Initialize values when screen is loaded
    LaunchedEffect(budgetSettings) {
        viewModel.initWalletForm(budgetSettings)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "My Wallet & Budget",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Balance Card
            item {
                Surface(
                    color = Color(0xFF161B26),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Available Balance",
                                color = TextGrayMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.1f))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Manual Wallet",
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = formatRupiah(budgetSettings.balance),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 40.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.TrendingUp,
                                contentDescription = "Trend",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+2.4% vs last month",
                                color = Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Configurations Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configure Budget Targets",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = TextGrayMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Limits Form fields
            item {
                Surface(
                    color = Color(0xFF161B26),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Monthly Spending Targets input
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "MONTHLY SPENDING BUDGET",
                                    color = TextGrayMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.sp
                                )
                                Icon(
                                    imageVector = Icons.Filled.EditNote,
                                    contentDescription = "Edit",
                                    tint = TextGrayMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            OutlinedTextField(
                                value = mBudgetText,
                                onValueChange = { viewModel.monthlySpendingBudget.value = it },
                                leadingIcon = { Text("Rp", color = TextGrayMuted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = BrandNavyBlack,
                                    unfocusedContainerColor = BrandNavyBlack,
                                    focusedBorderColor = BrandLime,
                                    unfocusedBorderColor = BrandBorderSlate
                                ),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Text(
                                text = "Your total limit for the month",
                                color = TextGrayMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Daily Spending targets input
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "DAILY SAFE LIMIT",
                                color = TextGrayMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.05.sp
                            )
                            OutlinedTextField(
                                value = dLimitText,
                                onValueChange = { viewModel.dailySafeLimit.value = it },
                                leadingIcon = { Text("Rp", color = TextGrayMuted, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = BrandNavyBlack,
                                    unfocusedContainerColor = BrandNavyBlack,
                                    focusedBorderColor = BrandLime,
                                    unfocusedBorderColor = BrandBorderSlate
                                ),
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        HorizontalDivider(color = BrandBorderSlate.copy(alpha = 0.5f))

                        // Switch row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-calculate based on remaining days",
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = isAutoCalcOn,
                                onCheckedChange = { viewModel.isAutoCalcOn.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BrandNavyBlack,
                                    checkedTrackColor = BrandLime,
                                    uncheckedThumbColor = TextGrayMuted,
                                    uncheckedTrackColor = Color(0xFF2B3544)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Save Wallet button
                        Button(
                            onClick = {
                                viewModel.saveWalletSettingsChanges {
                                    Toast.makeText(context, "Wallet targets updated successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandLime,
                                contentColor = BrandNavyBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Save Targets",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Privacy Notice Info Banner
            item {
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Shield",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "100% Offline & Private",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This app does not connect to any banking APIs. Your financial data is stored securely only on this local device.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun MyProfileScreen(
    viewModel: FinanceViewModel,
    onNavigateToSubscreen: (String) -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    // Setup level / progress metrics
    val progressPercent = 82f // Frugal master XP Progress matches 2450 out of 3000

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar Profile
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "My Profile",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { onNavigateToSubscreen("edit_account") }) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit Profile",
                    tint = Color.White
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar Progress Card Block
            item {
                Surface(
                    color = Color(0xFF161B26),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User Avatar Profile Picture Layout with camera icon overlay
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, BrandLime, CircleShape)
                                    .background(Color(0xFF212B39)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Hotlink image matching HTML
                                AsyncImage(
                                    model = userProfile.avatarUrl,
                                    contentDescription = "Profile Pic",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(BrandLime)
                                    .border(4.dp, Color(0xFF161B26), CircleShape)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCamera,
                                    contentDescription = "Change photo",
                                    tint = BrandNavyBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = userProfile.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit name",
                                tint = TextGrayMuted,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onNavigateToSubscreen("edit_account") }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Level badge & XP ProgressBar
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFF2B3544).copy(alpha = 0.5f))
                                .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(100.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Level ${userProfile.level}: Frugal Master",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.05.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress Indicator
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "XP PROGRESS",
                                    color = TextGrayMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.sp
                                )
                                Text(
                                    text = "${userProfile.xpPoints} / 3.000",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progressPercent / 100f },
                                color = BrandLime,
                                trackColor = Color(0xFF2B3544),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(100.dp))
                            )
                        }
                    }
                }
            }

            // Menu Profile Configurations Options Block
            item {
                Surface(
                    color = Color(0xFF161B26),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        ProfileMenuEntry(
                            icon = Icons.Outlined.Person,
                            label = "Edit Account Details",
                            onClick = { onNavigateToSubscreen("edit_account") }
                        )
                        HorizontalDivider(color = Color(0xFF1F2937).copy(alpha = 0.5f))
                        ProfileMenuEntry(
                            icon = Icons.Outlined.QueryStats,
                            label = "Financial Level Progress",
                            onClick = { onNavigateToSubscreen("level_progress") }
                        )
                        HorizontalDivider(color = Color(0xFF1F2937).copy(alpha = 0.5f))
                        ProfileMenuEntry(
                            icon = Icons.Outlined.Security,
                            label = "Security & Password",
                            onClick = { onNavigateToSubscreen("security") }
                        )
                    }
                }
            }

            // Logout row
            item {
                Surface(
                    color = Color(0xFF161B26),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.logoutUser() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Logout,
                                    contentDescription = "Logout",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Log Out",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Arrow",
                            tint = TextGrayMuted
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ProfileMenuEntry(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2B3544).copy(alpha = 0.5f))
                    .border(1.dp, Color(0xFF1F2937).copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = "Arrow",
            tint = TextGrayMuted
        )
    }
}

@Composable
fun EditAccountScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val name by viewModel.profileName.collectAsState()
    val email by viewModel.profileEmail.collectAsState()
    val phone by viewModel.profilePhone.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(userProfile) {
        viewModel.initEditProfileForm(userProfile)
    }

    Scaffold(
        containerColor = BrandNavyBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit Account Details",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile image
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color(0xFF1F2937), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF212B39))
                    ) {
                        AsyncImage(
                            model = userProfile.avatarUrl,
                            contentDescription = "User Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(BrandLime)
                        .border(4.dp, BrandNavyBlack, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = "Change photo",
                        tint = BrandNavyBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "CHANGE PROFILE PICTURE",
                color = TextGrayMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.05.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Form inputs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Full Name",
                        color = TextGrayMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.profileName.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = BrandSurfaceNavy,
                            unfocusedContainerColor = BrandSurfaceNavy,
                            focusedBorderColor = BrandLime,
                            unfocusedBorderColor = BrandBorderSlate
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Email Input with validated badge
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Email Address",
                        color = TextGrayMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.profileEmail.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "Verified email",
                                tint = BrandLimeLight
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = BrandSurfaceNavy,
                            unfocusedContainerColor = BrandSurfaceNavy,
                            focusedBorderColor = BrandLime,
                            unfocusedBorderColor = BrandBorderSlate
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Phone Input (Indonesia standard +62)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Phone Number",
                        color = TextGrayMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.05.sp
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { viewModel.profilePhone.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = BrandSurfaceNavy,
                            unfocusedContainerColor = BrandSurfaceNavy,
                            focusedBorderColor = BrandLime,
                            unfocusedBorderColor = BrandBorderSlate
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }

                // Security notice tips box
                Surface(
                    color = Color(0xFF121C2A),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Tips",
                            tint = TextGrayMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Your verified email is used for important account security notifications and recovery.",
                            color = TextGrayMuted,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Save profiles button
                Button(
                    onClick = {
                        viewModel.saveProfileChanges {
                            Toast.makeText(context, "Account details saved successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandLime,
                        contentColor = BrandNavyBlack
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LevelProgressScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()

    // 75% display XP Progress matches values
    val currentLevelProgressXp = 750
    val targetLevelXp = 1000
    val progressRatePct = 75f

    Scaffold(
        containerColor = BrandNavyBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Financial Level",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0xFF1F2937), CircleShape)
                    ) {
                        AsyncImage(
                            model = userProfile.avatarUrl,
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Unlocked Level Box block
            item {
                Surface(
                    color = Color(0xFF161B26),
                    border = BorderStroke(1.dp, Color(0xFF1F2937)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT STATUS",
                                    color = BrandLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.05.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Level ${userProfile.level}",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Frugal Master",
                                    color = TextGrayMuted,
                                    fontSize = 14.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF2B3544))
                                    .border(1.dp, Color(0xFF44483B), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WorkspacePremium,
                                    contentDescription = "Premium Badge Icon",
                                    tint = BrandLime,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // XP track percentage
                        Column(modifier = Modifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = { progressRatePct / 100f },
                                color = BrandLime,
                                trackColor = Color(0xFF1F2937),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(100.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$currentLevelProgressXp / $targetLevelXp XP to Level 5",
                                    color = TextGrayMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${progressRatePct.toInt()}%",
                                    color = BrandLime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Earned Badges section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Earned Badges",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "View All",
                            color = BrandLime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2x2 grid design for badges
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BadgeCardItem(
                                title = "Budget Streak",
                                icon = Icons.Filled.Bolt,
                                isUnlocked = true,
                                modifier = Modifier.weight(1f)
                            )
                            BadgeCardItem(
                                title = "Savings Guru",
                                icon = Icons.Filled.Savings,
                                isUnlocked = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            BadgeCardItem(
                                title = "Anti-Impulsive",
                                icon = Icons.Filled.DoNotDisturbOn,
                                isUnlocked = false,
                                modifier = Modifier.weight(1f)
                            )
                            BadgeCardItem(
                                title = "Master Planner",
                                icon = Icons.Filled.CalendarToday,
                                isUnlocked = false,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Benefits lists
            item {
                Column {
                    Text(
                        text = "Level Benefits",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Surface(
                        color = Color(0xFF161B26),
                        border = BorderStroke(1.dp, Color(0xFF1F2937)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            BenefitEntryRow(
                                title = "Advanced Analytics Reports",
                                description = "Deep dive into spending habits"
                            )
                            HorizontalDivider(color = Color(0xFF1F2937))
                            BenefitEntryRow(
                                title = "Custom Category Icons",
                                description = "Personalize your ledger view"
                            )
                            HorizontalDivider(color = Color(0xFF1F2937))
                            BenefitEntryRow(
                                title = "Priority Support",
                                description = "24/7 dedicated finance desk"
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

@Composable
fun BadgeCardItem(
    title: String,
    icon: ImageVector,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF161B26),
        border = BorderStroke(1.dp, Color(0xFF1F2937)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.alpha(if (isUnlocked) 1f else 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) BrandLime.copy(alpha = 0.1f) else Color(0xFF2B3544))
                    .border(
                        1.dp,
                        if (isUnlocked) BrandLime.copy(alpha = 0.3f) else Color(0xFF44483B),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isUnlocked) BrandLime else TextGrayMuted
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = if (isUnlocked) Color.White else TextGrayMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BenefitEntryRow(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Check",
            tint = BrandLime,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextGrayMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun SecurityScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current

    val biometricOn by viewModel.isBiometricEnabled.collectAsState()
    val pinLockOn by viewModel.isPinLockEnabled.collectAsState()

    var currentPasswordVal by remember { mutableStateOf("") }
    var newPasswordVal by remember { mutableStateOf("") }
    var confirmPasswordVal by remember { mutableStateOf("") }

    var isCurrentMasked by remember { mutableStateOf(true) }
    var isNewMasked by remember { mutableStateOf(true) }
    var isConfirmMasked by remember { mutableStateOf(true) }

    LaunchedEffect(userProfile) {
        viewModel.initSecurityForm(userProfile)
    }

    Scaffold(
        containerColor = BrandNavyBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security & Password",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Change Password",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Password Edit Container
            Surface(
                color = Color(0xFF161B26),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Current Password
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "My Wallet & Budget",
                            color = TextGrayMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = currentPasswordVal,
                                onValueChange = { currentPasswordVal = it },
                                placeholder = { Text("••••••••••••", color = TextGrayMuted.copy(alpha = 0.3f)) },
                                visualTransformation = if (isCurrentMasked) PasswordVisualTransformation() else VisualTransformation.None,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { isCurrentMasked = !isCurrentMasked }) {
                                        Icon(
                                            imageVector = if (isCurrentMasked) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Visibility",
                                            tint = TextGrayMuted
                                        )
                                    }
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1F2937))

                    // New Password
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "New Password",
                            color = TextGrayMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newPasswordVal,
                                onValueChange = { newPasswordVal = it },
                                placeholder = { Text("Min. 8 characters", color = TextGrayMuted.copy(alpha = 0.3f)) },
                                visualTransformation = if (isNewMasked) PasswordVisualTransformation() else VisualTransformation.None,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { isNewMasked = !isNewMasked }) {
                                        Icon(
                                            imageVector = if (isNewMasked) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Visibility",
                                            tint = TextGrayMuted
                                        )
                                    }
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF1F2937))

                    // Confirm New Password
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Confirm New Password",
                            color = TextGrayMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = confirmPasswordVal,
                                onValueChange = { confirmPasswordVal = it },
                                placeholder = { Text("Repeat new password", color = TextGrayMuted.copy(alpha = 0.3f)) },
                                visualTransformation = if (isConfirmMasked) PasswordVisualTransformation() else VisualTransformation.None,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { isConfirmMasked = !isConfirmMasked }) {
                                        Icon(
                                            imageVector = if (isConfirmMasked) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = "Visibility",
                                            tint = TextGrayMuted
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Text(
                text = "Advanced Security",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            // Switch block
            Surface(
                color = Color(0xFF161B26),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Biometrics switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2B3544))
                                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Fingerprint,
                                    contentDescription = "Finger",
                                    tint = BrandLime
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "Biometric Log In (Fingerprint/FaceID)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = biometricOn,
                            onCheckedChange = { viewModel.updateSecuritySwitches(it, pinLockOn) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandNavyBlack,
                                checkedTrackColor = BrandLime,
                                uncheckedThumbColor = TextGrayMuted,
                                uncheckedTrackColor = Color(0xFF1F2937)
                            )
                        )
                    }

                    HorizontalDivider(color = Color(0xFF1F2937))

                    // PIN lock switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2B3544))
                                    .border(1.dp, Color(0xFF1F2937), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = "Shield Lock",
                                    tint = TextGrayMuted
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "App Launch PIN Lock",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = pinLockOn,
                            onCheckedChange = { viewModel.updateSecuritySwitches(biometricOn, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BrandNavyBlack,
                                checkedTrackColor = BrandLime,
                                uncheckedThumbColor = TextGrayMuted,
                                uncheckedTrackColor = Color(0xFF1F2937)
                            )
                        )
                    }
                }
            }

            // Update Security button
            Button(
                onClick = {
                    Toast.makeText(context, "Security configurations saved successfully!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandLime,
                    contentColor = BrandNavyBlack
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Update Security",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LogExpenseDialog(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val amount by viewModel.expenseAmount.collectAsState()
    val note by viewModel.expenseNotes.collectAsState()
    val timestamp by viewModel.expenseDateTime.collectAsState()
    val category by viewModel.selectedCategory.collectAsState()

    val context = LocalContext.current

    // Set up standard list of items categories
    val categoriesList = listOf("Food", "Transport", "Bills", "Shopping", "Entertainment", "Others")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BrandNavyBlack.copy(alpha = 0.82f))
                .clickable { onDismiss() } // Tap outside dismisses
        ) {
            // Sliding Sheet style container at bottom
            Surface(
                color = Color(0xFF161B26),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = false) {} // prevent click consumption
            ) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Notch indicator
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFF1F2937))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Log Expense",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Large dynamic Amount field Rp 0 with clean underline
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Rp",
                                color = TextGrayMuted.copy(alpha = 0.5f),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { input ->
                                    val safeNum = input.replace("[^0-9]".toRegex(), "")
                                    viewModel.expenseAmount.value = if (safeNum.isEmpty()) "0" else safeNum
                                },
                                textStyle = MaterialTheme.typography.displayMedium.copy(
                                    color = BrandLime,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.Center
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    errorBorderColor = Color.Transparent
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .width(220.dp)
                                    .testTag("expense_amount_input")
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(240.dp)
                                .height(2.dp)
                                .background(BrandLime)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Category horizontal row pills selector
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SELECT CATEGORY",
                            color = TextGrayMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.05.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categoriesList) { item ->
                                val isSelected = item == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) BrandLime else Color(0xFF1F2937))
                                        .clickable { viewModel.selectedCategory.value = item }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (item) {
                                                "Food" -> Icons.Filled.Restaurant
                                                "Transport" -> Icons.Filled.DirectionsCar
                                                "Bills" -> Icons.Filled.ReceiptLong
                                                "Shopping" -> Icons.Filled.ShoppingBag
                                                "Entertainment" -> Icons.Filled.Celebration
                                                else -> Icons.Filled.MoreHoriz
                                            },
                                            contentDescription = item,
                                            tint = if (isSelected) BrandNavyBlack else Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item,
                                            color = if (isSelected) BrandNavyBlack else Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (isSelected) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = BrandNavyBlack,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Fields form: note input & Dates picker
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Notes textfield
                        OutlinedTextField(
                            value = note,
                            onValueChange = { viewModel.expenseNotes.value = it },
                            placeholder = { Text("Notes/Description", color = TextGrayMuted.copy(alpha = 0.5f)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.EditNote,
                                    contentDescription = "Notes",
                                    tint = TextGrayMuted
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color(0xFF0C1726),
                                unfocusedContainerColor = Color(0xFF0C1726),
                                focusedBorderColor = BrandLime,
                                unfocusedBorderColor = BrandBorderSlate
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_note_input")
                        )

                        // Dates dialog toggle row button layout
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = timestamp
                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = Calendar.getInstance()
                                selectedCal.set(year, month, dayOfMonth)
                                viewModel.expenseDateTime.value = selectedCal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0C1726))
                                .border(1.dp, BrandBorderSlate, RoundedCornerShape(14.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = "Today",
                                    tint = TextGrayMuted
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = formatDateTime(timestamp, "EEEE, HH:mm"),
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Open datepicker",
                                tint = TextGrayMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Primary ACTION Save transaction button
                    Button(
                        onClick = {
                            viewModel.saveExpense(
                                onSuccess = {
                                    Toast.makeText(context, "Transaction recorded successfully!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandLime,
                            contentColor = BrandNavyBlack
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("save_expense_button")
                    ) {
                        Text(
                            text = "Save Transaction",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
