package com.finny.pet.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `inventory` (`itemId` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `quantity` INTEGER NOT NULL, PRIMARY KEY(`itemId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `skin_ownership` (`skinId` TEXT NOT NULL, `title` TEXT NOT NULL, `owned` INTEGER NOT NULL, `equipped` INTEGER NOT NULL, PRIMARY KEY(`skinId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `pet_needs` (`profileId` INTEGER NOT NULL, `food` INTEGER NOT NULL, `water` INTEGER NOT NULL, `health` INTEGER NOT NULL, `mood` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`profileId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `task_progress_v2` (`taskId` TEXT NOT NULL, `attempts` INTEGER NOT NULL, `bestStars` INTEGER NOT NULL, `completed` INTEGER NOT NULL, `lastScore` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`taskId`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS `wallet_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` INTEGER NOT NULL, `reason` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `pet_levels` (`profileId` INTEGER NOT NULL, `level` INTEGER NOT NULL, `experience` INTEGER NOT NULL, PRIMARY KEY(`profileId`))")
        val now = System.currentTimeMillis()
        db.execSQL("INSERT OR IGNORE INTO pet_needs(profileId,food,water,health,mood,updatedAt) VALUES(1,80,80,100,90,$now)")
        db.execSQL("INSERT OR IGNORE INTO pet_levels(profileId,level,experience) VALUES(1,1,0)")
    }
}
