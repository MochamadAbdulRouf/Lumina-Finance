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

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao())
        
        // Populate static data
        viewModelScope.launch {
            repository.initializeDatabaseIfEmpty()
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
        monthlySpendingBudget.value = budget.monthlyBudget.toInt().toString()
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
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            repository.setLoginState(false)
            // Reset input stats
            authEmail.value = ""
            authPassword.value = ""
        }
    }

    fun saveProfileChanges(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProfile(
                name = profileName.value.trim(),
                email = profileEmail.value.trim(),
                phone = profilePhone.value.trim()
            )
            onSuccess()
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
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun saveWalletSettingsChanges(onSuccess: () -> Unit) {
        val balanceVal = walletBalance.value.toDoubleOrNull() ?: 0.0
        val mBudgetVal = monthlySpendingBudget.value.toDoubleOrNull() ?: 0.0
        val dLimitVal = dailySafeLimit.value.toDoubleOrNull() ?: 0.0
        val isAutoCalcValue = isAutoCalcOn.value

        viewModelScope.launch {
            repository.saveBudgetSettings(
                balance = balanceVal,
                monthlyBudget = mBudgetVal,
                dailyLimit = dLimitVal,
                autoCalculate = isAutoCalcValue
            )
            onSuccess()
        }
    }

    fun updateSecuritySwitches(biometric: Boolean, pinLock: Boolean) {
        isBiometricEnabled.value = biometric
        isPinLockEnabled.value = pinLock
        viewModelScope.launch {
            repository.updateSecuritySettings(biometric, pinLock)
        }
    }
}
