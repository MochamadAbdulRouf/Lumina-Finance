package com.example.data.repository

import com.example.data.db.FinanceDao
import com.example.data.model.BudgetSettings
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class FinanceRepository(private val financeDao: FinanceDao) {

    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val budgetSettings: Flow<BudgetSettings?> = financeDao.getBudgetSettingsFlow()
    val userProfile: Flow<UserProfile?> = financeDao.getUserProfileFlow()

    suspend fun initializeDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingProfile = financeDao.getUserProfileOnce()
        if (existingProfile == null) {
            // Populate user profile
            val defaultProfile = UserProfile(
                id = 1,
                name = "Mochamad Adit",
                email = "adit.dev@example.com",
                phone = "+62 812-9876-5432",
                xpPoints = 2450,
                level = 4,
                isBiometricEnabled = true,
                isPinLockEnabled = false,
                isLoggedIn = false // start at false for login flow demonstration
            )
            financeDao.insertOrUpdateProfile(defaultProfile)
        }

        val existingBudget = financeDao.getBudgetSettingsOnce()
        if (existingBudget == null) {
            // Populate defaults
            val defaultBudget = BudgetSettings(
                id = 1,
                balance = 2450000.0,
                monthlyBudget = 3000000.0,
                dailyLimit = 100000.0,
                isAutoCalculateActive = false
            )
            financeDao.insertOrUpdateBudget(defaultBudget)
        }

        // Check transactions
        val existingTransactions = financeDao.getAllTransactions().firstOrNull()
        if (existingTransactions.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val tx1 = Transaction(
                id = 1,
                merchant = "Kopi Senja Utama",
                category = "Food",
                amount = 25000.0,
                dateTime = now - 4 * 3600 * 1000, // 4 hours ago
                notes = "Culinary"
            )
            val tx2 = Transaction(
                id = 2,
                merchant = "Bluebird Taxi",
                category = "Transport",
                amount = 45000.0,
                dateTime = now - 8 * 3600 * 1000, // 8 hours ago
                notes = "Transport"
            )
            val tx3 = Transaction(
                id = 3,
                merchant = "Indomaret",
                category = "Shopping",
                amount = 120000.0,
                dateTime = now - 24 * 3600 * 1000, // 24 hours ago
                notes = "Shopping"
            )
            financeDao.insertTransaction(tx1)
            financeDao.insertTransaction(tx2)
            financeDao.insertTransaction(tx3)
        }
    }

    suspend fun addTransaction(merchant: String, category: String, amount: Double, notes: String, dateTime: Long) = withContext(Dispatchers.IO) {
        val tx = Transaction(
            merchant = merchant,
            category = category,
            amount = amount,
            notes = notes,
            dateTime = dateTime
        )
        financeDao.insertTransaction(tx)

        // Dynamically reduce balance
        val currentBudget = financeDao.getBudgetSettingsOnce() ?: BudgetSettings()
        val updatedBudget = currentBudget.copy(
            balance = currentBudget.balance - amount
        )
        financeDao.insertOrUpdateBudget(updatedBudget)

        // Increment user XP points for their level!
        val currentProfile = financeDao.getUserProfileOnce() ?: UserProfile()
        val bonusXp = 50
        var newXp = currentProfile.xpPoints + bonusXp
        var newLevel = currentProfile.level
        // Simplistic leveling system: Level 4: 2450 XP, let's say every 1000 XP is a level
        // Master progress uses 2,450 / 3,000 progress limit
        if (newXp >= 3000 && currentProfile.level == 4) {
            newLevel = 5
        }
        val updatedProfile = currentProfile.copy(
            xpPoints = newXp,
            level = newLevel
        )
        financeDao.insertOrUpdateProfile(updatedProfile)
    }

    suspend fun deleteTransaction(tx: Transaction) = withContext(Dispatchers.IO) {
        financeDao.deleteTransaction(tx.id)

        // Re-add balance
        val currentBudget = financeDao.getBudgetSettingsOnce() ?: BudgetSettings()
        val updatedBudget = currentBudget.copy(
            balance = currentBudget.balance + tx.amount
        )
        financeDao.insertOrUpdateBudget(updatedBudget)
    }

    suspend fun saveBudgetSettings(balance: Double, monthlyBudget: Double, dailyLimit: Double, autoCalculate: Boolean) = withContext(Dispatchers.IO) {
        val settings = BudgetSettings(
            balance = balance,
            monthlyBudget = monthlyBudget,
            dailyLimit = dailyLimit,
            isAutoCalculateActive = autoCalculate
        )
        financeDao.insertOrUpdateBudget(settings)
    }

    suspend fun updateProfile(name: String, email: String, phone: String) = withContext(Dispatchers.IO) {
        val currentProfile = financeDao.getUserProfileOnce() ?: UserProfile()
        val updatedProfile = currentProfile.copy(
            name = name,
            email = email,
            phone = phone
        )
        financeDao.insertOrUpdateProfile(updatedProfile)
    }

    suspend fun updateSecuritySettings(biometric: Boolean, pinLock: Boolean) = withContext(Dispatchers.IO) {
        val currentProfile = financeDao.getUserProfileOnce() ?: UserProfile()
        val updatedProfile = currentProfile.copy(
            isBiometricEnabled = biometric,
            isPinLockEnabled = pinLock
        )
        financeDao.insertOrUpdateProfile(updatedProfile)
    }

    suspend fun setLoginState(isLoggedIn: Boolean) = withContext(Dispatchers.IO) {
        val currentProfile = financeDao.getUserProfileOnce() ?: UserProfile()
        val updatedProfile = currentProfile.copy(
            isLoggedIn = isLoggedIn
        )
        financeDao.insertOrUpdateProfile(updatedProfile)
    }
}
