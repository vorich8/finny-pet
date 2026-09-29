package com.finny.pet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finny.pet.R
import com.finny.pet.audio.FinnyAudio
import com.finny.pet.audio.FinnySfx
import com.finny.pet.navigation.Routes
import com.finny.pet.domain.GameEconomy
import com.finny.pet.ui.components.*
import com.finny.pet.ui.games.EducationalGameCatalog
import com.finny.pet.ui.games.EducationalGameInfo
import com.finny.pet.ui.games.ArcadeHallPanel

data class BuildingPin(val id:String,val icon:ImageVector,val title:String,val subtitle:String,val x:Float,val y:Float,val level:Int)

private fun workGameIcon(id:String):ImageVector=when(id){
    "scales"->Icons.Default.Balance;"priorities"->Icons.Default.FilterAlt;"shopping_list"->Icons.Default.FormatListBulleted
    "friend_week"->Icons.Default.DateRange;"piggy"->Icons.Default.Savings;"goal"->Icons.Default.Flag
    "secret_box"->Icons.Default.Inventory2;"shop"->Icons.Default.ShoppingCart;"change"->Icons.Default.Calculate
    "expense_race"->Icons.Default.DirectionsRun;"traffic"->Icons.Default.Traffic;else->Icons.Default.PriceCheck
}

