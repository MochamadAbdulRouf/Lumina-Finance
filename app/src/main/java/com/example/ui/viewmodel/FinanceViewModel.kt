package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.BudgetSettings
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AnalyticsDay(
    val dateLabel: String,
    val initialLabel: String,
    val totalSpend: Double,
    val dateInMillis: Long,
    val xPercent: Float,
    val yPercent: Float
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao())
        
        // Populate static data
        viewModelScope.launch {
            try {
                repository.initializeDatabaseIfEmpty()
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to initialize database: ${e.message}", e)
            }
        }
    }

    // Exposed flows
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetSettings: StateFlow<BudgetSettings> = repository.budgetSettings
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetSettings())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    val categorySummaries: StateFlow<Map<String, Double>> = transactions
        .map { txList ->
            txList.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalExpense: StateFlow<Double> = transactions
        .map { txList -> txList.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailyQuota: StateFlow<Double> = budgetSettings
        .map { it.dailyLimit.let { limit -> if (limit <= 0.0) 50000.0 else limit } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50000.0)

    private fun isToday(timestamp: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        cal1.timeInMillis = timestamp
        val cal2 = java.util.Calendar.getInstance()
        cal2.timeInMillis = System.currentTimeMillis()
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }

    val todayCategoryExpenses: StateFlow<Map<String, Double>> = transactions
        .map { txList ->
            txList.filter { isToday(it.dateTime) }
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val todayCategoryPercentages: StateFlow<Map<String, Int>> = combine(todayCategoryExpenses, dailyQuota) { expenses, quota ->
        val result = mutableMapOf<String, Int>()
        val categories = listOf("Food", "Transport", "Bills", "Shopping", "Entertainment", "Others")
        categories.forEach { cat ->
            val amt = expenses[cat] ?: 0.0
            val pct = if (quota > 0.0) ((amt / quota) * 100.0).toInt() else 0
            result[cat] = pct
        }
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val selectedTimeFilter = MutableStateFlow("This Month")

    private val _userSelectedDay = MutableStateFlow("")

    val weeklyAnalyticsData: StateFlow<List<AnalyticsDay>> = combine(transactions, selectedTimeFilter) { txList, filter ->
        val result = when (filter) {
            "Today" -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfToday = calendar.timeInMillis
                (0..23).map { hour ->
                    val startOfHour = startOfToday + hour * 3600000L
                    val endOfHour = startOfHour + 3600000L - 1
                    val hourTransactions = txList.filter { it.dateTime in startOfHour..endOfHour }
                    val hourSpend = hourTransactions.sumOf { it.amount }
                    val label = String.format(java.util.Locale.ENGLISH, "%02d:00", hour)
                    AnalyticsDay(
                        dateLabel = label,
                        initialLabel = label,
                        totalSpend = hourSpend,
                        dateInMillis = startOfHour,
                        xPercent = hour / 23.0f,
                        yPercent = 0.0f
                    )
                }
            }
            "This Year" -> {
                val calendar = java.util.Calendar.getInstance()
                calendar.set(java.util.Calendar.MONTH, java.util.Calendar.JANUARY)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfYear = calendar.timeInMillis
                (0..11).map { month ->
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = startOfYear
                    cal.set(java.util.Calendar.MONTH, month)
                    val startOfMonth = cal.timeInMillis

                    val maxDaysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    cal.set(java.util.Calendar.DAY_OF_MONTH, maxDaysInMonth)
                    cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
                    cal.set(java.util.Calendar.MINUTE, 59)
                    cal.set(java.util.Calendar.SECOND, 59)
                    cal.set(java.util.Calendar.MILLISECOND, 999)
                    val endOfMonth = cal.timeInMillis

                    val monthTransactions = txList.filter { it.dateTime in startOfMonth..endOfMonth }
                    val monthSpend = monthTransactions.sumOf { it.amount }

                    val sdfMonthName = java.text.SimpleDateFormat("MMMM", java.util.Locale.ENGLISH)
                    val monthName = sdfMonthName.format(java.util.Date(startOfMonth))
                    val initialLabel = monthName.take(3)

                    AnalyticsDay(
                        dateLabel = monthName,
                        initialLabel = initialLabel,
                        totalSpend = monthSpend,
                        dateInMillis = startOfMonth,
                        xPercent = month / 11.0f,
                        yPercent = 0.0f
                    )
                }
            }
            else -> { // "This Month"
                val calendar = java.util.Calendar.getInstance()
                val maxDays = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfFirstDay = calendar.timeInMillis
                
                (1..maxDays).map { day ->
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = startOfFirstDay
                    cal.set(java.util.Calendar.DAY_OF_MONTH, day)
                    val startOfDay = cal.timeInMillis
                    val endOfDay = startOfDay + (24 * 60 * 60 * 1000L) - 1

                    val dayTransactions = txList.filter { it.dateTime in startOfDay..endOfDay }
                    val daySpend = dayTransactions.sumOf { it.amount }

                    val sdfDay = java.text.SimpleDateFormat("EEEE", java.util.Locale.ENGLISH)
                    val dayName = sdfDay.format(java.util.Date(startOfDay))

                    val label = String.format(java.util.Locale.ENGLISH, "%02d %s", day, dayName.take(3))
                    val dayOfMonthStr = day.toString()

                    AnalyticsDay(
                        dateLabel = label,
                        initialLabel = dayOfMonthStr,
                        totalSpend = daySpend,
                        dateInMillis = startOfDay,
                        xPercent = if (maxDays > 1) (day - 1) / (maxDays - 1).toFloat() else 0f,
                        yPercent = 0.0f
                    )
                }
            }
        }

        val maxSpend = result.maxOfOrNull { it.totalSpend } ?: 0.0
        result.map { day ->
            val yPercent = if (maxSpend > 0.0) {
                (day.totalSpend / maxSpend).toFloat()
            } else {
                0.0f
            }
            day.copy(yPercent = yPercent)
        }
    }
    .flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDay: StateFlow<String> = combine(_userSelectedDay, weeklyAnalyticsData) { userSel, points ->
        if (points.isEmpty()) return@combine ""
        val exists = points.any { it.dateLabel == userSel }
        if (exists) {
            userSel
        } else {
            val now = System.currentTimeMillis()
            val closest = points.minByOrNull { Math.abs(it.dateInMillis - now) }
            closest?.dateLabel ?: points.last().dateLabel
        }
    }
    .flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun selectDay(label: String) {
        _userSelectedDay.value = label
    }

    val selectedDayTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        selectedDay,
        weeklyAnalyticsData
    ) { txList, selLabel, points ->
        val point = points.find { it.dateLabel == selLabel }
        if (point == null) {
            emptyList()
        } else {
            val start = point.dateInMillis
            val filter = selectedTimeFilter.value
            val duration = when (filter) {
                "Today" -> 3600000L // 1 hour
                "This Month" -> 86400000L // 1 day
                "This Year" -> {
                    val cal = java.util.Calendar.getInstance()
                    cal.timeInMillis = start
                    val days = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    days * 86400000L // 1 month
                }
                else -> 86400000L
            }
            val end = start + duration - 1
            txList.filter { it.dateTime in start..end }
        }
    }
    .flowOn(kotlinx.coroutines.Dispatchers.Default)
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Input States for Forms
    // Log Expense Modal Form
    var expenseAmount = MutableStateFlow("0")
    var selectedCategory = MutableStateFlow("Food") // Category Name
    var expenseNotes = MutableStateFlow("")
    var expenseDateTime = MutableStateFlow(System.currentTimeMillis())

    // Edit Profile Form State
    var profileName = MutableStateFlow("")
    var profileEmail = MutableStateFlow("")
    var profilePhone = MutableStateFlow("")

    // Configure Wallet Budget Form State
    var walletBalance = MutableStateFlow("")
    var monthlySpendingBudget = MutableStateFlow("")
    var dailySafeLimit = MutableStateFlow("")
    var isAutoCalcOn = MutableStateFlow(false)

    // Security screen switches / password form state
    var currentPassword = MutableStateFlow("")
    var newPassword = MutableStateFlow("")
    var confirmPassword = MutableStateFlow("")
    var isBiometricEnabled = MutableStateFlow(true)
    var isPinLockEnabled = MutableStateFlow(false)

    // Auth flows
    var authEmail = MutableStateFlow("")
    var authPassword = MutableStateFlow("")
    var isSignUpMode = MutableStateFlow(false)

    // Setup form initializers
    fun initEditProfileForm(profile: UserProfile) {
        profileName.value = profile.name
        profileEmail.value = profile.email
        profilePhone.value = profile.phone
    }

    fun initWalletForm(budget: BudgetSettings) {
        walletBalance.value = budget.balance.toInt().toString()
        monthlySpendingBudget.value = budget.balance.toInt().toString()
        dailySafeLimit.value = budget.dailyLimit.toInt().toString()
        isAutoCalcOn.value = budget.isAutoCalculateActive
    }

    fun initSecurityForm(profile: UserProfile) {
        isBiometricEnabled.value = profile.isBiometricEnabled
        isPinLockEnabled.value = profile.isPinLockEnabled
    }

    // User Operations
    fun loginUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val emailValue = authEmail.value.trim()
        val pwdValue = authPassword.value.trim()
        if (emailValue.isEmpty()) {
            onError("Email address is required.")
            return
        }
        if (pwdValue.length < 6) {
            onError("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            try {
                // Update profile record
                val current = repository.userProfile.firstOrNull() ?: UserProfile()
                val finalProfile = current.copy(
                    email = emailValue,
                    isLoggedIn = true,
                    name = if (current.name == "Mochamad Adit" || current.name.isEmpty()) {
                        emailValue.substringBefore("@").replaceFirstChar { it.uppercase() }
                    } else current.name
                )
                repository.updateProfile(finalProfile.name, finalProfile.email, finalProfile.phone)
                repository.setLoginState(true)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to login user: ${e.message}", e)
                onError(e.localizedMessage ?: "An error occurred during log in.")
            }
        }
    }

    fun registerUser(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val emailValue = authEmail.value.trim()
        val pwdValue = authPassword.value.trim()
        if (emailValue.isEmpty()) {
            onError("Email address is required.")
            return
        }
        if (pwdValue.length < 6) {
            onError("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            try {
                // Register and auto logs-in
                val current = repository.userProfile.firstOrNull() ?: UserProfile()
                val finalProfile = current.copy(
                    email = emailValue,
                    isLoggedIn = true,
                    name = emailValue.substringBefore("@").replaceFirstChar { it.uppercase() }
                )
                repository.updateProfile(finalProfile.name, finalProfile.email, finalProfile.phone)
                repository.setLoginState(true)
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to register user: ${e.message}", e)
                onError(e.localizedMessage ?: "An error occurred during registration.")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                repository.setLoginState(false)
                // Reset input stats
                authEmail.value = ""
                authPassword.value = ""
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to logout: ${e.message}", e)
            }
        }
    }

    fun saveProfileChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.updateProfile(
                    name = profileName.value.trim(),
                    email = profileEmail.value.trim(),
                    phone = profilePhone.value.trim()
                )
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to save profile changes: ${e.message}", e)
            }
        }
    }

    fun saveExpense(onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Parse clean amount
        val cleanedAmountStr = expenseAmount.value.replace("[^0-9]".toRegex(), "")
        val amt = cleanedAmountStr.toDoubleOrNull() ?: 0.0
        if (amt <= 0.0) {
            onError("Please enter an expense amount greater than 0.")
            return
        }

        val notesStr = expenseNotes.value.trim().ifEmpty { selectedCategory.value }

        viewModelScope.launch {
            try {
                repository.addTransaction(
                    merchant = if (notesStr.isNotEmpty()) notesStr else "Expense Item",
                    category = selectedCategory.value,
                    amount = amt,
                    notes = selectedCategory.value,
                    dateTime = expenseDateTime.value
                )
                // Reset inputs
                expenseAmount.value = "0"
                expenseNotes.value = ""
                expenseDateTime.value = System.currentTimeMillis()
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to save expense: ${e.message}", e)
                onError(e.localizedMessage ?: "Failed to save expense.")
            }
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(tx)
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to delete transaction: ${e.message}", e)
            }
        }
    }

    fun saveWalletSettingsChanges(onSuccess: () -> Unit) {
        val cleanBalance = walletBalance.value.replace("[^0-9]".toRegex(), "")
        val balanceVal = cleanBalance.toDoubleOrNull() ?: 0.0
        
        val cleanLimit = dailySafeLimit.value.replace("[^0-9]".toRegex(), "")
        val dLimitVal = cleanLimit.toDoubleOrNull() ?: 0.0
        
        val isAutoCalcValue = isAutoCalcOn.value

        viewModelScope.launch {
            try {
                repository.saveBudgetSettings(
                    balance = balanceVal,
                    monthlyBudget = balanceVal,
                    dailyLimit = dLimitVal,
                    autoCalculate = isAutoCalcValue
                )
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to save wallet settings: ${e.message}", e)
            }
        }
    }

    fun updateSecuritySwitches(biometric: Boolean, pinLock: Boolean) {
        isBiometricEnabled.value = biometric
        isPinLockEnabled.value = pinLock
        viewModelScope.launch {
            try {
                repository.updateSecuritySettings(biometric, pinLock)
            } catch (e: Exception) {
                android.util.Log.e("CRASH_DUMP", "Failed to update security switches: ${e.message}", e)
            }
        }
    }
}
