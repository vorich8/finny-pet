package com.finny.pet.domain.usecase

import androidx.room.withTransaction
import com.finny.pet.data.database.*
import com.finny.pet.data.repository.*
import com.finny.pet.domain.BalanceCalculator
import com.finny.pet.domain.PetGrowthCalculator
import com.finny.pet.domain.Reward
import com.finny.pet.domain.GameEconomy
import javax.inject.Inject
import kotlin.random.Random

class CreateProfile @Inject constructor(private val profiles: ProfileRepository, private val balances: BalanceRepository, private val periods: PeriodRepository, private val goals:GoalRepository, private val progress:ProgressRepository, private val needs:PetNeedsRepository) {
    suspend operator fun invoke(petName: String) {
        profiles.save(ProfileEntity(petName = petName))
        balances.save(BalanceEntity())
        (1..5).forEach { periods.save(PeriodEntity(it, unlocked = it == 1)) }
        listOf(
            GoalEntity("goal_first","Первая мечта",60,active=true),
            GoalEntity("goal_home","Уютный домик",140,active=false),
            GoalEntity("goal_adventure","Большое приключение",240,active=false)
        ).forEach { goals.save(it) }
        progress.save(ProgressEntity("story_day",1))
        progress.save(ProgressEntity("next_illness_minute",(System.currentTimeMillis()/60_000L).toInt()+Random.nextInt(360,721)))
        needs.save(PetNeedsEntity())
    }
}

/** Seeds a complete local demo profile when the app is launched with --ez developer true. */
class EnableDeveloperMode @Inject constructor(
    private val profiles: ProfileRepository,
    private val pets: PetRepository,
    private val balances: BalanceRepository,
    private val periods: PeriodRepository,
    private val goals: GoalRepository,
    private val progress: ProgressRepository,
    private val needs: PetNeedsRepository,
    private val inventory: InventoryRepository,
    private val skins: SkinRepository
) {
    suspend operator fun invoke() {
        skins.clear()
        profiles.save(ProfileEntity(petName = "Демо", currentPeriod = 5))
        pets.save(PetEntity(variantId = "rabbit_1", stage = 3))
        balances.save(BalanceEntity(coins = 999, stars = 99))
        (1..5).forEach { periods.save(PeriodEntity(it, unlocked = true, completed = true)) }
        listOf(
            GoalEntity("goal_first", "Первая мечта", 60, 60, true, true),
            GoalEntity("goal_home", "Уютный домик", 140, 140, true, true),
            GoalEntity("goal_adventure", "Большое приключение", 240, 240, true, true)
        ).forEach { goals.save(it) }
        listOf(
            SkinOwnershipEntity("goal_hat", "Звёздная шапка", owned = true),
            SkinOwnershipEntity("goal_crown", "Корона мечты", owned = true)
        ).forEach { skins.save(it) }
        // Игрушки пока не доступны в приложении, поэтому демо-профиль не получает их.
        inventory.clear()
        needs.save(PetNeedsEntity(food = 100, water = 100, health = 100, mood = 100))
        listOf("scales", "priorities", "shopping_list", "friend_week", "piggy", "goal", "secret_box", "shop", "change", "expense_race", "traffic", "compare_prices").forEach { id ->
            progress.save(ProgressEntity("game_level_$id", 10))
            (1..10).forEach { progress.save(ProgressEntity("result_${id}_$it", 3)) }
        }
        progress.save(ProgressEntity("developer_mode", 1))
    }
}

enum class SpendResult { SUCCESS, NEED_CONFIRMATION, NOT_ENOUGH }

class RefreshPetNeeds @Inject constructor(private val needs:PetNeedsRepository,private val progress:ProgressRepository){
    suspend operator fun invoke(){
        val now=System.currentTimeMillis(); val old=needs.get() ?: PetNeedsEntity(updatedAt=now)
        // Потребности меняются игровыми шагами раз в полчаса. Даже после суток
        // вне игры питомец не оказывается мгновенно на нуле.
        val minutes=((now-old.updatedAt)/60_000L).toInt().coerceIn(0,1_440)
        val careSteps=minutes/30
        val nowMinute=(now/60_000L).toInt();val active=(progress.get("illness_active")?.value?:0)>0;var next=progress.get("next_illness_minute")?.value?:0
        if(next<=0){next=nowMinute+Random.nextInt(360,721);progress.save(ProgressEntity("next_illness_minute",next))}
        val becomesSick=!active&&nowMinute>=next
        if(becomesSick)progress.save(ProgressEntity("illness_active",1))
        if(careSteps>0||becomesSick) needs.save(old.copy(food=(old.food-careSteps).coerceAtLeast(0),water=(old.water-careSteps).coerceAtLeast(0),mood=(old.mood-careSteps/2).coerceAtLeast(0),health=if(becomesSick)minOf(old.health,45) else old.health,updatedAt=now))
        else if(needs.get()==null) needs.save(old)
    }
}

