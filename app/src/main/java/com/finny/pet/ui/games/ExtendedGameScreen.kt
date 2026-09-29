package com.finny.pet.ui.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finny.pet.ui.components.FeedbackDialog
import com.finny.pet.ui.components.FinnyButton
import com.finny.pet.ui.components.FinnyProgressTrack
import com.finny.pet.ui.components.ItemArtwork
import com.finny.pet.ui.components.MedicineArtwork
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class GameHeading(val icon:ImageVector,val title:String,val accent:Color)
private val headings=mapOf(
    "shopping_list" to GameHeading(Icons.Default.FormatListBulleted,"Список покупок",Color(0xFF4975D1)),
    "friend_week" to GameHeading(Icons.Default.DateRange,"Неделя друга",Color(0xFF43A779)),
    "expense_race" to GameHeading(Icons.Default.DirectionsRun,"Гонка расходов",Color(0xFFE46951)),
    "traffic" to GameHeading(Icons.Default.Traffic,"Светофор трат",Color(0xFFE59A31)),
    "compare_prices" to GameHeading(Icons.Default.PriceCheck,"Сравни цены",Color(0xFF348FB9)),
    "secret_box" to GameHeading(Icons.Default.Inventory2,"Секретный ящик",Color(0xFF8E64D2))
)

@Composable
fun ExtendedGameScreen(onBack:()->Unit,onDone:(Int)->Unit,vm:ExtendedGameViewModel=hiltViewModel()){
    val state by vm.state.collectAsState()
    val heading=headings[state.gameId]?:headings.getValue("shopping_list")
    val info=EducationalGameCatalog.get(state.gameId)
    state.hint?.let{FeedbackDialog("Давай разберёмся",it,vm::clearHint)}
    Box(Modifier.fillMaxSize().background(gameBackground(heading.accent))){
        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(bottom=30.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{
                PremiumGameHeader(heading.title,"Ошибок: ${state.mistakes} • выполни задание самостоятельно",state.level,heading.accent,heading.icon,onBack)
            }
            item{Box(Modifier.padding(horizontal=15.dp)){GameInstructionCard(info.instruction,heading.accent,"Задача")}}
            item{
                Box(Modifier.padding(horizontal=15.dp)){when(state.gameId){
                    "shopping_list"->ShoppingListGame(state.level,{vm.mistake(it)},{vm.complete(onDone)})
                    "friend_week"->FriendWeekGame(state.level,{vm.mistake(it)},{vm.complete(onDone)})
                    "expense_race"->ExpenseRaceGame(state.level,vm::recordMistake,{vm.complete(onDone)})
                    "traffic"->TrafficGame(state.level,{vm.mistake(it)},{vm.complete(onDone)})
                    "compare_prices"->ComparePricesGame(state.level,{vm.mistake(it)},{vm.complete(onDone)})
                    else->SecretBoxGame(state.level,{vm.mistake(it)},{vm.complete(onDone)})
                }}
            }
        }
    }
}

@Composable private fun MiniStat(icon:String,label:String,value:String,color:Color,modifier:Modifier=Modifier){
    GameStatCard(label,value,color,modifier,icon)
}

@Composable private fun FinnySpeech(text:String){
    FinnyCoachCard(text)
}

private data class ListedItem(val id:String,val icon:String,val name:String,val price:Int,val needed:Boolean)
private fun listLevel(level:Int):Pair<Int,List<ListedItem>>{
    val budget=listOf(60,65,70,75,80,85,90,95,100,110)[level-1]
    val all=listOf(ListedItem("food","🍗","Корм",20,true),ListedItem("water","💧","Вода",10,true),ListedItem("med","💊","Лекарство",15,true),ListedItem("rent","🏠","Домик",25,true),ListedItem("book","📚","Книга",10,true))
    val ids=when(level){1->setOf("food","water");2->setOf("food","med");3->setOf("rent","food","water");4->setOf("food","water","med");5->setOf("rent","food","med");6,7,8->setOf("rent","food","water","med");else->setOf("rent","food","water","med","book")}
    val required=all.filter{it.id in ids}
    val extras=listOf(ListedItem("toy","🐻","Игрушка",25,false),ListedItem("ice","🍦","Мороженое",15,false),ListedItem("ball","⚽","Мяч",20,false),ListedItem("console","🎮","Приставка",35,false))
    return budget to (required+extras).shuffled(Random(level))
}

