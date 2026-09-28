package com.finny.pet.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.SavedStateHandle
import com.finny.pet.domain.*
import com.finny.pet.domain.usecase.AwardGameResult
import com.finny.pet.data.repository.ProgressRepository
import com.finny.pet.data.database.ProgressEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private fun starsFor(errors:Int)=when { errors==0->3; errors<=2->2; else->1 }
private fun rewardFor(level:Int)=GameEconomy.rewardForLevel(level)
private suspend fun gameLevel(progress:ProgressRepository,id:String)=(progress.get("game_level_$id")?.value?:1).coerceIn(1,10)
private suspend fun selectedLevel(saved:SavedStateHandle,progress:ProgressRepository,id:String):Int{val unlocked=gameLevel(progress,id);val requested=saved.get<Int>("level")?:unlocked;return requested.coerceIn(1,unlocked)}
private suspend fun nextLevel(progress:ProgressRepository,id:String,level:Int){val unlocked=gameLevel(progress,id);val next=maxOf(unlocked,(level+1).coerceAtMost(10));if(next!=unlocked)progress.save(ProgressEntity("game_level_$id",next))}

data class ScalesUiState(val level:Int=1,val cards:List<MoneyCard>,val income:Int=0,val expenses:Int=0,val errors:Int=0,val message:String=""){val balance:Int get()=income-expenses}
private fun scalesCards(level:Int)=when(level){
    2->listOf(MoneyCard("work","Заработок",70,true),MoneyCard("reward","Награда",30,true),MoneyCard("food","Еда",25,false),MoneyCard("rent","Аренда",35,false),MoneyCard("toy","Игрушка",20,false),MoneyCard("sweet","Сладости",10,false))
    3->listOf(MoneyCard("work","Заработок",70,true),MoneyCard("reward","Награда",50,true),MoneyCard("gift","Подарок",30,true),MoneyCard("food","Еда",30,false),MoneyCard("rent","Аренда",40,false),MoneyCard("transport","Транспорт",20,false),MoneyCard("toy","Игрушка",25,false),MoneyCard("sweet","Сладости",15,false))
    4->listOf(MoneyCard("work","Заработок",80,true),MoneyCard("gift","Подарок",40,true),MoneyCard("food","Еда",30,false),MoneyCard("rent","Аренда",45,false),MoneyCard("transport","Транспорт",20,false),MoneyCard("toy","Игрушка",15,false),MoneyCard("sweet","Сладости",10,false))
    5->listOf(MoneyCard("work","Заработок",90,true),MoneyCard("reward","Награда",35,true),MoneyCard("gift","Подарок",25,true),MoneyCard("food","Еда",35,false),MoneyCard("rent","Аренда",45,false),MoneyCard("transport","Транспорт",25,false),MoneyCard("toy","Игрушка",20,false))
    6->listOf(MoneyCard("work","Заработок",100,true),MoneyCard("gift","Подарок",40,true),MoneyCard("food","Еда",35,false),MoneyCard("rent","Аренда",50,false),MoneyCard("transport","Транспорт",20,false),MoneyCard("medicine","Лекарство",15,false),MoneyCard("toy","Игрушка",10,false))
    7->listOf(MoneyCard("work","Заработок",110,true),MoneyCard("reward","Награда",40,true),MoneyCard("gift","Подарок",30,true),MoneyCard("food","Еда",40,false),MoneyCard("rent","Аренда",55,false),MoneyCard("transport","Транспорт",20,false),MoneyCard("medicine","Лекарство",15,false),MoneyCard("toy","Игрушка",25,false))
    8->listOf(MoneyCard("work","Заработок",120,true),MoneyCard("gift","Подарок",50,true),MoneyCard("reward","Награда",30,true),MoneyCard("food","Еда",45,false),MoneyCard("rent","Аренда",60,false),MoneyCard("transport","Транспорт",25,false),MoneyCard("medicine","Лекарство",20,false),MoneyCard("toy","Игрушка",20,false),MoneyCard("sweet","Сладости",10,false))
    9->listOf(MoneyCard("work","Заработок",130,true),MoneyCard("reward","Награда",45,true),MoneyCard("gift","Подарок",25,true),MoneyCard("food","Еда",40,false),MoneyCard("rent","Аренда",65,false),MoneyCard("transport","Транспорт",25,false),MoneyCard("medicine","Лекарство",20,false),MoneyCard("toy","Игрушка",30,false))
    10->listOf(MoneyCard("work","Заработок",150,true),MoneyCard("reward","Награда",50,true),MoneyCard("gift","Подарок",30,true),MoneyCard("food","Еда",50,false),MoneyCard("rent","Аренда",70,false),MoneyCard("transport","Транспорт",30,false),MoneyCard("medicine","Лекарство",20,false),MoneyCard("toy","Игрушка",35,false),MoneyCard("sweet","Сладости",15,false))
    else->listOf(MoneyCard("reward","Награда",50,true),MoneyCard("gift","Подарок",30,true),MoneyCard("food","Еда",20,false),MoneyCard("rent","Аренда домика",30,false),MoneyCard("toy","Игрушка",20,false))
}
@HiltViewModel class ScalesViewModel @Inject constructor(private val saved:SavedStateHandle,private val award:AwardGameResult,private val progress:ProgressRepository):ViewModel(){
    private val _state=MutableStateFlow(ScalesUiState(cards=scalesCards(1)));val state:StateFlow<ScalesUiState> = _state
    init{viewModelScope.launch{val l=selectedLevel(saved,progress,"scales");_state.value=ScalesUiState(level=l,cards=scalesCards(l))}}
    fun place(card:MoneyCard,incomeZone:Boolean){_state.update{s->when{
        card.income!=incomeZone->s.copy(errors=s.errors+1,message=if(card.income)"${card.title} — это доход: эти деньги мы получаем. Помести карточку в «Доход»." else "${card.title} — это расход: за это мы отдаём деньги. Помести карточку в «Расход».")
        !card.income && s.expenses+card.amount>s.income->s.copy(errors=s.errors+1,message="Так расходы станут больше доходов. Сначала добавь карточки с деньгами, которые мы получаем.")
        else->s.copy(cards=s.cards-card,income=s.income+if(card.income)card.amount else 0,expenses=s.expenses+if(!card.income)card.amount else 0,message="Верно!")}}
    }
    fun clearMessage(){_state.update{it.copy(message="")}}
    fun finish(onDone:(Int)->Unit)=viewModelScope.launch{val s=_state.value;if(s.cards.isEmpty()&&s.balance>=0){val stars=starsFor(s.errors);award("scales_${s.level}",rewardFor(s.level),stars);nextLevel(progress,"scales",s.level);onDone(stars)}}
}

