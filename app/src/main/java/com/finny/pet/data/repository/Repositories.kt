package com.finny.pet.data.repository

import com.finny.pet.data.database.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class ProfileRepository @Inject constructor(private val dao: ProfileDao) { fun observe()=dao.observe(); suspend fun get()=dao.get(); suspend fun save(v:ProfileEntity)=dao.upsert(v); suspend fun clear()=dao.clear() }
@Singleton class PetRepository @Inject constructor(private val dao: PetDao) { fun observe()=dao.observe(); suspend fun get()=dao.get(); suspend fun save(v:PetEntity)=dao.upsert(v); suspend fun clear()=dao.clear() }
@Singleton class BalanceRepository @Inject constructor(private val dao: BalanceDao) { fun observe()=dao.observe(); suspend fun get()=dao.get(); suspend fun save(v:BalanceEntity)=dao.upsert(v); suspend fun clear()=dao.clear() }
@Singleton class PeriodRepository @Inject constructor(private val dao: PeriodDao) { fun observeAll()=dao.observeAll(); suspend fun get(n:Int)=dao.get(n); suspend fun save(v:PeriodEntity)=dao.upsert(v); suspend fun clear()=dao.clear() }
@Singleton class GoalRepository @Inject constructor(private val dao: GoalDao) { fun observeAll()=dao.observeAll(); suspend fun get(id:String)=dao.get(id); suspend fun save(v:GoalEntity)=dao.upsert(v); suspend fun clear()=dao.clear() }
@Singleton class PurchaseRepository @Inject constructor(private val dao: PurchaseDao) { fun observeAll()=dao.observeAll(); suspend fun save(v:PurchaseEntity)=dao.insert(v); suspend fun clear()=dao.clear() }
@Singleton class LessonRepository @Inject constructor(private val dao: LessonDao) { fun observeAll()=dao.observeAll(); suspend fun save(v:LessonEntity)=dao.upsert(v); suspend fun clear()=dao.clear() }
@Singleton class ProgressRepository @Inject constructor(private val dao: ProgressDao) { fun observeAll()=dao.observeAll(); suspend fun get(k:String)=dao.get(k); suspend fun save(v:ProgressEntity)=dao.upsert(v); suspend fun clear()=dao.clear() }
@Singleton class InventoryRepository @Inject constructor(private val dao:InventoryDao){fun observeAll()=dao.observeAll();suspend fun get(id:String)=dao.get(id);suspend fun save(v:InventoryEntity)=dao.upsert(v);suspend fun clear()=dao.clear()}
@Singleton class SkinRepository @Inject constructor(private val dao:SkinDao){fun observeAll()=dao.observeAll();suspend fun get(id:String)=dao.get(id);suspend fun getEquipped()=dao.getEquipped();suspend fun save(v:SkinOwnershipEntity)=dao.upsert(v);suspend fun equip(v:SkinOwnershipEntity){dao.upsert(v.copy(owned=true,equipped=true))};suspend fun unequip(v:SkinOwnershipEntity){dao.upsert(v.copy(equipped=false))};suspend fun unequipAll()=dao.unequipAll();suspend fun clear()=dao.clear()}
@Singleton class PetNeedsRepository @Inject constructor(private val dao:PetNeedsDao){fun observe()=dao.observe();suspend fun get()=dao.get();suspend fun save(v:PetNeedsEntity)=dao.upsert(v);suspend fun clear()=dao.clear()}
@Singleton class TaskProgressV2Repository @Inject constructor(private val dao:TaskProgressV2Dao){fun observeAll()=dao.observeAll();suspend fun get(id:String)=dao.get(id);suspend fun save(v:TaskProgressV2Entity)=dao.upsert(v);suspend fun clear()=dao.clear()}
@Singleton class WalletRepository @Inject constructor(private val dao:WalletTransactionDao){fun observeAll()=dao.observeAll();suspend fun add(v:WalletTransactionEntity)=dao.insert(v);suspend fun clear()=dao.clear()}
@Singleton class PetLevelRepository @Inject constructor(private val dao:PetLevelDao){fun observe()=dao.observe();suspend fun get()=dao.get();suspend fun save(v:PetLevelEntity)=dao.upsert(v);suspend fun clear()=dao.clear()}