@Composable private fun ShoppingListGame(level:Int,wrong:(String)->Unit,done:()->Unit){
    val data=remember(level){listLevel(level)};val selected=remember(level){mutableStateListOf<String>()};val total=data.second.filter{it.id in selected}.sumOf{it.price};val required=data.second.filter{it.needed}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){MiniStat("◎","Бюджет",data.first.toString(),Color(0xFFFFF0C8),Modifier.weight(1f));MiniStat("#","В корзине",total.toString(),Color(0xFFE6F0FF),Modifier.weight(1f));MiniStat("✓","Остаток",(data.first-total).toString(),Color(0xFFDFF7EB),Modifier.weight(1f))}
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(23.dp),color=Color(0xFFFFF8E9),border=BorderStroke(2.dp,Color(0xFFFFC65D)),shadowElevation=6.dp){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){Text("📌 СПИСОК ФИННИ",fontWeight=FontWeight.Black,color=Color(0xFF775312));required.forEach{Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)){Text("✓",fontWeight=FontWeight.Black,color=Color(0xFF31865B));ItemArtwork(it.id,it.icon,24.dp,"capsule");Text("${it.name} — ${it.price} монет",fontWeight=FontWeight.Bold,color=Color(0xFF354563))}}}}
        Text("Витрина магазина",fontSize=19.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65))
        LazyRow(horizontalArrangement=Arrangement.spacedBy(9.dp)){items(data.second){item->val checked=item.id in selected;Surface(Modifier.width(124.dp).heightIn(min=150.dp).clickable{if(checked)selected.remove(item.id)else if(total+item.price<=data.first)selected.add(item.id)else wrong("На ${item.name.lowercase()} не хватает ${total+item.price-data.first} монет. Убери необязательную покупку.")},shape=RoundedCornerShape(19.dp),color=if(checked)Color(0xFFDFF7EB)else Color.White,border=BorderStroke(if(checked)3.dp else 1.dp,if(checked)Color(0xFF36A36F)else Color(0xFFD6DFEC)),shadowElevation=4.dp){Column(Modifier.padding(11.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(3.dp)){ItemArtwork(item.id,item.icon,52.dp,"bottle");Text(item.name,fontSize=14.sp,fontWeight=FontWeight.Bold,maxLines=2);Text("${item.price} мон.",fontWeight=FontWeight.Black,color=Color(0xFFB86E00));Text(if(checked)"В корзине" else "Добавить",fontSize=12.sp,fontWeight=FontWeight.Bold,color=if(checked)Color(0xFF198655)else Color(0xFF52617B))}}}}
        FinnySpeech("Сверяй корзину со списком. Сначала — всё необходимое, а желания только на остаток.")
        FinnyButton("Проверить покупки",{val chosen=selected.toSet();val need=required.map{it.id}.toSet();when{!chosen.containsAll(need)->wrong("В корзине не хватает: ${required.filter{it.id !in chosen}.joinToString{it.name}}. Найди эти товары в списке.");chosen.any{id->data.second.first{it.id==id}.needed.not()}->wrong("В задании нужны только товары из списка. Убери необязательные покупки и сохрани остаток.");else->done()}},modifier=Modifier.fillMaxWidth())
    }
}

