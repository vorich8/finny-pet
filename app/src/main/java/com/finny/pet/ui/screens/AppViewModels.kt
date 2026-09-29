package com.finny.pet.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finny.pet.audio.FinnyAudio
import com.finny.pet.data.database.ProfileEntity
import com.finny.pet.data.repository.BalanceRepository
import com.finny.pet.data.repository.ProfileRepository
import com.finny.pet.data.repository.PetRepository
import com.finny.pet.data.repository.ProgressRepository
import com.finny.pet.data.repository.InventoryRepository
import com.finny.pet.data.repository.PetNeedsRepository
import com.finny.pet.data.repository.PetLevelRepository
import com.finny.pet.data.repository.SkinRepository
import com.finny.pet.data.repository.GoalRepository
import com.finny.pet.data.repository.TaskProgressV2Repository
import com.finny.pet.data.database.ProgressEntity
import com.finny.pet.data.database.InventoryEntity
import com.finny.pet.data.database.PetNeedsEntity
import com.finny.pet.domain.usecase.CreatePet
import com.finny.pet.domain.usecase.CreateProfile
import com.finny.pet.domain.usecase.ResetProfile
import com.finny.pet.domain.usecase.RefreshPetNeeds
import com.finny.pet.domain.usecase.UseInventoryItem
import com.finny.pet.domain.usecase.CityPurchase
import com.finny.pet.domain.usecase.SpendResult
import com.finny.pet.domain.usecase.GrantPassiveIncome
import com.finny.pet.domain.usecase.BuySkin
import com.finny.pet.domain.usecase.SaveToGoal
import com.finny.pet.domain.usecase.AdvanceStoryDay
import com.finny.pet.domain.GameEconomy
import com.finny.pet.ui.games.EducationalGameCatalog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

private fun accessoryTitle(skin: com.finny.pet.data.database.SkinOwnershipEntity): String = when (skin.skinId) {
    "goal_hat" -> "Звёздная шапка"
    "goal_glasses" -> "Очки планировщика"
    "goal_crown" -> "Корона мечты"
    "hat_saver", "weekly_winter" -> "Тёплая шапка"
    "glasses_smart" -> "Умные очки"
    "crown_wise" -> "Корона мудрости"
    else -> skin.title.takeUnless { it.isBlank() || it.equals("preview", ignoreCase = true) }
        ?: "текущий головной убор"
}

data class BootstrapUiState(val loading:Boolean=true,val hasProfile:Boolean=false)
@HiltViewModel class BootstrapViewModel @Inject constructor(profiles:ProfileRepository):ViewModel(){
    private val _state=MutableStateFlow(BootstrapUiState()); val state:StateFlow<BootstrapUiState> = _state
    init { viewModelScope.launch { profiles.observe().first().let { _state.value=BootstrapUiState(false,it!=null) } } }
}

@HiltViewModel class CreatePetViewModel @Inject constructor(private val resetProfile:ResetProfile,private val createProfile:CreateProfile,private val createPet:CreatePet):ViewModel(){
    fun create(name:String,variantId:String,onDone:()->Unit)=viewModelScope.launch{
        // Экран выбора означает начало новой игры. Полная очистка защищает от
        // редкого состояния, когда профиль уже удалён, а старые счётчики дня остались.
        resetProfile()
        createProfile(name)
        createPet(variantId)
        onDone()
    }
}