@Composable fun CityMapScreen(level:Int,period:Int,coins:Int,onBuilding:(String)->Unit){
    val pins=listOf(
        BuildingPin("home",Icons.Default.Home,"Дом","Здесь живёт Финни",.20f,.22f,1),
        BuildingPin("bank",Icons.Default.AccountBalance,"Банк","Копи и планируй",.57f,.20f,1),
        BuildingPin("work",Icons.Default.Work,"Работа","Зарабатывай рубли",.76f,.36f,1),
        BuildingPin("food",Icons.Default.ShoppingBasket,"Продукты","Еда и забота",.16f,.48f,1),
        BuildingPin("arcade",Icons.Default.SportsEsports,"Игровой центр","Задания на счёт",.70f,.60f,2),
        BuildingPin("mall",Icons.Default.Store,"ТРЦ","Покупай по плану",.28f,.73f,1)
    )
    var travelling by remember{mutableStateOf<BuildingPin?>(null)}
    var locked by remember{mutableStateOf<BuildingPin?>(null)}
    val runner=remember{Animatable(-150f)}
    LaunchedEffect(travelling?.id){
        val destination=travelling ?: return@LaunchedEffect
        runner.snapTo(-150f)
        runner.animateTo(150f,tween(850,easing=FastOutSlowInEasing))
        onBuilding(destination.id)
    }
    locked?.let{pin->FeedbackDialog("Пока закрыто",if(pin.id=="arcade")"Игровой центр откроется после завершения первого дня." else "${pin.title} откроется на уровне ${pin.level}.",{locked=null},FinnySfx.WARNING)}
    val motion=rememberInfiniteTransition(label="mapMotion")
    val bob by motion.animateFloat(-5f,5f,infiniteRepeatable(tween(1100,easing=EaseInOutSine),RepeatMode.Reverse),label="pinBob")
    val pulse by motion.animateFloat(.96f,1.07f,infiniteRepeatable(tween(850,easing=EaseInOutSine),RepeatMode.Reverse),label="pinPulse")
    BoxWithConstraints(Modifier.fillMaxSize()){
        Image(painterResource(R.drawable.city_map_background_v2),"Карта города Финни",Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxWidth().height(66.dp).background(Brush.verticalGradient(listOf(Color(0xB8153C72),Color.Transparent))))
        Surface(Modifier.align(Alignment.TopCenter).padding(top=7.dp),shape=CircleShape,color=Color(0xE6203F73),shadowElevation=6.dp){Row(Modifier.padding(start=15.dp,end=7.dp,top=6.dp,bottom=6.dp),verticalAlignment=Alignment.CenterVertically){Text("Город Финни",color=Color.White,fontWeight=FontWeight.ExtraBold,fontSize=17.sp);Spacer(Modifier.width(10.dp));CoinCounter(coins)}}
        pins.forEach{pin->
            val unlocked=if(pin.id=="arcade")period>=2 else level>=pin.level
            Column(Modifier.offset(maxWidth*pin.x-48.dp,maxHeight*pin.y-30.dp).width(96.dp).clickable(enabled=travelling==null){if(unlocked){FinnyAudio.play(FinnySfx.NAVIGATE);travelling=pin}else locked=pin},horizontalAlignment=Alignment.CenterHorizontally){
                Surface(Modifier.size(54.dp).graphicsLayer{translationY=if(unlocked)bob else 0f;scaleX=if(unlocked)pulse else 1f;scaleY=if(unlocked)pulse else 1f},shape=CircleShape,color=if(unlocked)Color.White else Color(0xFFE7EAF0),border=BorderStroke(2.dp,if(unlocked)Color(0xFFFFD34E) else Color.White),shadowElevation=10.dp){
                    Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Icon(if(unlocked)pin.icon else Icons.Default.Lock,if(unlocked)pin.title else "Закрыто",Modifier.size(28.dp),tint=if(unlocked)Color(0xFF315EBA) else Color(0xFF7D8798))}
                }
                Surface(shape=CircleShape,color=Color(0xE51B3769),shadowElevation=3.dp){Text(pin.title,Modifier.padding(horizontal=7.dp,vertical=3.dp),fontSize=10.sp,fontWeight=FontWeight.ExtraBold,color=Color.White,maxLines=1)}
            }
        }
        if(travelling!=null)Box(Modifier.fillMaxSize().background(Color(0xCFF4F8FF)),contentAlignment=Alignment.Center){
            Surface(Modifier.padding(28.dp).fillMaxWidth(),shape=RoundedCornerShape(30.dp),color=Color.White,shadowElevation=18.dp){Column(Modifier.padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)){
                Text("В путь!",fontSize=27.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65))
                Box(Modifier.fillMaxWidth().height(116.dp),contentAlignment=Alignment.Center){Surface(Modifier.graphicsLayer{translationX=runner.value}.size(76.dp),shape=CircleShape,color=Color(0xFFE8F0FF),border=BorderStroke(3.dp,Color.White),shadowElevation=8.dp){Box(contentAlignment=Alignment.Center){Icon(Icons.Default.DirectionsRun,null,Modifier.size(42.dp),tint=Color(0xFF456DDB))}}}
                Text("Питомец бежит в «${travelling?.title}»",fontSize=16.sp,fontWeight=FontWeight.Bold,color=Color(0xFF4F5E77))
                LinearProgressIndicator(Modifier.fillMaxWidth(),color=Color(0xFF4B72DE),trackColor=Color(0xFFE3EAF8))
            }}
        }
    }
}