@Composable private fun FriendWeekGame(level:Int,wrong:(String)->Unit,done:()->Unit){
    val budget=listOf(70,75,80,85,90,95,100,105,110,120)[level-1];val minimum=listOf(8,8,9,9,10,10,11,11,12,12)[level-1];val reserveTarget=listOf(14,19,17,22,20,25,23,28,26,36)[level-1];val days=remember(level){mutableStateListOf(0,0,0,0,0,0,0)};val spent=days.sum();val reserve=budget-spent
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){MiniStat("◎","Неделя",budget.toString(),Color(0xFFFFF0C8),Modifier.weight(1f));MiniStat("+","Распределено",spent.toString(),Color(0xFFE6F0FF),Modifier.weight(1f));MiniStat("✓","Резерв",reserve.toString(),Color(0xFFDFF7EB),Modifier.weight(1f))}
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(25.dp),color=Color.White,shadowElevation=7.dp){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("КАЛЕНДАРЬ НЕДЕЛИ",fontWeight=FontWeight.Black,color=Color(0xFF2C456C));Text("Выдай каждому дню не меньше $minimum монет и сохрани в резерве хотя бы $reserveTarget.",fontSize=14.sp,lineHeight=19.sp,color=Color(0xFF68748A));FinnyProgressTrack(reserve.toFloat()/budget,Modifier.fillMaxWidth(),color=if(reserve>=reserveTarget)Color(0xFF43A779)else Color(0xFFE59A31),height=9.dp);Text("Резерв: $reserve из нужных $reserveTarget монет",fontSize=13.sp,fontWeight=FontWeight.Bold,color=Color(0xFF52617B));LazyRow(horizontalArrangement=Arrangement.spacedBy(9.dp)){items(days.size){i->Surface(Modifier.width(132.dp),shape=RoundedCornerShape(18.dp),color=if(days[i]>=minimum)Color(0xFFE1F7EB)else Color(0xFFF2F5FA),border=BorderStroke(2.dp,if(days[i]>=minimum)Color(0xFF48AE7D)else Color(0xFFD3DCE9))){Column(Modifier.padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(4.dp)){Text(listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс")[i],fontWeight=FontWeight.Black);Text("${days[i]} монет",fontSize=19.sp,fontWeight=FontWeight.Black,maxLines=1);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){IconButton(onClick={if(days[i]>0)days[i]--},enabled=days[i]>0,modifier=Modifier.size(36.dp)){Icon(Icons.Default.Remove,"Уменьшить",Modifier.size(20.dp))};IconButton(onClick={if(reserve>0)days[i]++},enabled=reserve>0,modifier=Modifier.size(36.dp)){Icon(Icons.Default.Add,"Добавить",Modifier.size(20.dp))};IconButton(onClick={days[i]=0},enabled=days[i]>0,modifier=Modifier.size(36.dp)){Icon(Icons.Default.RestartAlt,"Обнулить день",Modifier.size(19.dp))}};Text(if(days[i]>=minimum)"День обеспечен" else "Нужно ещё ${minimum-days[i]}",fontSize=11.sp,fontWeight=FontWeight.Bold,color=if(days[i]>=minimum)Color(0xFF23835A)else Color(0xFF6C7890),maxLines=1)}}}}}}
        FinnySpeech("Планируй понемногу на каждый день и не трать резерв — он спасает от неожиданностей.")
        FinnyButton("Проверить план недели",{when{days.any{it<minimum}->wrong("Не на каждый день хватает денег. Добавь минимум $minimum монет в дни с серой карточкой.");reserve<reserveTarget->wrong("В резерве осталось $reserve, а нужно сохранить $reserveTarget. Уменьши расходы по дням.");else->done()}},modifier=Modifier.fillMaxWidth())
    }
}

private data class RaceItem(val icon:String,val name:String,val price:Int)
private val raceItems=listOf(RaceItem("💧","Вода",5),RaceItem("🍞","Хлеб",8),RaceItem("🥛","Молоко",10),RaceItem("🧀","Сыр",12),RaceItem("🍎","Яблоки",15),RaceItem("🐟","Рыба",20),RaceItem("🎂","Торт",25),RaceItem("🎮","Приставка",35))
private data class RaceRunner(val id:Int,val item:RaceItem,val lane:Int,val x:Float)

@Composable private fun ExpenseRaceGame(level:Int,recordMistake:()->Unit,done:()->Unit){
    val secondsMax=listOf(20,25,30,35,40,45,50,55,60,65)[level-1];val objectCount=listOf(12,14,16,18,20,22,24,26,28,30)[level-1];val limit=listOf(40,45,50,55,60,65,70,75,80,90)[level-1]
    var seconds by rememberSaveable(level){mutableIntStateOf(secondsMax)}
    var processed by rememberSaveable(level){mutableIntStateOf(0)}
    var spawned by rememberSaveable(level){mutableIntStateOf(0)}
    var basket by rememberSaveable(level){mutableIntStateOf(0)}
    var lane by rememberSaveable(level){mutableIntStateOf(1)}
    var runId by rememberSaveable(level){mutableIntStateOf(0)}
    var running by rememberSaveable(level){mutableStateOf(true)}
    var helper by rememberSaveable(level){mutableStateOf("Веди тележку к полезным покупкам и объезжай то, что превысит лимит.")}
    val runners=remember(level,runId){mutableStateListOf<RaceRunner>()}

    LaunchedEffect(level,runId){
        seconds=secondsMax;processed=0;spawned=0;basket=0;lane=1;running=true
        val frameMs=16L
        val spawnEvery=((secondsMax*1000L)/(objectCount+1)).coerceAtLeast(650L)
        var spawnClock=spawnEvery-350L
        while(running&&seconds>0){
            delay(frameMs);spawnClock+=frameMs
            if(spawned<objectCount&&spawnClock>=spawnEvery){
                spawnClock=0L
                val ordinal=spawned
                runners+=RaceRunner(ordinal,raceItems[(ordinal*3+level)%raceItems.size],(ordinal*5+level)%3,1.08f)
                spawned++
            }
            val next=ArrayList<RaceRunner>(runners.size)
            runners.forEach{o->
                val moved=o.copy(x=o.x-(0.0062f+level*.00028f))
                val collided=moved.lane==lane&&moved.x in .10f..0.25f
                when{
                    collided->{
                        if(basket+moved.item.price<=limit){basket+=moved.item.price;helper="${moved.item.name} в корзине: +${moved.item.price} монет."}
                        else{helper="Стоп! ${moved.item.name} превысит лимит. Такой товар нужно объехать.";recordMistake()}
                        processed++
                    }
                    moved.x<-.15f->processed++
                    else->next+=moved
                }
            }
            runners.clear();runners.addAll(next)
        }
    }
    LaunchedEffect(level,runId){
        while(running&&seconds>0){delay(1000);seconds--}
        if(seconds<=0){running=false;helper="Финиш! В корзине $basket из $limit монет, лимит не превышен."}
    }
    fun moveLane(delta:Int){lane=(lane+delta).coerceIn(0,2)}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){MiniStat("⏱","Время","$seconds с",Color(0xFFFFE4D9),Modifier.weight(1f));MiniStat("🛒","Корзина","$basket/$limit",Color(0xFFE6F0FF),Modifier.weight(1f));MiniStat("🏁","Товары","$processed/$objectCount",Color(0xFFDFF7EB),Modifier.weight(1f))}
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),color=Color(0xFF263B61),shadowElevation=8.dp){
            Column(Modifier.padding(10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                BoxWithConstraints(Modifier.fillMaxWidth().height(226.dp).pointerInput(running){detectVerticalDragGestures{_,amount->if(running&&kotlin.math.abs(amount)>8f)moveLane(if(amount>0)1 else -1)}}){
                    repeat(3){i->Box(Modifier.align(Alignment.CenterStart).offset(y=((i-1)*66).dp).fillMaxWidth().height(52.dp).background(if(i%2==0)Color(0xFF3A5685)else Color(0xFF304A74),RoundedCornerShape(28.dp)));Text("${i+1}",Modifier.align(Alignment.CenterEnd).offset(x=(-7).dp,y=((i-1)*66).dp),color=Color.White.copy(.42f),fontWeight=FontWeight.Black)}
                    runners.forEach{o->
                        val danger=basket+o.item.price>limit
                        Surface(Modifier.align(Alignment.CenterStart).offset(x=(maxWidth-58.dp)*o.x,y=((o.lane-1)*66).dp).size(56.dp),shape=CircleShape,color=if(danger)Color(0xFFFFDDD8)else Color.White,border=BorderStroke(if(danger)3.dp else 1.dp,if(danger)Color(0xFFE95656)else Color.White),shadowElevation=7.dp){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){ItemArtwork(if(o.item.name=="Лекарство")"medicine" else "",o.item.icon,27.dp,"capsule");Text("${o.item.price}",fontSize=13.sp,fontWeight=FontWeight.Black,color=if(danger)Color(0xFFC93232)else Color(0xFFB86E00))}}
                    }
                    Surface(Modifier.align(Alignment.CenterStart).offset(x=9.dp,y=((lane-1)*66).dp).size(62.dp),shape=RoundedCornerShape(19.dp),color=Color(0xFFFFC557),border=BorderStroke(3.dp,Color.White),shadowElevation=9.dp){Box(contentAlignment=Alignment.Center){Text("🛒",fontSize=32.sp)}}
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){
                    Button(onClick={moveLane(-1)},enabled=running&&lane>0,modifier=Modifier.weight(1f).height(46.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4A72D8))){Text("▲ Выше",fontWeight=FontWeight.Black)}
                    Button(onClick={moveLane(1)},enabled=running&&lane<2,modifier=Modifier.weight(1f).height(46.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF4A72D8))){Text("▼ Ниже",fontWeight=FontWeight.Black)}
                }
            }
        }
        FinnyCoachCard(text=helper,modifier=Modifier.fillMaxWidth())
        if(!running){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){OutlinedButton(onClick={runId++},modifier=Modifier.weight(1f).height(52.dp),shape=RoundedCornerShape(17.dp)){Text("Ещё раз",fontWeight=FontWeight.Black)};FinnyButton("Завершить",done,modifier=Modifier.weight(1f))}}
    }
}