data class HomeUiState(val ready:Boolean=false,val petName:String="",val coins:Int=0,val stars:Int=0,val variantId:String="",val equippedSkin:String?=null,val hunger:Int=80,val water:Int=80,val joy:Int=90,val health:Int=100,val level:Int=1,val period:Int=1,val petStage:Int=1,val suggestedGameId:String="scales",val suggestedTask:String="Весы",val suggestedLevel:Int=1)
@HiltViewModel class HomeViewModel @Inject constructor(profiles:ProfileRepository,balances:BalanceRepository,pets:PetRepository,skins:SkinRepository,needs:PetNeedsRepository,levels:PetLevelRepository,private val progress:ProgressRepository,private val refresh:RefreshPetNeeds):ViewModel(){
    private val petStyle=combine(pets.observe(),skins.observeAll()){pet,s->pet to s.firstOrNull{it.equipped}?.skinId}
    private val base=combine(profiles.observe(),balances.observe(),petStyle,needs.observe(),levels.observe()){p,b,style,n,l->
        val stars=b?.stars?:0
        val earnedLevel=when{stars>=25->6;stars>=18->5;stars>=12->4;stars>=7->3;stars>=3->2;else->1}
        HomeUiState(p!=null&&style.first!=null,p?.petName.orEmpty(),b?.coins?:0,stars,style.first?.variantId.orEmpty(),style.second,n?.food?:80,n?.water?:80,n?.mood?:90,n?.health?:100,maxOf(l?.level?:1,earnedLevel),p?.currentPeriod?:1,style.first?.stage?:1)
    }
    val state:StateFlow<HomeUiState> = combine(base,progress.observeAll()){home,items->
        val values=items.associate{it.key to it.value};val min=EducationalGameCatalog.all.minOf{values["game_level_${it.id}"]?:1};val choices=EducationalGameCatalog.all.filter{(values["game_level_${it.id}"]?:1)==min};val pick=choices[(home.period-1).coerceAtLeast(0)%choices.size]
        home.copy(suggestedGameId=pick.id,suggestedTask=pick.title,suggestedLevel=(values["game_level_${pick.id}"]?:1).coerceIn(1,10))
    }.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),HomeUiState())
    private val _announcement=MutableStateFlow<String?>(null);val announcement:StateFlow<String?> = _announcement
    init {
        viewModelScope.launch { refresh();while(true){delay(60_000);refresh()} }
        viewModelScope.launch { profiles.observe().filterNotNull().collect{p->if(p.currentPeriod>1&&(progress.get("announced_day")?.value?:1)<p.currentPeriod)_announcement.value=if(p.currentPeriod==2)"Начался день 2! Открыт Игровой центр: теперь доступны Тетрис и Змейка. На Работе тебя ждут новые уровни заданий." else "Начался день ${p.currentPeriod}! На Работе доступны новые уровни и можно повторить уже пройденные задания."} }
    }
    fun dismissAnnouncement()=viewModelScope.launch{progress.save(ProgressEntity("announced_day",state.value.period));_announcement.value=null}
}

data class WorkGameProgress(val id:String,val level:Int=1,val completed:Int=0)
@HiltViewModel class WorkProgressViewModel @Inject constructor(progress:ProgressRepository):ViewModel(){
    val state:StateFlow<List<WorkGameProgress>> = progress.observeAll().map{items->val values=items.associate{it.key to it.value};EducationalGameCatalog.all.map{game->WorkGameProgress(game.id,(values["game_level_${game.id}"]?:1).coerceIn(1,10),(1..10).count{values.containsKey("result_${game.id}_$it")})}}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),EducationalGameCatalog.all.map{WorkGameProgress(it.id)})
}

data class InventoryUiState(val items:List<InventoryEntity> = emptyList(),val skins:List<com.finny.pet.data.database.SkinOwnershipEntity> = emptyList(),val needs:PetNeedsEntity=PetNeedsEntity(),val message:String?=null)
@HiltViewModel class InventoryViewModel @Inject constructor(private val inventory:InventoryRepository,skins:SkinRepository,needs:PetNeedsRepository,private val useItem:UseInventoryItem,private val skinRepository:SkinRepository):ViewModel(){
    private val message=MutableStateFlow<String?>(null)
    val state=combine(inventory.observeAll(),skins.observeAll(),needs.observe(),message){items,s,n,m->InventoryUiState(items,s,n?:PetNeedsEntity(),m)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),InventoryUiState())
    fun use(id:String)=viewModelScope.launch{val item=inventory.get(id);message.value=if(useItem(id))when(item?.category){"FOOD"->"${item.title}: питомец поел, шкала еды выросла.";"WATER"->"${item.title}: питомец попил, шкала воды выросла.";"MEDICINE"->"Лекарство помогло: здоровье восстановлено.";else->"Предмет использован."} else "Сейчас «${item?.title?:"этот предмет"}» не нужен — он останется в рюкзаке."}
    fun clear(){message.value=null}
    fun equip(skin:com.finny.pet.data.database.SkinOwnershipEntity)=viewModelScope.launch{
        if(skin.equipped) skinRepository.unequipAll()
        else {
            val current=skinRepository.getEquipped()
            if(current!=null && current.skinId!=skin.skinId) message.value="Сначала сними «${accessoryTitle(current)}»: в слоте головного убора уже есть аксессуар."
            else skinRepository.equip(skin)
        }
    }
}

data class StoreUiState(val message:String?=null,val pending:CityGood?=null)
data class CityGood(val id:String,val icon:String,val title:String,val category:String,val price:Int)
@HiltViewModel class CityStoreViewModel @Inject constructor(private val purchase:CityPurchase,private val balances:BalanceRepository):ViewModel(){
    private val _state=MutableStateFlow(StoreUiState());val state:StateFlow<StoreUiState> = _state
    fun buy(g:CityGood,confirmed:Boolean=false)=viewModelScope.launch{when(purchase(g.id,g.title,g.category,g.price,confirmed)){SpendResult.SUCCESS->_state.value=StoreUiState("Покупка в рюкзаке!");SpendResult.NOT_ENOUGH->{val have=balances.get()?.coins?:0;_state.value=StoreUiState("«${g.title}» стоит ${g.price} ₽. У тебя $have; не хватает ${(g.price-have).coerceAtLeast(0)}. Выполни задание в одном из зданий и возвращайся.")};SpendResult.NEED_CONFIRMATION->_state.value=StoreUiState(pending=g)}}
    fun clear() {_state.value=StoreUiState()}
}