enum class PriorityZone(val title:String){NOW("Нужно сейчас"),LATER("Можно потом"),NO("Не покупать сейчас")}
data class PriorityLevel(val budget:Int,val cards:List<PriorityCard>)
private fun priorityLevel(n:Int)=when(n){
    2->PriorityLevel(70,listOf(PriorityCard("rent","Аренда домика",30,true),PriorityCard("food","Еда",20,true),PriorityCard("water","Вода",10,true),PriorityCard("toy","Игрушка",25,false),PriorityCard("ball","Мяч",20,false)))
    3->PriorityLevel(100,listOf(PriorityCard("food","Еда",25,true),PriorityCard("water","Вода",15,true),PriorityCard("medicine","Лекарство",20,true),PriorityCard("toy","Игрушка",25,false),PriorityCard("ball","Мяч",20,false),PriorityCard("ice","Мороженое",10,false),PriorityCard("saving","Накопление",20,false,"NO")))
    4->PriorityLevel(80,listOf(PriorityCard("food","Еда",25,true),PriorityCard("water","Вода",10,true),PriorityCard("medicine","Лекарство",15,true),PriorityCard("toy","Игрушка",20,false),PriorityCard("ball","Мяч",20,false),PriorityCard("console","Приставка",30,false,"NO"),PriorityCard("ice","Мороженое",10,false,"NO")))
    5->PriorityLevel(90,listOf(PriorityCard("rent","Аренда",30,true),PriorityCard("food","Еда",25,true),PriorityCard("water","Вода",10,true),PriorityCard("book","Книжка",15,false),PriorityCard("ball","Мяч",20,false),PriorityCard("console","Приставка",30,false,"NO"),PriorityCard("ice","Мороженое",10,false,"NO")))
    6->PriorityLevel(100,listOf(PriorityCard("rent","Аренда",30,true),PriorityCard("food","Еда",25,true),PriorityCard("water","Вода",15,true),PriorityCard("medicine","Лекарство",20,true),PriorityCard("book","Книжка",15,false),PriorityCard("toy","Игрушка",20,false),PriorityCard("console","Приставка",35,false,"NO"),PriorityCard("ice","Мороженое",10,false,"NO")))
    7->PriorityLevel(110,listOf(PriorityCard("rent","Аренда",35,true),PriorityCard("food","Еда",30,true),PriorityCard("water","Вода",15,true),PriorityCard("medicine","Лекарство",20,true),PriorityCard("toy","Игрушка",20,false),PriorityCard("book","Книжка",15,false),PriorityCard("console","Приставка",35,false,"NO"),PriorityCard("ice","Мороженое",10,false,"NO")))
    8->PriorityLevel(120,listOf(PriorityCard("rent","Аренда",35,true),PriorityCard("food","Еда",30,true),PriorityCard("water","Вода",15,true),PriorityCard("medicine","Лекарство",20,true),PriorityCard("toy","Игрушка",25,false),PriorityCard("ball","Мяч",20,false),PriorityCard("console","Приставка",35,false,"NO"),PriorityCard("sweet","Сладости",10,false,"NO")))
    9->PriorityLevel(130,listOf(PriorityCard("rent","Аренда",40,true),PriorityCard("food","Еда",30,true),PriorityCard("water","Вода",15,true),PriorityCard("medicine","Лекарство",20,true),PriorityCard("book","Книжка",10,true),PriorityCard("toy","Игрушка",25,false),PriorityCard("ball","Мяч",25,false),PriorityCard("console","Приставка",35,false,"NO"),PriorityCard("ice","Мороженое",10,false,"NO")))
    10->PriorityLevel(150,listOf(PriorityCard("rent","Аренда",45,true),PriorityCard("food","Еда",35,true),PriorityCard("water","Вода",20,true),PriorityCard("medicine","Лекарство",25,true),PriorityCard("book","Книжка",10,true),PriorityCard("toy","Игрушка",30,false),PriorityCard("ball","Мяч",25,false),PriorityCard("console","Приставка",40,false,"NO"),PriorityCard("sweet","Сладости",15,false,"NO")))
    else->PriorityLevel(50,listOf(PriorityCard("food","Еда",20,true),PriorityCard("water","Вода",10,true),PriorityCard("toy","Игрушка",25,false),PriorityCard("ice","Мороженое",10,false)))
}
data class PrioritiesUiState(val level:Int=1,val budget:Int,val cards:List<PriorityCard>,val placed:Int=0,val errors:Int=0,val message:String="")
@HiltViewModel class PrioritiesViewModel @Inject constructor(private val saved:SavedStateHandle,private val award:AwardGameResult,private val progress:ProgressRepository):ViewModel(){private val first=priorityLevel(1);private val _state=MutableStateFlow(PrioritiesUiState(1,first.budget,first.cards));val state:StateFlow<PrioritiesUiState> = _state
    init{viewModelScope.launch{val l=selectedLevel(saved,progress,"priorities");val data=priorityLevel(l);_state.value=PrioritiesUiState(l,data.budget,data.cards)}}
    fun place(c:PriorityCard,z:PriorityZone){val ok=c.target==z.name;_state.update{s->if(!ok)s.copy(errors=s.errors+1,message=if(c.required)"${c.title} необходимо питомцу сейчас. Сначала обеспечь еду, воду, жильё и лечение." else if(c.target=="NO")"${c.title} сейчас не помещается в план. Эту покупку лучше пропустить, чтобы хватило на важное." else "${c.title} — желание. Его можно купить позже, когда важные расходы уже оплачены.") else if(z==PriorityZone.NOW&&s.placed+c.price>s.budget)s.copy(errors=s.errors+1,message="Не хватает ${s.placed+c.price-s.budget} монет. Перенеси необязательную покупку в «Можно потом».") else s.copy(cards=s.cards-c,placed=s.placed+if(z==PriorityZone.NOW)c.price else 0,message="Хороший выбор!")}}
    fun clearMessage(){_state.update{it.copy(message="")}}
    fun finish(onDone:(Int)->Unit)=viewModelScope.launch{val s=_state.value;if(s.cards.isEmpty()){val stars=starsFor(s.errors);award("priorities_${s.level}",rewardFor(s.level),stars);nextLevel(progress,"priorities",s.level);onDone(stars)}}
}