@Composable fun BuildingScreen(id:String,onBack:()->Unit,navigate:(String)->Unit,vm:CityStoreViewModel=hiltViewModel(),skinVm:SkinShopViewModel=hiltViewModel(),homeVm:HomeViewModel=hiltViewModel()){
    val state by vm.state.collectAsState()
    val skinState by skinVm.state.collectAsState()
    val homeState by homeVm.state.collectAsState()
    var section by rememberSaveable(id){mutableIntStateOf(0)}
    val info=when(id){"home"->Triple("Дом Финни",Icons.Default.Home,Color(0xFF70B9F3));"food"->Triple("Магазин продуктов",Icons.Default.ShoppingBasket,Color(0xFFFFA84D));"work"->Triple("Работа",Icons.Default.Work,Color(0xFF6F91E8));"arcade"->Triple("Игровой центр",Icons.Default.SportsEsports,Color(0xFF9A70DC));"mall"->Triple("Торговый центр",Icons.Default.Store,Color(0xFFFF7C9F));else->Triple("Банк",Icons.Default.AccountBalance,Color(0xFF5BC49B))}
    val background=when(id){"home"->R.drawable.room_background;"food"->R.drawable.interior_food;"work"->R.drawable.interior_work;"arcade"->R.drawable.interior_arcade;"mall"->R.drawable.interior_mall;else->R.drawable.interior_bank}
    state.message?.let{FeedbackDialog(if(it.contains("не хватает",true))"Рублей пока не хватает" else "Покупка готова",it,vm::clear,if(it.contains("не хватает",true))FinnySfx.WARNING else FinnySfx.COIN)}
    skinState.message?.let{FeedbackDialog(if(it.contains("не хватает",true))"Рублей пока не хватает" else "Гардероб",it,skinVm::clear,if(it.contains("не хватает",true))FinnySfx.WARNING else FinnySfx.COIN)}
    state.pending?.let{g->AlertDialog(onDismissRequest=vm::clear,title={Text("Оставить на важное?")},text={Text("После покупки останется меньше 20 ₽. Можно сохранить их на еду, воду или цель.")},dismissButton={TextButton(onClick=vm::clear){Text("Оставить на важное")}},confirmButton={Button(onClick={vm.buy(g,true)}){Text("Всё равно купить")}})}
    skinState.pending?.let{g->AlertDialog(onDismissRequest=skinVm::clear,title={Text("Оставить на важное?")},text={Text("Похоже, после покупки останется мало денег. Оставим немного на еду, воду и лекарства?")},dismissButton={TextButton(onClick=skinVm::clear){Text("Оставить на важное")}},confirmButton={Button(onClick={skinVm.buy(g,true)}){Text("Всё равно купить")}})}
    Box(Modifier.fillMaxSize()){
        Image(painterResource(background),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x7710274C),Color.Transparent,Color(0xBB10274C)))))
        Column(Modifier.fillMaxSize()){
            Surface(color=Color.White,shape=RoundedCornerShape(bottomStart=28.dp,bottomEnd=28.dp),shadowElevation=8.dp){Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Назад")};Surface(color=info.third.copy(.22f),shape=CircleShape){Icon(info.second,null,Modifier.padding(10.dp).size(25.dp),tint=info.third)};Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(info.first,fontSize=21.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF263A65),maxLines=1,overflow=TextOverflow.Ellipsis);Text(when(id){"work"->"12 заданий • по 10 уровней";"bank"->"Недельная цель и накопления";"food","mall"->"Покупки без игровых наград";"home"->"Отдых, питомец и рюкзак";else->"Игровые тренировки"},fontSize=12.sp,color=Color(0xFF667085),maxLines=1,overflow=TextOverflow.Ellipsis)}}}
            if(id=="mall"){
                Surface(Modifier.fillMaxWidth().padding(12.dp),shape=RoundedCornerShape(22.dp),color=Color.White,shadowElevation=7.dp){Row(Modifier.fillMaxWidth().padding(7.dp),horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(section==0,{section=0},{Text("Покупки")},Modifier.weight(1f),leadingIcon={Icon(Icons.Default.ShoppingBag,null,Modifier.size(19.dp))});FilterChip(section==1,{section=1},{Text("Аксессуары")},Modifier.weight(1f),leadingIcon={Icon(Icons.Default.Checkroom,null,Modifier.size(19.dp))})}}
            }
            when{
                id=="home"->ActionInterior("Дом — место отдыха и заботы. Здесь нет платных заданий.",listOf(("🎒" to "Открыть рюкзак") to Routes.Inventory,("💼" to "Идти на Работу") to Routes.building("work")),navigate)
                id=="work"->WorkGamesPanel(navigate)
                id=="bank"->GoalsHub()
                id=="food"->StoreInterior(foodGoods(homeState.period),vm::buy)
                id=="mall"&&section==0->StoreInterior(wantGoods,vm::buy)
                id=="mall"&&section==1->SkinStore(homeState.level,skinState.owned,skinVm::buy,skinVm::equip)
                id=="arcade"->ArcadeHallPanel(navigate)
                else->WorkGamesPanel(navigate)
            }
        }
    }
}