data class SkinGood(val id:String,val title:String,val price:Int=0,val level:Int=1,val rewardOnly:Boolean=false)
data class SkinShopUiState(val owned:List<com.finny.pet.data.database.SkinOwnershipEntity> = emptyList(),val message:String?=null,val pending:SkinGood?=null)
@HiltViewModel class SkinShopViewModel @Inject constructor(skins:SkinRepository,private val buySkin:BuySkin,private val skinRepository:SkinRepository):ViewModel(){
    private val feedback=MutableStateFlow<Pair<String?,SkinGood?>>(null to null)
    val state=combine(skins.observeAll(),feedback){owned,f->SkinShopUiState(owned,f.first,f.second)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),SkinShopUiState())
    fun buy(g:SkinGood,confirmed:Boolean=false)=viewModelScope.launch{when(buySkin(g.id,g.title,g.price,confirmed)){SpendResult.SUCCESS->feedback.value="Скин добавлен в гардероб!" to null;SpendResult.NOT_ENOUGH->feedback.value="Рублей пока не хватает. Выполни задание и возвращайся." to null;SpendResult.NEED_CONFIRMATION->feedback.value=null to g}}
    fun equip(g:SkinGood)=viewModelScope.launch{skinRepository.get(g.id)?.let{skin->if(skin.equipped)skinRepository.unequipAll() else {val current=skinRepository.getEquipped();if(current!=null&&current.skinId!=skin.skinId)feedback.value="Сначала сними «${accessoryTitle(current)}»: в слоте головного убора уже есть аксессуар." to null else skinRepository.equip(skin)}}}
    fun clear(){feedback.value=null to null}
}

data class GoalsUiState(val goals:List<com.finny.pet.data.database.GoalEntity> = emptyList(),val coins:Int=0,val message:String?=null)
@HiltViewModel class GoalsViewModel @Inject constructor(private val goals:GoalRepository,private val balances:BalanceRepository,private val save:SaveToGoal,private val skins:SkinRepository):ViewModel(){
    private val message=MutableStateFlow<String?>(null)
    val state=combine(goals.observeAll(),balances.observe(),message){g,b,m->GoalsUiState(g,b?.coins?:0,m)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),GoalsUiState())
    init{viewModelScope.launch{listOf(com.finny.pet.data.database.GoalEntity("goal_first","Первая мечта",60,active=true),com.finny.pet.data.database.GoalEntity("goal_home","Уютный домик",140,active=true),com.finny.pet.data.database.GoalEntity("goal_adventure","Большое приключение",240,active=true)).forEach{if(goals.get(it.id)==null)goals.save(it)}}}
    fun add(id:String,amount:Int)=viewModelScope.launch{val have=balances.get()?.coins?:0;val before=goals.get(id);val remaining=((before?.cost?:0)-(before?.saved?:0)).coerceAtLeast(0);if(amount>remaining){message.value="До цели осталось $remaining ₽. Выбери сумму не больше остатка."}else if(save(id,amount)){val goal=goals.get(id);val reward=when(id){"goal_first"->Triple("goal_hat","Звёздная шапка","шапка");"goal_home"->Triple("goal_glasses","Очки планировщика","очки");else->Triple("goal_crown","Корона мечты","корона")};if(goal?.completed==true&&skins.get(reward.first)?.owned!=true)skins.save(com.finny.pet.data.database.SkinOwnershipEntity(reward.first,reward.second,owned=true));message.value=if(goal?.completed==true)"Цель достигнута! Эксклюзивный аксессуар «${reward.second}» добавлен в гардероб. Купить его в магазине нельзя." else "Отложено $amount ₽. Ещё немного ближе к цели!"}else message.value="Ты выбрал $amount ₽, но свободно только $have. Не хватает ${(amount-have).coerceAtLeast(0)} ₽."}
    fun clear(){message.value=null}
}