class UseInventoryItem @Inject constructor(private val inventory:InventoryRepository,private val needs:PetNeedsRepository,private val progress:ProgressRepository){
    suspend operator fun invoke(id:String):Boolean{
        val item=inventory.get(id) ?: return false; if(item.quantity<=0)return false
        val n=needs.get() ?: PetNeedsEntity()
        if((item.category=="MEDICINE"&&n.health>=100)||(item.category=="FOOD"&&n.food>=100)||(item.category=="WATER"&&n.water>=100))return false
        val foodBoost=when(id){"food_fish"->35;"food_bowl"->30;"food_carrot"->20;"food_apple"->18;"food_berry"->15;else->25}
        val waterBoost=when(id){"water_spring"->35;"water_milk"->25;"water_tea"->20;else->30}
        val updated=when(item.category){"FOOD"->n.copy(food=(n.food+foodBoost).coerceAtMost(100));"WATER"->n.copy(water=(n.water+waterBoost).coerceAtMost(100));"MEDICINE"->n.copy(health=100);else->return false}
        if(item.category=="MEDICINE"){progress.save(ProgressEntity("illness_active",0));progress.save(ProgressEntity("next_illness_minute",(System.currentTimeMillis()/60_000L).toInt()+Random.nextInt(720,1_441)))}
        inventory.save(item.copy(quantity=item.quantity-1));needs.save(updated.copy(updatedAt=System.currentTimeMillis()));return true
    }
}

class CityPurchase @Inject constructor(private val db:AppDatabase,private val balances:BalanceRepository,private val inventory:InventoryRepository,private val wallet:WalletRepository,private val progress:ProgressRepository,private val needs:PetNeedsRepository){
    suspend operator fun invoke(itemId:String,title:String,category:String,price:Int,confirmed:Boolean=false):SpendResult=db.withTransaction{
        val balance=balances.get() ?: BalanceEntity()
        if(price<=0)return@withTransaction SpendResult.NOT_ENOUGH
        val previous=inventory.get(itemId)
        if(category=="WANT" && (previous?.quantity?:0)>0)return@withTransaction SpendResult.NOT_ENOUGH
        if(balance.coins<price)return@withTransaction SpendResult.NOT_ENOUGH
        if(!confirmed)return@withTransaction SpendResult.NEED_CONFIRMATION
        balances.save(balance.copy(coins=balance.coins-price));val old=inventory.get(itemId)
        inventory.save(InventoryEntity(itemId,title,category,if(category=="WANT")1 else (old?.quantity?:0)+1));wallet.add(WalletTransactionEntity(amount=-price,reason="Покупка: $title"))
        if(category=="WANT") progress.save(ProgressEntity("toy_placed_$itemId",1))
        progress.save(ProgressEntity("day_spent",(progress.get("day_spent")?.value?:0)+price))
        val actualKey=if(category=="WANT") "day_want_spent" else "day_required_spent"
        progress.save(ProgressEntity(actualKey,(progress.get(actualKey)?.value?:0)+price))
        if(category=="FOOD")progress.save(ProgressEntity("day_care",1))
        val currentNeeds=needs.get() ?: PetNeedsEntity()
        val moodBoost=when(category){"WANT"->8;"FOOD","WATER"->3;else->0}
        if(moodBoost>0) needs.save(currentNeeds.copy(mood=(currentNeeds.mood+moodBoost).coerceAtMost(100),updatedAt=System.currentTimeMillis()))
        SpendResult.SUCCESS
    }
}

class CreatePet @Inject constructor(private val pets: PetRepository) { suspend operator fun invoke(variantId:String)=pets.save(PetEntity(variantId=variantId)) }
class CompleteLesson @Inject constructor(private val lessons: LessonRepository) { suspend operator fun invoke(v:LessonEntity)=lessons.save(v.copy(completed=true)) }

class DistributeBudget @Inject constructor() {
    operator fun invoke(income:Int, required:Int, optional:Int, saving:Int) = BalanceCalculator.calculate(income, required + optional + saving)
}

class BuyItem @Inject constructor(private val balances:BalanceRepository, private val purchases:PurchaseRepository) {
    suspend operator fun invoke(item:PurchaseEntity):Boolean {
        val balance=balances.get() ?: BalanceEntity()
        if(!BalanceCalculator.canSpend(balance.coins,item.price)) return false
        purchases.save(item); balances.save(balance.copy(coins=balance.coins-item.price)); return true
    }
}