private data class TrafficCard(val icon:String,val name:String,val zone:Int,val explanation:String)
private val trafficCards=listOf(
    TrafficCard("🍗","Корм",0,"Корм нужен питомцу каждый день — это обязательная покупка."),TrafficCard("💧","Вода",0,"Без воды нельзя обойтись — покупаем сейчас."),TrafficCard("💊","Лекарство",0,"Лечение важно для здоровья питомца."),TrafficCard("🏠","Жильё",0,"Безопасный дом — обязательный расход."),
    TrafficCard("🐻","Игрушка",1,"Игрушка радует, но её можно купить позже."),TrafficCard("📚","Книга",1,"Полезное желание: запланируй после обязательных расходов."),TrafficCard("⚽","Мяч",1,"Мяч можно купить, если после важного остались деньги."),
    TrafficCard("🎮","Вторая приставка",2,"Это дорогая повторная покупка — сейчас лучше отказаться."),TrafficCard("🍬","Лишние сладости",2,"Импульсивную покупку лучше пропустить."),TrafficCard("✨","Случайный сувенир",2,"Если вещь не нужна и не запланирована, деньги лучше сохранить.")
)

@Composable private fun TrafficGame(level:Int,wrong:(String)->Unit,done:()->Unit){
    val total=listOf(6,7,8,9,10,11,12,13,14,16)[level-1];val secondsMax=listOf(15,18,20,22,25,28,30,33,36,40)[level-1];val deck=remember(level){List(total){trafficCards[(it*3+level)%trafficCards.size]}};var index by rememberSaveable(level){mutableIntStateOf(0)};var seconds by rememberSaveable(level){mutableIntStateOf(secondsMax)}
    LaunchedEffect(seconds,index){if(seconds>0&&index<deck.size){delay(1000);seconds--}}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){MiniStat("⏱","Время","$seconds с",Color(0xFFFFEBC6),Modifier.weight(1f));MiniStat("✓","Разобрано","$index/$total",Color(0xFFDFF7EB),Modifier.weight(1f))}
        if(index<deck.size&&seconds>0){val card=deck[index];Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),color=Color.White,shadowElevation=8.dp){Column(Modifier.padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){ItemArtwork(if(card.name=="Лекарство")"medicine" else "",card.icon,94.dp,"capsule");Text(card.name,fontSize=25.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text("Куда поставить эту трату?",color=Color(0xFF68748A))}}
            listOf(Triple(Color(0xFF42AC72),"Сейчас","Нужно"),Triple(Color(0xFFF0B43F),"Потом","Желание"),Triple(Color(0xFFE45F67),"Стоп","Не брать")).forEachIndexed{i,b->Button(onClick={if(card.zone==i){index++;if(index==deck.size)done()}else wrong(card.explanation)},modifier=Modifier.fillMaxWidth().height(58.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=b.first)){Text("${listOf("🟢","🟡","🔴")[i]} ${b.second} • ${b.third}",fontSize=17.sp,fontWeight=FontWeight.Black)}}
        }else if(seconds==0){FinnySpeech("Время закончилось. Ничего страшного: повтори и сначала ищи обязательные покупки.");FinnyButton("Повторить раунд",{seconds=secondsMax;index=0},modifier=Modifier.fillMaxWidth())}
    }
}