data class StoryUiState(val day:Int=1,val earned:Int=0,val spent:Int=0,val saved:Int=0,val cared:Boolean=false,val weekEarned:Int=0,val weekSaved:Int=0,val goalCompleted:Boolean=false,val message:String?=null){
    val target:Int get()=GameEconomy.incomeTarget(day)
    val savingDone:Boolean get()=saved>0||goalCompleted
    val ready:Boolean get()=earned>=target&&cared&&savingDone
}
@HiltViewModel class StoryViewModel @Inject constructor(profiles:ProfileRepository,progress:ProgressRepository,goals:GoalRepository,private val advance:AdvanceStoryDay):ViewModel(){
    private val message=MutableStateFlow<String?>(null)
    val state=combine(profiles.observe(),progress.observeAll(),goals.observeAll(),message){profile,items,goalItems,msg->val p=items.associate{it.key to it.value};StoryUiState(profile?.currentPeriod?:1,p["day_earned"]?:0,p["day_spent"]?:0,p["day_saved"]?:0,(p["day_care"]?:0)>0,p["week_earned"]?:0,p["week_saved"]?:0,goalItems.any{it.active&&it.completed},msg)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),StoryUiState())
    fun check(onReady:()->Unit){val s=state.value;if(s.ready)onReady() else message.value=buildString{append("До итогов дня осталось: ");val missing=mutableListOf<String>();if(s.earned<s.target)missing+="заработать ещё ${s.target-s.earned} ₽";if(!s.cared)missing+="купить полезный корм для питомца";if(!s.savingDone)missing+="сделать вклад в цель";append(missing.joinToString(", "))}.replaceFirstChar{it.uppercase()}}
    private var advancing=false
    fun next(onNext:(Boolean)->Unit){if(advancing)return;advancing=true;viewModelScope.launch{val old=state.value.day;advance();onNext(old>=5)}}
    fun clear(){message.value=null}
}

data class AdultUiState(val stars:Int=0,val coins:Int=0,val completed:Int=0,val level:Int=1)
@HiltViewModel class AdultViewModel @Inject constructor(private val resetProfile:ResetProfile,balances:BalanceRepository,tasks:TaskProgressV2Repository):ViewModel(){
    val state=combine(balances.observe(),tasks.observeAll()){b,t->val stars=b?.stars?:0;AdultUiState(stars,b?.coins?:0,t.count{it.completed},when{stars>=25->6;stars>=18->5;stars>=12->4;stars>=7->3;stars>=3->2;else->1})}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),AdultUiState())
    fun reset(onDone:()->Unit)=viewModelScope.launch{resetProfile();onDone()}
}

object VisualPrefs { val animationsEnabled=androidx.compose.runtime.mutableStateOf(true);val soundEnabled=androidx.compose.runtime.mutableStateOf(true) }
data class ResultUiState(val ready:Boolean=false,val reward:Int=0,val petName:String="",val variantId:String="",val stage:Int=1,val newLevel:Int=0,val replay:Boolean=false,val gameId:String="scales")
@HiltViewModel class ResultViewModel @Inject constructor(private val progress:ProgressRepository,private val profiles:ProfileRepository,private val pets:PetRepository):ViewModel(){
    private val _state=MutableStateFlow(ResultUiState());val state:StateFlow<ResultUiState> = _state
    init{viewModelScope.launch{val profile=profiles.get();val pet=pets.get();val games=listOf("scales","priorities","shopping_list","friend_week","piggy","goal","secret_box","shop","change","expense_race","traffic","compare_prices");val index=(progress.get("last_game_index")?.value?:0).coerceIn(games.indices);_state.value=ResultUiState(profile!=null&&pet!=null,(progress.get("last_reward")?.value?:0).coerceAtLeast(0),profile?.petName.orEmpty(),pet?.variantId.orEmpty(),pet?.stage?:1,progress.get("last_new_level")?.value?:0,(progress.get("last_was_replay")?.value?:0)>0,games[index])}}
}
data class SettingsUiState(val animations:Boolean=true,val sound:Boolean=true)
@HiltViewModel class SettingsViewModel @Inject constructor(private val progress:ProgressRepository):ViewModel(){
    private val _state=MutableStateFlow(SettingsUiState());val state:StateFlow<SettingsUiState> = _state
    init{viewModelScope.launch{val a=(progress.get("settings_animation")?.value?:1)==1;val s=(progress.get("settings_sound")?.value?:1)==1;_state.value=SettingsUiState(a,s);VisualPrefs.animationsEnabled.value=a;VisualPrefs.soundEnabled.value=s}}
    fun animation(enabled:Boolean)=viewModelScope.launch{_state.value=_state.value.copy(animations=enabled);VisualPrefs.animationsEnabled.value=enabled;progress.save(ProgressEntity("settings_animation",if(enabled)1 else 0))}
    fun sound(enabled:Boolean)=viewModelScope.launch{_state.value=_state.value.copy(sound=enabled);VisualPrefs.soundEnabled.value=enabled;progress.save(ProgressEntity("settings_sound",if(enabled)1 else 0));if(enabled)FinnyAudio.playPreview()}
}