class SaveToGoal @Inject constructor(private val db:AppDatabase,private val goals:GoalRepository, private val balances:BalanceRepository,private val progress:ProgressRepository) {
    suspend operator fun invoke(id:String, amount:Int):Boolean = db.withTransaction {
        val balance=balances.get() ?: return@withTransaction false; val goal=goals.get(id) ?: return@withTransaction false
        if(amount<=0||goal.completed||amount>goal.cost-goal.saved||!BalanceCalculator.canSpend(balance.coins,amount)) return@withTransaction false
        val completed=goal.saved+amount>=goal.cost
        balances.save(balance.copy(coins=balance.coins-amount)); goals.save(goal.copy(saved=goal.saved+amount, completed=completed, active=!completed))
        if(completed){
            val next=when(id){"goal_first"->"goal_home";"goal_home"->"goal_adventure";else->null}
            next?.let{goals.get(it)?.let{following->if(!following.completed)goals.save(following.copy(active=true))}}
        }
        progress.save(ProgressEntity("day_saved",(progress.get("day_saved")?.value?:0)+amount));progress.save(ProgressEntity("week_saved",(progress.get("week_saved")?.value?:0)+amount));true
    }
}

class WithdrawFromGoal @Inject constructor(private val db:AppDatabase,private val goals:GoalRepository,private val balances:BalanceRepository) {
    suspend operator fun invoke(id:String):Boolean = db.withTransaction {
        val goal=goals.get(id) ?: return@withTransaction false
        val amount=goal.saved
        if(amount<=0 || goal.completed)return@withTransaction false
        val balance=balances.get() ?: BalanceEntity()
        balances.save(balance.copy(coins=balance.coins+amount))
        goals.save(goal.copy(saved=0,active=true))
        true
    }
}

class CompletePeriod @Inject constructor(private val periods:PeriodRepository, private val profiles:ProfileRepository) {
    suspend operator fun invoke(number:Int) { periods.save(PeriodEntity(number,true,true)); if(number<5) periods.save(PeriodEntity(number+1,true,false)) }
}

class AdvanceStoryDay @Inject constructor(private val profiles:ProfileRepository,private val periods:PeriodRepository,private val progress:ProgressRepository,private val pets:PetRepository){
    suspend operator fun invoke():Int{
        val profile=profiles.get()?:return 1
        val day=profile.currentPeriod.coerceIn(1,5)
        periods.save(PeriodEntity(day,true,true))
        val next=(day+1).coerceAtMost(5)
        if(day<5){periods.save(PeriodEntity(next,true,false));profiles.save(profile.copy(currentPeriod=next))}
        val pet=pets.get();if(pet!=null)pets.save(pet.copy(stage=when{next>=5->3;next>=2->2;else->1}))
        listOf("day_earned","day_spent","day_saved","day_care","day_required_spent","day_want_spent","plan_required","plan_wants","plan_saving","plan_confirmed").forEach{progress.save(ProgressEntity(it,0))}
        return next
    }
}

class GrowPet @Inject constructor(private val pets:PetRepository) {
    suspend operator fun invoke(pet:PetEntity, completedPeriod:Int) = pets.save(pet.copy(stage=PetGrowthCalculator.stage(completedPeriod).ordinal+1))
}

class GrantPassiveIncome @Inject constructor(private val balances:BalanceRepository,private val wallet:WalletRepository){
    suspend operator fun invoke(level:Int):Int{
        val amount=level.coerceIn(1,6)-1
        if(amount<=0)return 0
        val balance=balances.get()?:BalanceEntity()
        balances.save(balance.copy(coins=balance.coins+amount))
        wallet.add(WalletTransactionEntity(amount=amount,reason="Доход за развитие питомца"))
        return amount
    }
}

class BuySkin @Inject constructor(private val db:AppDatabase,private val balances:BalanceRepository,private val skins:SkinRepository,private val wallet:WalletRepository,private val progress:ProgressRepository){
    suspend operator fun invoke(id:String,title:String,price:Int,confirmed:Boolean=false):SpendResult = db.withTransaction {
        if(skins.get(id)?.owned==true)return@withTransaction SpendResult.SUCCESS
        val balance=balances.get()?:BalanceEntity()
        if(price<=0)return@withTransaction SpendResult.NOT_ENOUGH
        if(balance.coins<price)return@withTransaction SpendResult.NOT_ENOUGH
        if(!confirmed)return@withTransaction SpendResult.NEED_CONFIRMATION
        balances.save(balance.copy(coins=balance.coins-price))
        skins.save(SkinOwnershipEntity(id,title,owned=true))
        wallet.add(WalletTransactionEntity(amount=-price,reason="Скин: $title"))
        progress.save(ProgressEntity("day_want_spent",(progress.get("day_want_spent")?.value?:0)+price))
        SpendResult.SUCCESS
    }
}

