package com.example.data.db

import androidx.room.*
import com.example.data.model.BudgetSettings
import com.example.data.model.Transaction
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    // Transactions API
    @Query("SELECT * FROM transactions ORDER BY dateTime DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)

    // Budget Settings API
    @Query("SELECT * FROM budget_settings WHERE id = 1 LIMIT 1")
    fun getBudgetSettingsFlow(): Flow<BudgetSettings?>

    @Query("SELECT * FROM budget_settings WHERE id = 1 LIMIT 1")
    suspend fun getBudgetSettingsOnce(): BudgetSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: BudgetSettings)

    // User Profile API
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}