data class PiggyUiState(val level:Int=1,val collected:Int=0,val caught:Int=0,val temptations:Int=0,val processed:Int=0,val totalObjects:Int=40,val totalCoins:Int=35,val seconds:Int=40,val running:Boolean=false)
private fun piggyLevel(level:Int):PiggyUiState{val times=listOf(40,45,48,50,52,55,58,60,62,65);val targets=listOf(35,42,50,58,65,72,80,88,96,105);return PiggyUiState(level=level,totalObjects=100,totalCoins=targets[level-1],seconds=times[level-1])}
@HiltViewModel class PiggyBankViewModel @Inject constructor(private val saved:SavedStateHandle,private val award:AwardGameResult,private val progress:ProgressRepository):ViewModel(){private val _state=MutableStateFlow(PiggyUiState());val state:StateFlow<PiggyUiState> = _state;private var level=1
    init{viewModelScope.launch{level=selectedLevel(saved,progress,"piggy");_state.value=piggyLevel(level)}}
    fun start(){_state.value=piggyLevel(level).copy(running=true)}
    fun coin(value:Int){_state.update{if(!it.running)it else{val sum=it.collected+value;it.copy(collected=sum,caught=it.caught+1,processed=it.processed+1,running=sum<it.totalCoins)}}}
    fun temptation(){_state.update{if(!it.running)it else it.copy(processed=it.processed+1)}}
    fun missCoin(){_state.update{if(!it.running)it else it.copy(processed=it.processed+1)}}
    fun catchTemptation(){_state.update{if(!it.running)it else{val errors=it.temptations+1;it.copy(temptations=errors,processed=it.processed+1,running=errors<3)}}}
    fun tick(){_state.update{if(!it.running)it else it.copy(seconds=(it.seconds-1).coerceAtLeast(0),running=it.seconds>1)}}
    fun finish(onDone:(Int)->Unit)=viewModelScope.launch{val s=_state.value;val percent=(s.collected*100/s.totalCoins).coerceAtMost(100);val stars=RewardCalculator.piggyBank(percent,s.temptations).stars;award("piggy_$level",rewardFor(level),stars);nextLevel(progress,"piggy",level);onDone(stars)}
}