/** Awards coins only on the first completion and only the improvement in stars on replays. */
class AwardGameResult @Inject constructor(private val db:AppDatabase,private val balances:BalanceRepository,private val progress:ProgressRepository,private val tasks:TaskProgressV2Repository,private val wallet:WalletRepository) {
    suspend operator fun invoke(gameKey:String,coins:Int,stars:Int):Reward = db.withTransaction {
        val safeStars=stars.coerceIn(0,3)
        val old=progress.get("result_$gameKey")?.value
        val balance=balances.get() ?: BalanceEntity()
        val coinDelta=if(old==null) coins.coerceAtLeast(0) else 0
        val starDelta=if(old==null) safeStars else (safeStars-old).coerceAtLeast(0)
        if(coinDelta>0||starDelta>0) balances.save(balance.copy(coins=(balance.coins+coinDelta).coerceAtLeast(0),stars=(balance.stars+starDelta).coerceAtLeast(0)))
        if(coinDelta>0)wallet.add(WalletTransactionEntity(amount=coinDelta,reason="Награда за задание"))
        if(coinDelta>0){progress.save(ProgressEntity("day_earned",(progress.get("day_earned")?.value?:0)+coinDelta));progress.save(ProgressEntity("week_earned",(progress.get("week_earned")?.value?:0)+coinDelta))}
        val task=tasks.get(gameKey)?:TaskProgressV2Entity(gameKey)
        tasks.save(task.copy(attempts=task.attempts+1,bestStars=maxOf(task.bestStars,safeStars),completed=true,lastScore=safeStars,updatedAt=System.currentTimeMillis()))
        if(old==null||safeStars>old) progress.save(ProgressEntity("result_$gameKey",safeStars))
        progress.save(ProgressEntity("last_reward",coinDelta))
        val completedLevel=gameKey.substringAfterLast('_').toIntOrNull()?:0
        val gameId=gameKey.substringBeforeLast('_')
        val gameIndex=listOf("scales","priorities","shopping_list","friend_week","piggy","goal","secret_box","shop","change","expense_race","traffic","compare_prices").indexOf(gameId)
        progress.save(ProgressEntity("last_completed_level",completedLevel))
        progress.save(ProgressEntity("last_game_index",gameIndex.coerceAtLeast(0)))
        progress.save(ProgressEntity("last_was_replay",if(old==null)0 else 1))
        progress.save(ProgressEntity("last_new_level",if(old==null&&completedLevel in 1..9)completedLevel+1 else 0))
        Reward(coinDelta,starDelta)
    }
}

/** Fixed fan-game income from the story specification. Arcade games do not award stars. */
class EarnArcadeReward @Inject constructor(
    private val balances:BalanceRepository,
    private val progress:ProgressRepository,
    private val wallet:WalletRepository
) {
    suspend operator fun invoke(game:String):Int {
        val reward=GameEconomy.arcadeReward
        val balance=balances.get() ?: BalanceEntity()
        balances.save(balance.copy(coins=balance.coins+reward))
        wallet.add(WalletTransactionEntity(amount=reward,reason="Игровой центр: $game"))
        progress.save(ProgressEntity("day_earned",(progress.get("day_earned")?.value?:0)+reward))
        progress.save(ProgressEntity("week_earned",(progress.get("week_earned")?.value?:0)+reward))
        progress.save(ProgressEntity("last_reward",reward))
        return reward
    }
}

class ResetProfile @Inject constructor(
    private val db:AppDatabase,
    private val profiles:ProfileRepository,
    private val pets:PetRepository,
    private val balances:BalanceRepository,
    private val periods:PeriodRepository,
    private val goals:GoalRepository,
    private val purchases:PurchaseRepository,
    private val lessons:LessonRepository,
    private val progress:ProgressRepository,
    private val inventory:InventoryRepository,
    private val skins:SkinRepository,
    private val needs:PetNeedsRepository,
    private val tasks:TaskProgressV2Repository,
    private val wallet:WalletRepository,
    private val levels:PetLevelRepository
) {
    suspend operator fun invoke() = db.withTransaction {
        wallet.clear();tasks.clear();needs.clear();skins.clear();inventory.clear();levels.clear();progress.clear(); purchases.clear(); goals.clear(); periods.clear(); balances.clear(); pets.clear(); profiles.clear(); lessons.clear()
    }
}