@Composable
private fun WorkGamesPanel(navigate:(String)->Unit,vm:WorkProgressViewModel=hiltViewModel()) {
    val progress by vm.state.collectAsState()
    var selected by remember { mutableStateOf<EducationalGameInfo?>(null) }
    val sections=EducationalGameCatalog.all.groupBy { it.category }

    selected?.let { game ->
        val gameProgress=progress.firstOrNull { it.id==game.id } ?: WorkGameProgress(game.id)
        AlertDialog(
            onDismissRequest={selected=null},
            containerColor=Color(0xFFF8FAFF),
            shape=RoundedCornerShape(30.dp),
            title={Row(verticalAlignment=Alignment.CenterVertically){Surface(Modifier.size(48.dp),shape=RoundedCornerShape(16.dp),color=Color(0xFFE4ECFF)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Icon(workGameIcon(game.id),null,Modifier.size(28.dp),tint=Color(0xFF365FC4))}};Spacer(Modifier.width(11.dp));Column{Text(game.title,fontSize=21.sp,fontWeight=FontWeight.Black,color=Color(0xFF20365F));Text("Учебная мини-игра",fontSize=13.sp,fontWeight=FontWeight.Bold,color=Color(0xFF667895))}}},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=Color.White,border=BorderStroke(1.dp,Color(0xFFD9E4F4))){Row(Modifier.padding(13.dp),verticalAlignment=Alignment.Top,horizontalArrangement=Arrangement.spacedBy(9.dp)){Icon(Icons.Default.Lightbulb,null,Modifier.size(22.dp),tint=Color(0xFFE59621));Text(game.instruction,Modifier.weight(1f),fontSize=15.sp,lineHeight=21.sp,fontWeight=FontWeight.Medium,color=Color(0xFF3F506D))}}
                    Text("Выбери уровень",fontSize=17.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF263A65))
                    for(row in 0..2) {
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                            for(column in 1..4) {
                                val level=row*4+column
                                if(level<=10) {
                                    val open=level<=gameProgress.level
                                    val done=level<=gameProgress.completed
                                    OutlinedButton(
                                        onClick={selected=null;navigate(Routes.educationalGame(game.id,level))},
                                        enabled=open,
                                        modifier=Modifier.weight(1f).heightIn(min=48.dp),
                                        contentPadding=PaddingValues(0.dp)
                                    ){if(done)Icon(Icons.Default.CheckCircle,null,Modifier.size(17.dp));Text(if(done)" $level" else if(open)"$level" else "—",fontWeight=FontWeight.Bold)}
                                } else Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    Text("Пройденный уровень можно повторить для тренировки. Повторная награда не начисляется.",fontSize=14.sp,lineHeight=19.sp,color=Color(0xFF596A84))
                }
            },
            confirmButton={TextButton(onClick={selected=null},modifier=Modifier.heightIn(min=48.dp)){Text("Закрыть",fontWeight=FontWeight.Bold)}}
        )
    }

    LazyColumn(Modifier.fillMaxSize().padding(horizontal=14.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=28.dp)) {
        item {
            Surface(color=Color(0xEE203F73),shape=RoundedCornerShape(24.dp),shadowElevation=9.dp) {
                Column(Modifier.padding(16.dp)) {
                    Text("Финансовая академия",color=Color.White,fontSize=22.sp,fontWeight=FontWeight.Black)
                    Text("Выбери задание и уровень. За первое прохождение — рубли и звёзды; повтор помогает улучшить результат.",color=Color(0xFFDDE8FF),fontSize=14.sp)
                }
            }
        }
        sections.forEach { section ->
            item { Text(section.key,color=Color.White,fontSize=19.sp,fontWeight=FontWeight.Black,modifier=Modifier.padding(top=5.dp)) }
            items(section.value) { game ->
                val gameProgress=progress.firstOrNull { it.id==game.id } ?: WorkGameProgress(game.id)
                FinnyCard(Modifier.fillMaxWidth().clickable { selected=game }) {
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        Surface(color=when(game.category){"Сбережения"->Color(0xFFFFF0C7);"Платежи и покупки"->Color(0xFFFFE4EC);else->Color(0xFFEAF1FF)},shape=RoundedCornerShape(18.dp)) { Icon(workGameIcon(game.id),null,Modifier.padding(12.dp).size(28.dp),tint=Color(0xFF456DDB)) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(game.title,fontSize=18.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF24355E))
                            Text("Доступен уровень ${gameProgress.level} · пройдено ${gameProgress.completed}/10",fontSize=14.sp,color=Color(0xFF596A84))
                            FinnyProgressTrack(gameProgress.completed/10f,Modifier.fillMaxWidth().padding(top=6.dp),height=6.dp)
                        }
                        Icon(Icons.Default.ChevronRight,null,tint=Color(0xFF456DDB))
                    }
                }
            }
        }
    }
}