data class GoalUiState(val level:Int=1,val goal:String="Игрушка",val cost:Int=50,val saved:Int=0,val step:Int=0,val incomes:List<Int> = listOf(30),val expenses:List<Int> = listOf(10),val options:List<Int> = listOf(5,10,15,20),val selected:Int=0,val errors:Int=0,val message:String=""){val free:Int get()=if(step<incomes.size) incomes[step]-expenses[step] else 0;val preview:Int get()=(saved+selected).coerceAtMost(cost);val finalStep:Boolean get()=level==3||level==6||level==10}
private fun goalLevel(level:Int):GoalUiState{val income=listOf(30,35,40,30,35,50,40,55,60,80)[level-1];val expense=listOf(10,12,15,10,12,20,15,20,20,28)[level-1];val goal=when{level<=3->"Игрушка";level<=6->"Домик";else->"Игровой комплекс"};val cost=when{level<=3->50;level<=6->100;else->150};val base=listOf(0,15,25,0,35,70,0,35,75,100)[level-1];val free=income-expense;val needed=(cost-base).coerceAtMost(free);return GoalUiState(level,goal,cost,base,0,listOf(income),listOf(expense),(listOf(5,10,15,20)+needed).distinct().sorted())}
@HiltViewModel class GoalGameViewModel @Inject constructor(private val saved:SavedStateHandle,private val award:AwardGameResult,private val progress:ProgressRepository):ViewModel(){private val _state=MutableStateFlow(GoalUiState());val state:StateFlow<GoalUiState> = _state
    init{viewModelScope.launch{_state.value=goalLevel(selectedLevel(saved,progress,"goal"))}}
    fun select(amount:Int){_state.update{s->if(amount>s.free)s.copy(errors=s.errors+1,message="Свободно только ${s.free} монет: доход минус обязательные расходы.") else s.copy(selected=amount,message="Выбрано $amount монет. Теперь положи их в копилку.")}}
    fun confirm(onSuccess:()->Unit){var accepted=false;_state.update{s->val need=s.cost-s.saved;when{s.selected<=0->s.copy(message="Сначала выбери сумму.");s.finalStep&&s.selected!=need->s.copy(errors=s.errors+1,message="Чтобы завершить цель, нужно отложить ровно $need монет.");else->{accepted=true;s.copy(saved=(s.saved+s.selected).coerceAtMost(s.cost),selected=0,step=s.step+1,message="Отложено ${s.selected} монет")}}};if(accepted)onSuccess()}
    fun clearMessage(){_state.update{it.copy(message="")}}
    fun finish(onDone:(Int)->Unit)=viewModelScope.launch{val s=_state.value;if(s.step>=s.incomes.size){val stars=starsFor(s.errors);award("goal_${s.level}",rewardFor(s.level),stars);nextLevel(progress,"goal",s.level);onDone(stars)}}
}

