package com.finny.pet.di

import android.content.Context
import androidx.room.Room
import com.finny.pet.content.ContentLoader
import com.finny.pet.data.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton fun contentLoader(@ApplicationContext context: Context) = ContentLoader(context)
}

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton fun database(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "finny.db").addMigrations(MIGRATION_1_2).build()
    @Provides fun profileDao(db: AppDatabase)=db.profileDao()
    @Provides fun petDao(db: AppDatabase)=db.petDao()
    @Provides fun balanceDao(db: AppDatabase)=db.balanceDao()
    @Provides fun periodDao(db: AppDatabase)=db.periodDao()
    @Provides fun goalDao(db: AppDatabase)=db.goalDao()
    @Provides fun purchaseDao(db: AppDatabase)=db.purchaseDao()
    @Provides fun lessonDao(db: AppDatabase)=db.lessonDao()
    @Provides fun progressDao(db: AppDatabase)=db.progressDao()
    @Provides fun inventoryDao(db: AppDatabase)=db.inventoryDao()
    @Provides fun skinDao(db: AppDatabase)=db.skinDao()
    @Provides fun petNeedsDao(db: AppDatabase)=db.petNeedsDao()
    @Provides fun taskProgressV2Dao(db: AppDatabase)=db.taskProgressV2Dao()
    @Provides fun walletTransactionDao(db: AppDatabase)=db.walletTransactionDao()
    @Provides fun petLevelDao(db: AppDatabase)=db.petLevelDao()
}

@Module @InstallIn(SingletonComponent::class)
object RepositoryModule