private fun foodGoods(day:Int)=listOf(
    CityGood("food_bowl","🥣","Сбалансированный корм","FOOD",GameEconomy.foodCost(day)),
    CityGood("food_fish","🐟","Рыбное угощение","FOOD",26),
    CityGood("food_carrot","🥕","Хрустящая морковь","FOOD",9),
    CityGood("food_apple","🍎","Сладкое яблоко","FOOD",11),
    CityGood("food_berry","🍓","Лесные ягоды","FOOD",13),
    CityGood("water_fresh","💧","Чистая вода","WATER",10),
    CityGood("water_spring","💧","Родниковая вода","WATER",16),
    CityGood("water_milk","🥛","Тёплое молоко","WATER",14),
    CityGood("water_tea","🍵","Ягодный чай","WATER",12),
    CityGood("medicine","💊","Лекарство","MEDICINE",20)
)
// The room currently renders one toy, the ball. Do not sell decorative
// placeholders that disappear after purchase.
private val wantGoods=listOf(CityGood("ball","⚽","Весёлый мяч","WANT",28))
private val skinGoods=listOf(SkinGood("goal_hat","Звёздная шапка",rewardOnly=true),SkinGood("goal_glasses","Очки планировщика",rewardOnly=true),SkinGood("goal_crown","Корона мечты",rewardOnly=true))

private fun goodEffect(g:CityGood)=when(g.id){
    "food_fish"->"Еда +35";"food_bowl"->"Еда +30";"food_carrot"->"Еда +20";"food_apple"->"Еда +18";"food_berry"->"Еда +15"
    "water_spring"->"Вода +35";"water_fresh"->"Вода +30";"water_milk"->"Вода +25";"water_tea"->"Вода +20"
    "medicine"->"Здоровье до 100";else->if(g.category=="WANT")"Покупка по желанию" else "Для заботы"
}

private fun itemIcon(id:String,category:String):ImageVector=when(id){
    "food_bowl","food_basic"->Icons.Default.Restaurant;"food_fish"->Icons.Default.SetMeal
    "food_carrot"->Icons.Default.Grass;"food_apple"->Icons.Default.ShoppingBasket;"food_berry"->Icons.Default.Spa
    "water_fresh","water_spring"->Icons.Default.WaterDrop;"water_milk"->Icons.Default.LocalDrink;"water_tea"->Icons.Default.EmojiFoodBeverage
    "medicine"->Icons.Default.Medication;"ball"->Icons.Default.SportsSoccer;"book"->Icons.Default.MenuBook
    "puzzle"->Icons.Default.Extension;"bow"->Icons.Default.Checkroom;"bed"->Icons.Default.Bed;"ice"->Icons.Default.Icecream
    else->if(category=="FOOD")Icons.Default.Restaurant else if(category=="WATER")Icons.Default.WaterDrop else Icons.Default.ShoppingBag
}