private data class Offer(val store:String,val icon:String,val price:Int,val count:Int){val unit:Float get()=price.toFloat()/count}
private fun offersFor(level:Int,round:Int):List<Offer>{val base=18+level*2+round*3;return if((level+round)%2==0)listOf(Offer("Магазин А","🏪",base,1),Offer("Магазин Б","🛒",base+8,2)) else listOf(Offer("Магазин А","🏪",base+5,1),Offer("Магазин Б","🛒",base,1))}

@Composable private fun ComparePricesGame(level:Int,wrong:(String)->Unit,done:()->Unit){
    val rounds=(2+level/3).coerceAtMost(5);var round by rememberSaveable(level){mutableIntStateOf(0)};var saved by rememberSaveable(level){mutableIntStateOf(0)};val offers=offersFor(level,round);val best=offers.minBy{it.unit}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){MiniStat("#","Сравнение","${round+1}/$rounds",Color(0xFFE6F0FF),Modifier.weight(1f));MiniStat("✓","Сэкономлено",saved.toString(),Color(0xFFDFF7EB),Modifier.weight(1f))}
        Text(if(offers.any{it.count>1})"Сравни цену за одну упаковку" else "Выбери магазин с меньшей ценой",fontSize=19.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65))
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){offers.forEach{offer->Surface(Modifier.weight(1f).clickable{if(offer==best){val worst=offers.maxOf{it.unit};saved+=(worst-best.unit).toInt();if(round+1>=rounds)done()else round++}else wrong(if(offer.count>1)"Посчитай цену одной упаковки: ${offer.price} ÷ ${offer.count}. У другого магазина единица товара дешевле." else "${offer.store} дороже. Сравни цены и выбери меньшее число.")},shape=RoundedCornerShape(24.dp),color=Color.White,border=BorderStroke(2.dp,Color(0xFFB9CEE5)),shadowElevation=7.dp){Column(Modifier.padding(15.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(offer.icon,fontSize=55.sp);Text(offer.store,fontWeight=FontWeight.Black,color=Color(0xFF263A65),textAlign=TextAlign.Center);Text("${offer.price} мон.",fontSize=24.sp,fontWeight=FontWeight.Black,color=Color(0xFF2988B5));Text(if(offer.count==1)"1 упаковка" else "${offer.count} упаковки",color=Color(0xFF68748A));if(offer.count>1)Text("≈ ${"%.1f".format(offer.unit)} за одну",fontSize=12.sp,color=Color(0xFF68748A))}}}}
        FinnySpeech("Смотри не только на большую цифру на ценнике: сравнивай одинаковое количество товара.")
    }
}

