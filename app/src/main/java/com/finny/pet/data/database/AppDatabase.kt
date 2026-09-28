package com.finny.pet.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProfileEntity::class, PetEntity::class, BalanceEntity::class, PeriodEntity::class, GoalEntity::class, PurchaseEntity::class, LessonEntity::class, ProgressEntity::class, InventoryEntity::class, SkinOwnershipEntity::class, PetNeedsEntity::class, TaskProgressV2Entity::class, WalletTransactionEntity::class, PetLevelEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun petDao(): PetDao
    abstract fun balanceDao(): BalanceDao
    abstract fun periodDao(): PeriodDao
    abstract fun goalDao(): GoalDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun lessonDao(): LessonDao
    abstract fun progressDao(): ProgressDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun skinDao(): SkinDao
    abstract fun petNeedsDao(): PetNeedsDao
    abstract fun taskProgressV2Dao(): TaskProgressV2Dao
    abstract fun walletTransactionDao(): WalletTransactionDao
    abstract fun petLevelDao(): PetLevelDao
}