@Composable private fun GoodIllustration(id:String,category:String,modifier:Modifier){
    val picture=when(id){"food_bowl"->R.drawable.good_salad;"medicine"->R.drawable.icon_medicine_bottle;"ice"->R.drawable.good_icecream;"candy"->R.drawable.good_candy;"pizza"->R.drawable.good_pizza;else->null}
    Surface(modifier,shape=RoundedCornerShape(17.dp),color=if(id=="candy")Color(0xFF111527) else Color(0xFFFFF9EC)){
        Box(contentAlignment=Alignment.Center){
            if(picture!=null)Image(painterResource(picture),id,Modifier.fillMaxSize().padding(3.dp),contentScale=ContentScale.Fit)
            else Icon(itemIcon(id,category),id,Modifier.size(32.dp),tint=when(category){"FOOD"->Color(0xFFB56A00);"WATER"->Color(0xFF287DB2);else->Color(0xFF7552A7)})
        }
    }
}

@Composable private fun StoreInterior(goods:List<CityGood>,buy:(CityGood)->Unit){
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=Color.White,shadowElevation=5.dp){Text("Выбери товар. Если рублей не хватит, игра объяснит, как заработать ещё.",Modifier.padding(14.dp),fontSize=15.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF3D4E6C))}}
        items(goods){g->FinnyCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){GoodIllustration(g.id,g.category,Modifier.size(58.dp));Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(g.title,fontWeight=FontWeight.Bold,fontSize=18.sp,maxLines=2,overflow=TextOverflow.Ellipsis);Text(goodEffect(g),fontSize=12.sp,fontWeight=FontWeight.Bold,color=when(g.category){"FOOD"->Color(0xFF9A6500);"WATER"->Color(0xFF287DB2);"MEDICINE"->Color(0xFFB94D5D);else->Color(0xFF7552A7)});Text("${g.price} ₽",color=Color(0xFF667085),maxLines=1)};Spacer(Modifier.width(6.dp));Button(onClick={buy(g)},shape=CircleShape,contentPadding=PaddingValues(horizontal=14.dp,vertical=9.dp)){Text("Купить",fontSize=13.sp,fontWeight=FontWeight.Bold)}}}}
    }
}
@Composable private fun SkinStore(level:Int,owned:List<com.finny.pet.data.database.SkinOwnershipEntity>,buy:(SkinGood)->Unit,equip:(SkinGood)->Unit){
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=Color.White,shadowElevation=5.dp){Text("Здесь только аксессуары на голову. Их нельзя купить: выполни одну из трёх целей и получи особую награду.",Modifier.padding(14.dp),fontSize=15.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF3D4E6C))}}
        items(skinGoods){g->val item=owned.firstOrNull{it.skinId==g.id};FinnyCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Surface(Modifier.size(68.dp),color=if(item?.owned==true)Color(0xFFEAF1FF) else Color(0xFFF0F2F6),shape=RoundedCornerShape(20.dp)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){if(item?.owned==true)SkinArtwork(g.id,Modifier.fillMaxSize().padding(5.dp)) else Icon(Icons.Default.Lock,null,Modifier.size(28.dp),tint=Color(0xFF8A94A7))}};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(g.title,fontWeight=FontWeight.Bold,fontSize=18.sp,maxLines=2,overflow=TextOverflow.Ellipsis);Text(if(item?.owned==true)"Получен за цель" else "Эксклюзивная награда за цель",fontSize=14.sp,color=Color(0xFF596A84),maxLines=2)};Spacer(Modifier.width(6.dp));Button(onClick={equip(g)},enabled=item?.owned==true,shape=CircleShape,contentPadding=PaddingValues(horizontal=12.dp,vertical=10.dp)){Text(if(item?.equipped==true)"Снять" else if(item?.owned==true)"Надеть" else "Закрыто",fontSize=13.sp,fontWeight=FontWeight.Bold)}}}}
    }
}
@Composable private fun ActionInterior(subtitle:String,actions:List<Pair<Pair<String,String>,String>>,navigate:(String)->Unit){
    LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=Color.White,shadowElevation=5.dp,border=BorderStroke(1.dp,Color(0xFFD7DFEF))){Text(subtitle,Modifier.padding(14.dp),fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF3D4E6C))}}
        items(actions){a->FinnyCard(Modifier.fillMaxWidth().clickable{navigate(a.second)}){Row(verticalAlignment=Alignment.CenterVertically){Text(a.first.first,fontSize=40.sp);Spacer(Modifier.width(14.dp));Text(a.first.second,fontSize=21.sp,fontWeight=FontWeight.ExtraBold)}}}
    }
}