@Composable private fun SecretBoxGame(level:Int,wrong:(String)->Unit,done:()->Unit){
    val incomes=listOf(30,35,40,45,50,55,60,65,70,80);val choices=listOf(listOf(5,10,15),listOf(8,12,18),listOf(10,15,20),listOf(10,15,20),listOf(12,18,25),listOf(15,20,25),listOf(15,20,30),listOf(18,22,30),listOf(20,25,30),listOf(20,28,35))[level-1];val answer=listOf(10,12,15,15,18,20,20,22,25,28)[level-1];val remain=listOf(20,23,25,30,32,35,40,43,45,52)[level-1];var chosen by rememberSaveable(level){mutableIntStateOf(0)};var event by rememberSaveable(level){mutableStateOf(false)}
    Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){MiniStat("💰","Доход",incomes[level-1].toString(),Color(0xFFFFF0C8),Modifier.weight(1f));MiniStat("🏦","В резерв",if(chosen==0)"?" else chosen.toString(),Color(0xFFE9E3FF),Modifier.weight(1f));MiniStat("✓","Останется",if(chosen==0)"?" else (incomes[level-1]-chosen).toString(),Color(0xFFDFF7EB),Modifier.weight(1f))}
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),color=if(event)Color(0xFFFFE9D7)else Color(0xFFEDE7FF),shadowElevation=8.dp){Column(Modifier.padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally){if(event)MedicineArtwork("syringe",Modifier.size(96.dp)) else Text("🎁",fontSize=88.sp);Text(if(event)"Неожиданный расход!" else "Секретный ящик",fontSize=24.sp,fontWeight=FontWeight.Black,color=Color(0xFF3E356E));Text(if(event)"Финни понадобилось лекарство. Резерв помог оплатить его без долгов." else "Выбери разумную сумму в резерв: хватит и на сегодня, и на неожиданность.",textAlign=TextAlign.Center,color=Color(0xFF625D77))}}
        if(!event){choices.forEach{amount->OutlinedButton(onClick={if(amount==answer){chosen=amount;event=true}else wrong(if(amount<answer)"Сумма слишком мала для надёжного резерва. Попробуй оставить $answer монет." else "Если отложить $amount, на текущие нужды останется мало. Безопасный выбор — $answer монет.")},modifier=Modifier.fillMaxWidth().height(54.dp),shape=RoundedCornerShape(18.dp)){Text("Отложить $amount • останется ${incomes[level-1]-amount}",fontSize=16.sp,fontWeight=FontWeight.Bold)}}}else{FinnySpeech("Отлично! Ты сохранил $chosen монет, а на текущие нужды осталось $remain. Резерв защищает план.");FinnyButton("Помочь Финни",done,modifier=Modifier.fillMaxWidth())}
    }
}