data class ShopUiState(val level:Int=1,val budget:Int=100,val items:List<ShopItem> = emptyList(),val cart:List<ShopItem> = emptyList(),val message:String="",val errors:Int=0){val total:Int get()=cart.sumOf{it.price};val left:Int get()=(budget-total).coerceAtLeast(0);val canPay:Boolean get()=items.filter{it.required}.all{it in cart}}
private fun shopLevel(level:Int):ShopUiState{val budgets=listOf(100,100,110,120,130,140,150,160,180,200);val food=listOf(30,30,30,35,35,40,40,45,45,50);val water=listOf(0,15,15,20,20,20,20,20,25,25);val medicine=listOf(0,0,20,0,20,0,25,25,30,30);val rent=listOf(0,0,0,0,0,35,0,30,35,40);val items=mutableListOf(ShopItem("food",if(level%2==0)"Свежее мясо" else "Еда сытная",food[level-1],true,"🍗"));if(water[level-1]>0)items+=ShopItem("water","Вода питьевая",water[level-1],true,"💧");if(medicine[level-1]>0)items+=ShopItem("medicine","Лекарство",medicine[level-1],true,"💊");if(rent[level-1]>0)items+=ShopItem("rent","Аренда домика",rent[level-1],true,"🏠");items+=listOf(ShopItem("toy","Игрушка мишка",if(level>=4)25 else 20,false,"🧸"),ShopItem("ice","Мороженое",if(level>=4)15 else 10,false,"🍦"),ShopItem("book","Книжка сказок",if(level>=5)15 else 20,false,"📚"),ShopItem("ball","Мяч",if(level>=4)30 else 25,false,"⚽"));return ShopUiState(level,budgets[level-1],items)}
@HiltViewModel class ShopGameViewModel @Inject constructor(private val saved:SavedStateHandle,private val award:AwardGameResult,private val progress:ProgressRepository):ViewModel(){private val _state=MutableStateFlow(shopLevel(1));val state:StateFlow<ShopUiState> = _state
    init{viewModelScope.launch{_state.value=shopLevel(selectedLevel(saved,progress,"shop"))}}
    fun add(item:ShopItem){_state.update{s->when{item in s.cart->s.copy(message="Этот товар уже в корзине.");item.price>s.left->s.copy(message="Не хватает ${item.price-s.left} монет.",errors=s.errors+1);else->s.copy(cart=s.cart+item,message="${item.title} добавлен")}}}
    fun remove(item:ShopItem){_state.update{it.copy(cart=it.cart-item,message="Товар убран. Монеты вернулись в бюджет.")}}
    fun pay(onDone:(Int)->Unit)=viewModelScope.launch{val s=_state.value;if(!s.canPay){val missing=s.items.filter{it.required&&it !in s.cart}.joinToString{it.title};_state.update{it.copy(message="Подумай, без чего нельзя обойтись. В корзине пока нет: $missing.",errors=it.errors+1)}}else{val stars=starsFor(s.errors);award("shop_${s.level}",rewardFor(s.level),stars);nextLevel(progress,"shop",s.level);onDone(stars)}}
    fun clearMessage(){_state.update{it.copy(message="")}}
}

