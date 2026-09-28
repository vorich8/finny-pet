package com.finny.pet.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(@PrimaryKey val id: Long = 1, val petName: String, val currentPeriod: Int = 1, val createdAt: Long = System.currentTimeMillis())

@Entity(tableName = "pets")
data class PetEntity(@PrimaryKey val profileId: Long = 1, val variantId: String, val stage: Int = 1, val emotion: String = "JOY")

@Entity(tableName = "balances")
data class BalanceEntity(@PrimaryKey val profileId: Long = 1, val coins: Int = 0, val stars: Int = 0)

@Entity(tableName = "periods")
data class PeriodEntity(@PrimaryKey val number: Int, val unlocked: Boolean, val completed: Boolean = false)

@Entity(tableName = "goals")
data class GoalEntity(@PrimaryKey val id: String, val title: String, val cost: Int, val saved: Int = 0, val active: Boolean = false, val completed: Boolean = false)

@Entity(tableName = "purchases")
data class PurchaseEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val itemId: String, val title: String, val price: Int, val required: Boolean, val period: Int)

@Entity(tableName = "lessons")
data class LessonEntity(@PrimaryKey val id: String, val topic: String, val title: String, val body: String, val completed: Boolean = false)

@Entity(tableName = "progress")
data class ProgressEntity(@PrimaryKey val key: String, val value: Int = 0, val updatedAt: Long = System.currentTimeMillis())

@Entity(tableName = "inventory")
data class InventoryEntity(@PrimaryKey val itemId: String, val title: String, val category: String, val quantity: Int = 0)

@Entity(tableName = "skin_ownership")
data class SkinOwnershipEntity(@PrimaryKey val skinId: String, val title: String, val owned: Boolean = false, val equipped: Boolean = false)

@Entity(tableName = "pet_needs")
data class PetNeedsEntity(
    @PrimaryKey val profileId: Long = 1,
    val food: Int = 80,
    val water: Int = 80,
    val health: Int = 100,
    val mood: Int = 90,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "task_progress_v2")
data class TaskProgressV2Entity(@PrimaryKey val taskId: String, val attempts: Int = 0, val bestStars: Int = 0, val completed: Boolean = false, val lastScore: Int = 0, val updatedAt: Long = System.currentTimeMillis())

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val amount: Int, val reason: String, val createdAt: Long = System.currentTimeMillis())

@Entity(tableName = "pet_levels")
data class PetLevelEntity(@PrimaryKey val profileId: Long = 1, val level: Int = 1, val experience: Int = 0)
