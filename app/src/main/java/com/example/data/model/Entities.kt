package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val merchant: String,
    val category: String, // "Food", "Transport", "Bills", "Shopping", "Entertainment", "Others"
    val amount: Double,
    val dateTime: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "budget_settings")
data class BudgetSettings(
    @PrimaryKey val id: Int = 1,
    val balance: Double = 2450000.0,
    val monthlyBudget: Double = 3000000.0,
    val dailyLimit: Double = 1000000.0,
    val isAutoCalculateActive: Boolean = false
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Mochamad Adit",
    val email: String = "adit.dev@example.com",
    val phone: String = "+62 812-xxxx-xxxx",
    val avatarUrl: String = "https://lh3.googleusercontent.com/aida-public/AB6AXuC5KvBHuczzqEXjpDuVZAuItE9tzfrHsipbAbMypHRY_fPBPah0gLtt9-EJ8rwP87s92ScNA7OAd0VIzDZEwfXF2z_DzVWcuwW6shaGwJXs0ytI434BJaRlqc46gsvBaJtKE09y3eKNEYskDkL2lG_i2QcI6obkglZZVvs9olOnuZVhp2LlGm0VjUIezJeW2lTSwfTv7pC16irMtOEaBgEaucBFh9uPil4GBJfFh05eJhgPEmJk61vdxJLB_uFqZ5NF7xGYxw7HgX-l",
    val xpPoints: Int = 2450,
    val level: Int = 4,
    val isBiometricEnabled: Boolean = true,
    val isPinLockEnabled: Boolean = false,
    val isLoggedIn: Boolean = false // user is not logged in initially to allow demoing the login screen!
)