data class ChangeUiState(val task:Int=1,val itemName:String="Хлеб и молоко",val price:Int=35,val paid:Int=50,val available:List<Int> = listOf(50,20,20,10,10,5,5,2,2,1,1),val selected:List<Int> = emptyList(),val message:String="",val errors:Int=0){val expected:Int get()=(paid-price).coerceAtLeast(0);val total:Int get()=selected.sum()}
private fun changeLevel(level:Int):ChangeUiState{val prices=listOf(35,48,25,42,67,85,74,58,93,76);val paid=listOf(50,50,50,50,100,100,100,100,100,100);val names=listOf("Хлеб и молоко","Свежий багет","Яблочный сок","Сыр домашний","Набор печенья","Фруктовая корзина","Чай и сладости","Обед питомца","Шоколадный торт","Большой набор");return ChangeUiState(task=level,itemName=names[level-1],price=prices[level-1],paid=paid[level-1])}
@HiltViewModel class ChangeGameViewModel @Inject constructor(private val saved:SavedStateHandle,private val award:AwardGameResult,private val progress:ProgressRepository):ViewModel(){private val _state=MutableStateFlow(ChangeUiState());val state:StateFlow<ChangeUiState> = _state
    init{viewModelScope.launch{_state.value=changeLevel(selectedLevel(saved,progress,"change"))}}
    fun add(v:Int){_state.update{s->if(v !in s.available)s else s.copy(selected=s.selected+v,available=s.available-v,message="")}}
    fun remove(v:Int){_state.update{s->s.copy(selected=s.selected-v,available=s.available+v)}}
    fun check(onDone:(Int)->Unit)=viewModelScope.launch{val s=_state.value;if(s.total!=s.expected){_state.update{it.copy(errors=it.errors+1,message=if(s.total<s.expected)"Не хватает ${s.expected-s.total} монет. Сложи выбранные монеты и добавь недостающую сумму." else "Получилось ${s.total}, а сдача должна быть ${s.expected} монет. Убери лишние монеты.")}}else{val stars=starsFor(s.errors);award("change_${s.task}",rewardFor(s.task),stars);nextLevel(progress,"change",s.task);onDone(stars)}}
    fun clearMessage(){_state.update{it.copy(message="")}}
}