@Composable fun InventoryScreen(onBack:()->Unit,vm:InventoryViewModel=hiltViewModel()){
    val s by vm.state.collectAsState()
    s.message?.let{FeedbackDialog("Рюкзак",it,vm::clear)}
    Column(Modifier.fillMaxSize().background(Color(0xFFFFFAF2)).padding(16.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Назад")};Icon(Icons.Default.Inventory2,null,tint=Color(0xFF456DDB));Spacer(Modifier.width(8.dp));Text("Рюкзак",fontSize=27.sp,fontWeight=FontWeight.ExtraBold)}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){NeedPill("🍲",s.needs.food,Modifier.weight(1f));NeedPill("💧",s.needs.water,Modifier.weight(1f));NeedPill(if(s.needs.health<100)"🤒" else "❤",s.needs.health,Modifier.weight(1f))}
        Spacer(Modifier.height(14.dp))
        if(s.items.none{it.quantity>0}&&s.skins.none{it.owned}) {
            Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text("Рюкзак пуст. Загляни в магазины на карте.",fontSize=17.sp,color=Color(0xFF667085))}
        } else {
            LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){
                items(s.items.filter{it.quantity>0}){item->
                    FinnyCard(Modifier.fillMaxWidth()){
                        Row(verticalAlignment=Alignment.CenterVertically){
                            GoodIllustration(item.itemId,item.category,Modifier.size(54.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)){Text(item.title,fontWeight=FontWeight.Bold,fontSize=18.sp);Text("В рюкзаке: ${item.quantity}")}
                            if(item.category!="WANT")Button(onClick={vm.use(item.itemId)},contentPadding=PaddingValues(horizontal=11.dp,vertical=9.dp)){Text("Использовать",fontSize=12.sp,fontWeight=FontWeight.Bold)}
                        }
                    }
                }
                if(s.skins.any{it.owned&&it.skinId in headAccessoryIds})item{Text("Головные аксессуары",fontSize=21.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF263A65),modifier=Modifier.padding(top=8.dp))}
                items(s.skins.filter{it.owned&&it.skinId in headAccessoryIds}){skin->FinnyCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){Surface(Modifier.size(58.dp),shape=RoundedCornerShape(17.dp),color=Color(0xFFEAF1FF)){SkinArtwork(skin.skinId,Modifier.fillMaxSize().padding(4.dp))};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(skin.title,fontWeight=FontWeight.Bold,fontSize=18.sp);Text(if(skin.equipped)"Надето" else "Можно надеть",color=Color(0xFF667085))};Button(onClick={vm.equip(skin)}){Text(if(skin.equipped)"Снять" else "Надеть")}}}}
            }
        }
    }
}
@Composable private fun NeedPill(icon:String,value:Int,modifier:Modifier){
    Surface(modifier,shape=CircleShape,color=Color.White,shadowElevation=3.dp){Text("$icon $value%",Modifier.padding(10.dp),fontWeight=FontWeight.Bold)}
}
