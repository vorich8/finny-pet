package com.finny.pet.ui.screens

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import com.finny.pet.navigation.Routes
import com.finny.pet.R
import com.finny.pet.audio.FinnyAudio
import com.finny.pet.audio.FinnySfx
import com.finny.pet.ui.components.*
import kotlinx.coroutines.delay

@Composable fun BootstrapScreen(onFirstLaunch:()->Unit,onReady:()->Unit,vm:BootstrapViewModel=hiltViewModel()){
    val state by vm.state.collectAsState()
    LaunchedEffect(state.loading,state.hasProfile){if(!state.loading){if(state.hasProfile)onReady() else onFirstLaunch()}}
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE4F1FF),Color(0xFFFFF2D7)))),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){PetView(showLabels=false,sizeOverride=150.dp);CircularProgressIndicator(color=Color(0xFF456DDB));Text("Загружаем питомца…",fontSize=15.sp,fontWeight=FontWeight.Bold,color=Color(0xFF40516F))}}
}

@Composable fun OnboardingScreen(onNext:()->Unit){
    Box(Modifier.fillMaxSize()){
        Image(painterResource(R.drawable.room_background),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color.White.copy(.35f),Color.White.copy(.95f)))))
        Column(Modifier.fillMaxSize().padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Bottom){
            PetView(showLabels=false)
            Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(28.dp),color=Color.White.copy(.96f),shadowElevation=12.dp,border=BorderStroke(1.dp,Color(0xFFD9E4F4))){
                Column(Modifier.padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(7.dp)){
                    Text("Привет!",fontSize=30.sp,fontWeight=FontWeight.Black,color=Color(0xFF25365C))
                    Text("Выбери нового друга и вместе учитесь обращаться с пари.",fontSize=17.sp,lineHeight=23.sp,color=Color(0xFF42506C),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(5.dp))
                    FinnyButton("Выбрать друга",onNext,modifier=Modifier.fillMaxWidth())
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable fun MoneyTutorialScreen(onDone:()->Unit){
    val cards=listOf(
        Triple("🍲","Сначала — важное","Сначала оставь деньги на еду, воду, здоровье и другие обязательные расходы."),
        Triple("🎈","Потом — желания","Игрушки и развлечения можно выбрать, если после важного остались деньги."),
        Triple("⭐","И ещё — мечта","Откладывай небольшую часть P регулярно. Так большая цель становится ближе.")
    )
    var page by rememberSaveable{mutableIntStateOf(0)}
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFDDEEFF),Color(0xFFFFF2D6))))){
        Column(Modifier.fillMaxSize().padding(22.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){Text("Три шага к умному выбору",Modifier.weight(1f),fontSize=21.sp,fontWeight=FontWeight.Black,color=Color(0xFF25365C),maxLines=2);Text("${page+1} / 3",fontWeight=FontWeight.Bold,color=Color(0xFF456DDB),maxLines=1,softWrap=false)}
            Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(34.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(14.dp)){Column(Modifier.fillMaxWidth().padding(26.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(18.dp)){Text(cards[page].first,fontSize=92.sp);Text(cards[page].second,fontSize=28.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text(cards[page].third,fontSize=19.sp,color=Color(0xFF58677F),textAlign=androidx.compose.ui.text.style.TextAlign.Center);Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){cards.indices.forEach{Surface(Modifier.size(if(it==page)28.dp else 10.dp,10.dp),shape=CircleShape,color=if(it==page)Color(0xFF456DDB) else Color(0xFFD8DEEA)){}}}}}
            FinnyButton(if(page==2)"Открыть карту" else "Дальше",{if(page==2)onDone() else page++},modifier=Modifier.fillMaxWidth())
        }
    }
}

@Composable fun CreatePetScreen(onDone:()->Unit,vm:CreatePetViewModel=hiltViewModel()){
    val pets=listOf(
        Triple("rabbit","Зайка","Добрый и внимательный"), Triple("squirrel","Белка","Весёлая и запасливая"),
        Triple("hamster","Хомяк","Уютный друг"), Triple("axolotl","Аксолотль","Необычный исследователь"), Triple("dragon","Дракончик","Маленький мечтатель")
    )
    var name by remember{mutableStateOf("Лучик")};var species by remember{mutableStateOf("rabbit")}
    Box(Modifier.fillMaxSize()){
        Image(painterResource(R.drawable.room_background),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xBBE8F5FF),Color(0xEEFFF8EA)))))
        LazyColumn(Modifier.fillMaxSize(),contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(15.dp)){
            item{Text("Кто станет твоим другом?",fontSize=28.sp,fontWeight=FontWeight.Black,color=Color(0xFF20365F));Text("Выбери питомца и придумай ему имя",fontSize=16.sp,color=Color(0xFF596A84))}
            item{Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(12.dp)){pets.forEach{pet->
                val selected=species==pet.first
                Card(Modifier.width(196.dp).height(258.dp).clickable{species=pet.first},shape=RoundedCornerShape(28.dp),border=BorderStroke(if(selected)4.dp else 1.dp,if(selected)Color(0xFFFFC83D) else Color(0xFFD9E4F4)),colors=CardDefaults.cardColors(containerColor=Color.White.copy(.97f)),elevation=CardDefaults.cardElevation(if(selected)12.dp else 5.dp)){
                    Column(Modifier.fillMaxSize().padding(10.dp),horizontalAlignment=Alignment.CenterHorizontally){Box(Modifier.weight(1f),contentAlignment=Alignment.Center){PetView(pet.second,variantId="${pet.first}_1",showLabels=false)};Text(pet.second,fontSize=20.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF263A65));Text(pet.third,fontSize=14.sp,color=Color(0xFF596A84),maxLines=1);Surface(Modifier.padding(top=6.dp).heightIn(min=36.dp),shape=CircleShape,color=if(selected)Color(0xFFDDF5E8) else Color(0xFFEAF0FF)){Row(Modifier.padding(horizontal=14.dp,vertical=7.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(5.dp)){if(selected)Icon(Icons.Default.CheckCircle,null,Modifier.size(18.dp),tint=Color(0xFF2F7C58));Text(if(selected)"Выбран" else "Выбрать",fontWeight=FontWeight.Bold,color=if(selected)Color(0xFF2F7C58) else Color(0xFF365FC4))}}}
                }
            }}}
            item{OutlinedTextField(value=name,onValueChange={name=it.take(16)},label={Text("Как назовём друга?")},modifier=Modifier.fillMaxWidth(),singleLine=true,shape=RoundedCornerShape(20.dp));Spacer(Modifier.height(8.dp));FinnyButton("Начать приключение",{vm.create(name,"${species}_1",onDone)},enabled=name.isNotBlank(),modifier=Modifier.fillMaxWidth())}
        }
    }
}

@Composable fun HomeScreen(nav:NavHostController,vm:HomeViewModel=hiltViewModel(),storyVm:StoryViewModel=hiltViewModel(),settingsVm:SettingsViewModel=hiltViewModel(),budgetVm:BudgetPlanViewModel=hiltViewModel()){
    val home by vm.state.collectAsState()
    val story by storyVm.state.collectAsState()
    val settings by settingsVm.state.collectAsState()
    val budget by budgetVm.state.collectAsState()
    story.message?.let{FeedbackDialog("План дня",it,storyVm::clear)}
    val announcement by vm.announcement.collectAsState();announcement?.let{FeedbackDialog("Новый этап открыт!",it,vm::dismissAnnouncement,FinnySfx.LEVEL_UP)}
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val labels=listOf("Дом","Карта","Ещё","Цели")
    val icons=listOf(R.drawable.nav_home,R.drawable.nav_map,R.drawable.nav_knowledge,R.drawable.nav_goals)
    Scaffold(containerColor=Color(0xFFF6F2EB),bottomBar={FinnyBottomBar(labels,icons,tab){tab=it}}){pad->
        Box(Modifier.padding(pad).fillMaxSize()){
            if(!home.ready){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator(color=Color(0xFF456DDB))}} else when(tab){
                0->HomeDashboard(home.petName,home.coins,home.stars,home.variantId,home.equippedSkin,home.hunger,home.water,home.joy,home.health,home.level,home.petStage,home.period,home.placedToys,story,budget,home.suggestedTask,home.suggestedLevel,settings.sound,onSoundToggle={settingsVm.sound(!settings.sound)},onBudgetChange={kind,value->budgetVm.set(kind,value)},onBudgetConfirm=budgetVm::confirm,onBudgetClear=budgetVm::clear,onTask={nav.navigate(Routes.educationalGame(home.suggestedGameId,home.suggestedLevel))},onAllTasks={nav.navigate(Routes.building("work"))},onFinishDay={storyVm.check{nav.navigate(Routes.DaySummary)}},onPet={nav.navigate(Routes.Pet)})
                1->CityMapScreen(home.level,home.period,home.coins){nav.navigate(Routes.building(it))}
                2->MoreHub(onAdult={nav.navigate(Routes.Adult)})
                else->GoalsHub()
            }
        }
    }
}

@Composable private fun FinnyBottomBar(labels:List<String>,icons:List<Int>,selected:Int,onSelect:(Int)->Unit){
    val iconSize=34.dp
    Surface(color=Color(0xFF233F7D),shadowElevation=14.dp){
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=8.dp,vertical=6.dp).height(68.dp),horizontalArrangement=Arrangement.spacedBy(3.dp)){
            labels.indices.forEach{i->
                val active=selected==i
                val scale by animateFloatAsState(if(active)1f else .91f,spring(),label="navScale")
                Surface(Modifier.weight(1f).fillMaxHeight().clickable{if(i!=selected){FinnyAudio.play(FinnySfx.NAVIGATE);onSelect(i)}},shape=RoundedCornerShape(29.dp),color=if(active)Color(0xFFF8FBFF) else Color.Transparent){
                    Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
                        if(i==2)Icon(Icons.Default.MoreHoriz,labels[i],Modifier.size(iconSize).graphicsLayer{scaleX=scale;scaleY=scale},tint=if(active)Color(0xFF456DDB) else Color(0xFF8CB3FF))
                        else Image(painterResource(icons[i]),labels[i],Modifier.size(iconSize).graphicsLayer{scaleX=scale;scaleY=scale},contentScale=ContentScale.Fit,alpha=if(active)1f else .92f)
                        Text(labels[i],fontSize=14.sp,color=if(active)Color(0xFF19345F) else Color.White,fontWeight=if(active)FontWeight.ExtraBold else FontWeight.SemiBold,maxLines=1)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun HomeDashboard(name:String,coins:Int,stars:Int,variantId:String,equippedSkin:String?,hunger:Int,water:Int,joy:Int,health:Int,level:Int,petStage:Int,period:Int,placedToys:Set<String>,story:StoryUiState,budget:BudgetPlanState,suggestedTask:String,suggestedLevel:Int,soundOn:Boolean,onSoundToggle:()->Unit,onBudgetChange:(String,Int)->Unit,onBudgetConfirm:()->Unit,onBudgetClear:()->Unit,onTask:()->Unit,onAllTasks:()->Unit,onFinishDay:()->Unit,onPet:()->Unit){
    var showPlan by rememberSaveable{mutableStateOf(false)}
    val phrases=when{
        health<100->listOf("Мне нездоровится. Лекарство из рюкзака поможет поправиться.","Давай позаботимся о здоровье.")
        hunger<25->listOf("Я проголодался. Заглянем в магазин продуктов?","Сначала выберем полезную еду, а потом подумаем о желаниях.")
        water<25->listOf("Хочется пить. В магазине есть несколько полезных напитков.","Давай сначала пополним запас воды.")
        joy<30->listOf("Мне немного грустно. Давай поиграем вместе!","Совместная игра поднимет мне настроение.")
        else->listOf("Я готов к новому заданию!","Давай понемногу копить на нашу мечту.","Сначала важное, затем желания и накопления!")
    }
    var phraseIndex by rememberSaveable{mutableIntStateOf(0)}
    LaunchedEffect(phrases,phraseIndex){delay(5_500);phraseIndex=(phraseIndex+1)%phrases.size}
    BoxWithConstraints(Modifier.fillMaxSize()){
        Image(painterResource(R.drawable.room_background),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x26152C54),Color.Transparent,Color(0xA91C315B)))))
        Row(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(horizontal=16.dp,vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){
            Surface(Modifier.weight(1f),color=Color(0xF02A477D),shape=RoundedCornerShape(22.dp),border=BorderStroke(1.dp,Color.White.copy(.4f)),shadowElevation=9.dp){Row(Modifier.padding(horizontal=13.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Surface(Modifier.size(38.dp),shape=CircleShape,color=Color(0x33FFFFFF)){Box(contentAlignment=Alignment.Center){Text("🐾",fontSize=20.sp)}};Spacer(Modifier.width(9.dp));Column(Modifier.weight(1f)){Text(name,color=Color.White,fontWeight=FontWeight.Black,fontSize=18.sp,maxLines=1,overflow=TextOverflow.Ellipsis);Text("Ур. $level  •  ★ $stars",color=Color(0xFFDCE8FF),fontSize=13.sp,maxLines=1)}}}
            IconButton(onClick=onSoundToggle,modifier=Modifier.size(48.dp).background(Color(0xF02A477D),CircleShape)){Icon(if(soundOn)Icons.Default.VolumeUp else Icons.Default.VolumeOff,if(soundOn)"Выключить звук" else "Включить звук",tint=Color.White)}
            CoinCounter(coins)
        }
        // Keep the pet inside a dedicated middle zone.  The old centered offset
        // used the full window, so on narrow/tall BlueStacks profiles the pet
        // could collide with the header or the bottom controls.
        val availableWidth = maxWidth
        val availableHeight = maxHeight
        Box(
            Modifier
                .fillMaxSize()
                .padding(top = 74.dp, bottom = 214.dp),
            contentAlignment = Alignment.Center
        ) {
            val middleHeight = (availableHeight - 288.dp).coerceAtLeast(120.dp)
            val safePetSize = minOf(availableWidth * .72f, middleHeight * .86f, 244.dp)
            PetView(
                name = name,
                emotion = if (health < 100) "SICK" else if (hunger < 25 || water < 25 || joy < 30) "SAD" else "JOY",
                variantId = variantId,
                stage = petStage,
                skinId = equippedSkin,
                showLabels = false,
                sizeOverride = safePetSize,
                modifier = Modifier
                    .offset(y = middleHeight * .10f)
                    .clickable { FinnyAudio.play(FinnySfx.PET); onPet() }
            )
            if("ball" in placedToys) ToyArtwork("ball",Modifier.align(Alignment.Center).offset(x=92.dp,y=82.dp).size(62.dp))
            if("feeder" in placedToys) ToyArtwork("feeder",Modifier.align(Alignment.Center).offset(x=(-96).dp,y=82.dp).size(58.dp))
            if("blocks" in placedToys) ToyArtwork("blocks",Modifier.align(Alignment.Center).offset(x=108.dp,y=40.dp).size(52.dp))
        }
        Column(Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(start=14.dp,end=14.dp,bottom=8.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){NeedChip("🍲","Еда",hunger/100f,Color(0xFF63C86A),Modifier.weight(1f));NeedChip("💧","Вода",water/100f,Color(0xFF45A9ED),Modifier.weight(1f));NeedChip(if(health<100)"🤒" else "😊",if(health<100)"Здоровье" else "Радость",(if(health<100)health else joy)/100f,if(health<100)Color(0xFFF17775) else Color(0xFFFFC446),Modifier.weight(1f))}
            Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp),color=Color(0xF5FFFFFF),border=BorderStroke(1.dp,Color(0xFFD5E0F2)),shadowElevation=4.dp){Row(Modifier.padding(horizontal=12.dp,vertical=10.dp),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Баланс",fontSize=12.sp,color=Color(0xFF667085));Text("$coins P",fontSize=18.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65))};Column(Modifier.weight(1f)){Text("Накоплено",fontSize=12.sp,color=Color(0xFF667085));Text("${story.saved} P",fontSize=18.sp,fontWeight=FontWeight.Black,color=Color(0xFF2F7C58))};Column(Modifier.weight(1.2f)){Text("Текущая цель",fontSize=12.sp,color=Color(0xFF667085));Text("${story.saved} / ${story.target} P",fontSize=18.sp,fontWeight=FontWeight.Black,color=Color(0xFF7655B3))}}}
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button(onClick=if(story.ready)onFinishDay else onTask,modifier=Modifier.weight(1.35f).height(56.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=if(story.ready)Color(0xFF338B63) else Color(0xFF4169D8))){Icon(if(story.ready)Icons.Default.CheckCircle else Icons.Default.PlayArrow,null);Spacer(Modifier.width(7.dp));Text(if(story.ready)"Итоги дня" else "К заданию: $suggestedTask",maxLines=2,fontWeight=FontWeight.Black,fontSize=14.sp)}
                Button(onClick={showPlan=true},modifier=Modifier.weight(.85f).height(56.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFF4B63F),contentColor=Color(0xFF263A65))){Icon(Icons.Default.Checklist,null);Spacer(Modifier.width(6.dp));Text("План дня",fontWeight=FontWeight.Black,maxLines=1)}
            }
        }
    }
    if(showPlan)ModalBottomSheet(onDismissRequest={showPlan=false},containerColor=Color(0xFFF8FAFF),scrimColor=Color.Black.copy(.45f),shape=RoundedCornerShape(topStart=30.dp,topEnd=30.dp)){
        Column(Modifier.fillMaxWidth().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(start=20.dp,end=20.dp,bottom=24.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
            Row(verticalAlignment=Alignment.CenterVertically){Text("План дня ${period.coerceIn(1,5)} из 5",Modifier.weight(1f),fontSize=24.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text("${story.earned} / ${story.target}",fontWeight=FontWeight.Black,color=Color(0xFF4169D8))}
            BudgetPlanCard(budget,onBudgetChange,onBudgetConfirm,onBudgetClear)
            FinnyProgressTrack(story.earned.toFloat()/story.target.coerceAtLeast(1),Modifier.fillMaxWidth(),color=Color(0xFFFFB93F),height=12.dp)
            PlanRequirement(if(story.earned>=story.target)"Заработать ${story.target} P в заданиях" else "Заработать ещё ${(story.target-story.earned).coerceAtLeast(0)} P",story.earned>=story.target)
            PlanRequirement("Купить полезный корм в «Продуктах»",story.cared)
            PlanRequirement(when{story.saved>0->"Отложено в Банк: ${story.saved} P";story.goalCompleted->"Недельная цель уже накоплена";else->"Отложить доступную сумму в Банке"},story.savingDone)
            Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp),color=Color.White,border=BorderStroke(1.dp,Color(0xFFD5DEF0)),shadowElevation=5.dp){Row(Modifier.fillMaxWidth().clickable{showPlan=false;if(story.ready)onFinishDay() else onTask()}.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Surface(shape=CircleShape,color=if(story.ready)Color(0xFF43A875) else Color(0xFFFFB84D)){Icon(if(story.ready)Icons.Default.CheckCircle else Icons.Default.Work,null,Modifier.padding(11.dp),tint=Color.White)};Spacer(Modifier.width(12.dp));Column(Modifier.weight(1f)){Text(if(story.ready)"Все пункты выполнены" else suggestedTask,fontSize=18.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text(if(story.ready)"Можно завершить день" else "Уровень $suggestedLevel • награда доступна",color=Color(0xFF52617A))};Icon(Icons.Default.ArrowForward,null,tint=Color(0xFF4169D8))}}
            FinnyButton(if(story.ready)"Открыть итоги" else "Начать задание",{showPlan=false;if(story.ready)onFinishDay() else onTask()},modifier=Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={showPlan=false;onAllTasks()},modifier=Modifier.weight(1f).heightIn(min=50.dp),shape=RoundedCornerShape(18.dp)){Text("Все задания",fontWeight=FontWeight.Bold)};OutlinedButton(onClick={showPlan=false;onFinishDay()},modifier=Modifier.weight(1f).heightIn(min=50.dp),shape=RoundedCornerShape(18.dp)){Text("Проверить день",fontWeight=FontWeight.Bold)}}
        }
    }
}

@Composable private fun BudgetPlanCard(s:BudgetPlanState,onChange:(String,Int)->Unit,onConfirm:()->Unit,onClear:()->Unit){
    Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp),color=Color.White,border=BorderStroke(1.dp,Color(0xFFD5DEF0)),shadowElevation=5.dp){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){Text("План бюджета",fontSize=19.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65),modifier=Modifier.weight(1f));Text("Доступно ${s.budget} P",fontWeight=FontWeight.Bold,color=Color(0xFF456DDB))}
        Text(if(s.confirmed)"План подтверждён. Ниже видно сравнение с фактом." else "Распредели доступные пари до начала периода.",fontSize=14.sp,color=Color(0xFF667085))
        if(!s.confirmed){
            BudgetLine("Обязательные",s.required,"required",s.budget,onChange)
            BudgetLine("Желания",s.wants,"wants",s.budget,onChange)
            BudgetLine("Накопления",s.saving,"saving",s.budget,onChange)
            Text("Распределено: ${s.planned} P · Остаток: ${s.remaining} P",fontWeight=FontWeight.Bold,color=if(s.planned<=s.budget)Color(0xFF2F7C58) else Color(0xFFB94D5D))
            Button(onClick=onConfirm,enabled=s.planned<=s.budget,modifier=Modifier.fillMaxWidth()){Text("Подтвердить план")}
        }else{
            Text("План: обязательные ${s.required} P, желания ${s.wants} P, накопления ${s.saving} P",fontSize=14.sp,color=Color(0xFF3F506D))
            Text("Факт: обязательные ${s.actualRequired} P, желания ${s.actualWants} P, накопления ${s.actualSaving} P",fontSize=14.sp,color=Color(0xFF3F506D))
            OutlinedButton(onClick=onClear,modifier=Modifier.fillMaxWidth()){Text("Скрыть сообщение")}
        }
        s.message?.let{Text(it,fontSize=13.sp,color=Color(0xFF7655B3))}
    }}
}
@Composable private fun BudgetLine(title:String,value:Int,kind:String,budget:Int,onChange:(String,Int)->Unit){Row(verticalAlignment=Alignment.CenterVertically){Text(title,Modifier.weight(1f),fontWeight=FontWeight.Bold,color=Color(0xFF263A65));IconButton(onClick={onChange(kind,value-5)},enabled=value>0){Icon(Icons.Default.Remove,"Уменьшить")};Text("$value P",modifier=Modifier.width(54.dp),textAlign=androidx.compose.ui.text.style.TextAlign.Center,fontWeight=FontWeight.Black);IconButton(onClick={onChange(kind,value+5)},enabled=value+5<=budget){Icon(Icons.Default.Add,"Увеличить")}}}
@Composable private fun PlanStatus(title:String,done:Boolean,modifier:Modifier=Modifier){Surface(modifier,shape=RoundedCornerShape(16.dp),color=if(done)Color(0xFFE5F6EC) else Color(0xFFFFF1D5),border=BorderStroke(1.dp,if(done)Color(0xFF77BE91) else Color(0xFFE8B95E))){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(done)Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,null,tint=if(done)Color(0xFF28845B) else Color(0xFF9B691D));Spacer(Modifier.width(7.dp));Text(title,fontWeight=FontWeight.ExtraBold,color=Color(0xFF263A65),maxLines=1)}}}

@Composable private fun PlanRequirement(text:String,done:Boolean){Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp),color=if(done)Color(0xFFE7F6ED) else Color(0xFFFFF4DC),border=BorderStroke(1.dp,if(done)Color(0xFF76BE91) else Color(0xFFE8B95E))){Row(Modifier.padding(horizontal=13.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(done)Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,null,tint=if(done)Color(0xFF28845B) else Color(0xFF9B691D));Spacer(Modifier.width(9.dp));Text(text,fontSize=14.sp,fontWeight=FontWeight.Bold,color=Color(0xFF263A65))}}}

@Composable fun DaySummaryScreen(onBack:()->Unit,onNext:(Boolean)->Unit,vm:StoryViewModel=hiltViewModel()){
    val s by vm.state.collectAsState()
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFDCEBFF),Color(0xFFFFF4D6))))){
        Scaffold(containerColor=Color.Transparent,bottomBar={Surface(color=Color.White,shadowElevation=14.dp){FinnyButton(if(s.day>=5)"Завершить неделю" else "Перейти к следующему дню",{vm.next(onNext)},modifier=Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp))}}){pad->
            LazyColumn(Modifier.fillMaxSize().padding(pad),contentPadding=PaddingValues(20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)){
                item{Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Назад")};Text("Итоги дня ${s.day}",fontSize=28.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65))}}
                item{Box(Modifier.fillMaxWidth().height(205.dp),contentAlignment=Alignment.Center){PetView(emotion=if(s.cared&&s.savingDone)"Радость" else "SAD",showLabels=false,sizeOverride=200.dp)}}
                item{FinnyCard(Modifier.fillMaxWidth()){Text("Заработано: ${s.earned} P",fontSize=19.sp,fontWeight=FontWeight.Bold);Text("Потрачено: ${s.spent} P",fontSize=19.sp,fontWeight=FontWeight.Bold);Text(if(s.goalCompleted&&s.saved==0)"Недельная цель уже накоплена" else "Отложено в Банк: ${s.saved} P",fontSize=19.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(8.dp));Text(if(s.cared&&s.savingDone)"Спасибо, что позаботился обо мне и о нашей цели!" else "Завтра попробуем лучше спланировать день.",fontSize=16.sp,color=Color(0xFF667085))}}
            }
        }
    }
}

@Composable fun WeekFinalScreen(onHome:()->Unit,vm:StoryViewModel=hiltViewModel(),adultVm:AdultViewModel=hiltViewModel()){
    val s by vm.state.collectAsState();val a by adultVm.state.collectAsState()
    LazyColumn(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFEDB5),Color(0xFFDCEBFF)))).padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Как ты справился",fontSize=30.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65))};item{Box(Modifier.height(280.dp)){PetView(emotion="Радость",stage=3,showLabels=false)}};item{FinnyCard(Modifier.fillMaxWidth()){Text("Мы сделали это вместе!",fontSize=22.sp,fontWeight=FontWeight.Black);Text("За неделю заработано: ${s.weekEarned} P");Text("Отложено на цель: ${s.weekSaved} P");Text("Пройдено заданий: ${a.completed}");Text("Получено звёзд: ${a.stars}")}};item{FinnyButton("Вернуться домой",onHome,modifier=Modifier.fillMaxWidth())}}
}

@Composable private fun NeedChip(icon:String,title:String,progress:Float,color:Color,modifier:Modifier=Modifier){Surface(modifier.height(54.dp),shape=RoundedCornerShape(18.dp),color=Color.White,border=BorderStroke(1.dp,Color(0xFFD5E0F2)),shadowElevation=5.dp){Column(Modifier.fillMaxSize().padding(horizontal=8.dp,vertical=7.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)){Text("$icon $title",fontWeight=FontWeight.ExtraBold,fontSize=13.sp,color=Color(0xFF263A65),maxLines=1,softWrap=false,overflow=TextOverflow.Ellipsis);FinnyProgressTrack(progress,Modifier.fillMaxWidth(),color=color,height=8.dp)}}}

@Composable private fun SectionTitle(text:String,modifier:Modifier=Modifier){Text(text,modifier=modifier,fontSize=25.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF24355E),maxLines=1,overflow=TextOverflow.Ellipsis)}

@Composable fun GoalsHub(vm:GoalsViewModel=hiltViewModel()){
    val s by vm.state.collectAsState();val withdrawId by vm.withdrawal.collectAsState();s.message?.let{FeedbackDialog("Моя цель",it,vm::clear)}
    withdrawId?.let { id ->
        val amount=s.goals.firstOrNull{it.id==id}?.saved ?: 0
        AlertDialog(onDismissRequest=vm::cancelWithdraw,title={Text("Снять накопления?")},text={Text("Вернуть $amount P из цели в текущий баланс? Цель продолжит ждать накопления.")},dismissButton={TextButton(onClick=vm::cancelWithdraw){Text("Отмена")}},confirmButton={Button(onClick=vm::confirmWithdraw){Text("Снять средства")}})
    }
    val ordered=s.goals.filter{it.id in setOf("goal_first","goal_home","goal_adventure")}.sortedBy{listOf("goal_first","goal_home","goal_adventure").indexOf(it.id)}
    val goals=listOfNotNull(ordered.firstOrNull{!it.completed} ?: ordered.lastOrNull())
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),contentPadding=PaddingValues(top=14.dp,bottom=28.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(22.dp),color=Color.White,shadowElevation=7.dp){Row(Modifier.fillMaxWidth().padding(14.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){SectionTitle("Моя цель",Modifier.weight(1f));CoinCounter(s.coins)}}}
        if(goals.isEmpty())item{FinnyCard(Modifier.fillMaxWidth()){Text("Готовим цель недели…",fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color(0xFF263A65));Text("Она появится через мгновение.",color=Color(0xFF667085))}}
        items(goals){goal->val reward=when(goal.id){"goal_first"->"Без подарка";"goal_home"->"Новая ступень питомца";else->"Корона мечты"};FinnyCard(Modifier.fillMaxWidth()){Text(if(goal.completed)"✓ ${goal.title}" else "★ ${goal.title}",fontSize=21.sp,fontWeight=FontWeight.Bold);Text("Награда: $reward",fontSize=13.sp,fontWeight=FontWeight.Bold,color=Color(0xFF7655B3));FinnyProgressBar(goal.saved.toFloat()/goal.cost,"${goal.saved} / ${goal.cost} P");Text(when{goal.completed->if(goal.id=="goal_first")"Цель достигнута! Питомец продолжает расти." else "Цель достигнута!";s.coins<=0->"Свободных пари пока нет. Сначала выполни задание на Работе.";else->"Свободно: ${s.coins} P. Можно отложить часть суммы, а остальное оставить на заботу."},fontSize=15.sp,color=Color(0xFF667085));if(!goal.completed){Text(if(s.coins>0)"Сколько отложить?" else "Сначала заработай пари",fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=8.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf(5,10,15,20).forEach{amount->OutlinedButton(onClick={vm.add(goal.id,amount)},enabled=s.coins>=amount,modifier=Modifier.weight(1f),contentPadding=PaddingValues(0.dp)){Text("+$amount")}}};if(goal.saved>0)OutlinedButton(onClick={vm.requestWithdraw(goal.id)},modifier=Modifier.fillMaxWidth()){Text("Снять накопленные средства")}}else Text(if(goal.id=="goal_first")"Питомец растёт дальше" else "Награда будет доступна в гардеробе",fontWeight=FontWeight.Bold,color=Color(0xFF2F7C58))}}
    }
}

private data class CatalogGood(val icon:String,val title:String,val price:Int,val tag:String)
@Composable private fun ShopHub(coins:Int,open:()->Unit){
    val goods=listOf(CatalogGood("🥣","Корм для питомца",30,"Нужно"),CatalogGood("💧","Свежая вода",15,"Нужно"),CatalogGood("💊","Лекарство",20,"Нужно"),CatalogGood("🏠","Аренда домика",30,"Нужно"),CatalogGood("⚽","Мяч",25,"Желание"))
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){SectionTitle("Магазин",Modifier.weight(1f));CoinCounter(coins)}};items(goods){g->FinnyCard(Modifier.fillMaxWidth()){Row(verticalAlignment=Alignment.CenterVertically){if(g.title=="Лекарство")MedicineArtwork("bottle",Modifier.size(64.dp)) else Surface(color=Color(0xFFFFF1D6),shape=RoundedCornerShape(18.dp)){Text(g.icon,Modifier.padding(15.dp),fontSize=34.sp)};Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(g.title,fontSize=18.sp,fontWeight=FontWeight.Bold,maxLines=2,overflow=TextOverflow.Ellipsis);Text("${g.price} P • ${g.tag}",fontSize=15.sp,color=Color(0xFF667085),maxLines=1,overflow=TextOverflow.Ellipsis)};IconButton(onClick=open){Icon(Icons.Default.AddShoppingCart,null,tint=Color(0xFF456DDB))}}}}}
}

@Composable private fun MoreHub(onAdult:()->Unit,vm:SettingsViewModel=hiltViewModel()){
    val s by vm.state.collectAsState();var settings by remember{mutableStateOf(false)}
    val context=LocalContext.current
    val audioManager=remember(context){context.getSystemService(Context.AUDIO_SERVICE) as AudioManager}
    var mediaVolume by remember{mutableIntStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC))}
    if(settings)AlertDialog(
        onDismissRequest={settings=false},
        title={Text("Звук и анимация",fontWeight=FontWeight.Black,color=Color(0xFF21345B))},
        text={Column(verticalArrangement=Arrangement.spacedBy(14.dp)){
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically){Text("Анимация",Modifier.weight(1f),fontSize=16.sp);Switch(s.animations,vm::animation)}
            Row(Modifier.fillMaxWidth().heightIn(min=48.dp),verticalAlignment=Alignment.CenterVertically){Text("Звук игры",Modifier.weight(1f),fontSize=16.sp);Switch(s.sound,vm::sound)}
            OutlinedButton(onClick={mediaVolume=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);FinnyAudio.playPreview()},enabled=s.sound,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RoundedCornerShape(16.dp)){
                Icon(Icons.Default.VolumeUp,null);Spacer(Modifier.width(8.dp));Text("Проверить звук",fontSize=16.sp,fontWeight=FontWeight.Bold)
            }
            Text(if(mediaVolume==0)"Громкость мультимедиа на устройстве выключена. Прибавь её кнопкой телефона." else "Не слышно? Прибавь громкость мультимедиа на телефоне и нажми ещё раз.",fontSize=14.sp,color=Color(0xFF40516F),lineHeight=20.sp)
            Text("Настройки сохраняются на устройстве.",fontSize=14.sp,color=Color(0xFF667085))
        }},
        confirmButton={FinnyButton("Готово",{settings=false})}
    )
    LazyColumn(Modifier.fillMaxSize().padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{SectionTitle("Ещё")};item{MenuCard(Icons.Default.MenuBook,"Знания","Короткие уроки встроены в задания"){} };item{MenuCard(Icons.Default.VolumeUp,"Звук и анимация","Настройки доступности"){settings=true} };item{MenuCard(Icons.Default.SupervisorAccount,"Для взрослых","Прогресс и сброс профиля",onAdult)};item{Text("Все данные хранятся только на этом устройстве. Регистрация, реклама и реальные платежи отсутствуют.",fontSize=14.sp,color=Color(0xFF667085),modifier=Modifier.padding(12.dp))}}
}
@Composable private fun MenuCard(icon:androidx.compose.ui.graphics.vector.ImageVector,title:String,subtitle:String,onClick:()->Unit){FinnyCard(Modifier.fillMaxWidth().clickable{FinnyAudio.play(FinnySfx.NAVIGATE);onClick()}){Row(verticalAlignment=Alignment.CenterVertically){Icon(icon,null,Modifier.size(32.dp),tint=Color(0xFF456DDB));Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(title,fontSize=18.sp,fontWeight=FontWeight.Bold);Text(subtitle,fontSize=15.sp,color=Color(0xFF667085))};Icon(Icons.Default.ChevronRight,null)}}}

@Composable fun PetScreen(onBack:()->Unit,onInventory:()->Unit,onQuickGame:()->Unit,vm:HomeViewModel=hiltViewModel()){
    val s by vm.state.collectAsState()
    val stageName=listOf("Малыш","Подросший","Взрослый")[s.petStage.coerceIn(1,3)-1]
    Box(Modifier.fillMaxSize()){
        Image(painterResource(R.drawable.room_background),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0x66213D70),Color.Transparent,Color(0xCC213D70)))))
        LazyColumn(Modifier.fillMaxSize().navigationBarsPadding(),contentPadding=PaddingValues(start=18.dp,top=18.dp,end=18.dp,bottom=28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(10.dp)){
            item{Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),color=Color.White,shadowElevation=7.dp){Row(Modifier.padding(8.dp),verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowBack,"Назад")};Text("Мой питомец",fontSize=25.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF263A65))}}}
            item{Box(Modifier.fillMaxWidth().height(190.dp),contentAlignment=Alignment.Center){PetView(name=s.petName,variantId=s.variantId,stage=s.petStage,skinId=s.equippedSkin,emotion=if(s.health<100)"SICK" else if(s.hunger<25||s.water<25)"SAD" else "JOY",showLabels=false,sizeOverride=190.dp)}}
            item{Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),color=Color.White,shadowElevation=8.dp,border=BorderStroke(1.dp,Color(0xFFD6DEEE))){Column(Modifier.fillMaxWidth().padding(14.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text("Стадия ${s.petStage} из 3 — $stageName",fontSize=19.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF263A65));Text(when{s.health<100->"${s.petName} плохо себя чувствует. Используй лекарство из рюкзака, чтобы восстановить здоровье.";s.hunger<25||s.water<25->"${s.petName} нуждается в заботе. Выбери еду или напиток в магазине продуктов.";else->"${s.petName} растёт вместе с твоими финансовыми навыками."},fontSize=14.sp,lineHeight=18.sp,color=Color(0xFF4F5E77));Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){NeedChip("🍲","Еда",s.hunger/100f,Color(0xFF63C86A),Modifier.weight(1f));NeedChip("💧","Вода",s.water/100f,Color(0xFF45A9ED),Modifier.weight(1f));NeedChip("❤","Здоровье",s.health/100f,Color(0xFFF17775),Modifier.weight(1f))};if(s.health<100||(s.hunger>=25&&s.water>=25)||s.coins>=8)FinnyButton("Открыть рюкзак",onInventory,modifier=Modifier.fillMaxWidth()) else FinnyButton("Заработать пари на Работе",onQuickGame,modifier=Modifier.fillMaxWidth())}}}
        }
    }
}

@Composable fun ResultScreen(stars:Int,onHome:()->Unit,onNext:(String,Int)->Unit,vm:ResultViewModel=hiltViewModel()){
    val s by vm.state.collectAsState()
    LaunchedEffect(s.reward,s.newLevel,s.replay){
        when { s.newLevel>0->FinnyAudio.play(FinnySfx.LEVEL_UP); s.reward>0||s.replay->FinnyAudio.play(FinnySfx.SUCCESS) }
    }
    Scaffold(containerColor=Color.Transparent,bottomBar={Surface(color=Color.White,shadowElevation=14.dp){Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=16.dp,vertical=12.dp),horizontalArrangement=Arrangement.spacedBy(9.dp)){if(s.newLevel>0)OutlinedButton(onClick=onHome,modifier=Modifier.weight(.8f).heightIn(min=54.dp),shape=CircleShape){Text("Домой",fontWeight=FontWeight.Bold)};FinnyButton(if(s.newLevel>0)"Играть уровень ${s.newLevel}" else "Вернуться домой",{if(s.newLevel>0)onNext(s.gameId,s.newLevel) else onHome()},modifier=Modifier.weight(1.2f))}}}){pad->
        Column(Modifier.fillMaxSize().padding(pad).background(Brush.verticalGradient(listOf(Color(0xFFFFE9A8),Color(0xFFF0F6FF)))).padding(horizontal=22.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)){
            Box(Modifier.weight(1f).heightIn(max=235.dp),contentAlignment=Alignment.Center){if(s.ready)PetView(name=s.petName,variantId=s.variantId,stage=s.stage,emotion="Радость",showLabels=false) else CircularProgressIndicator()}
            Text(if(stars==3)"Великолепно!" else if(stars==2)"Хорошая работа!" else "Ты справился!",fontSize=28.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));StarsRow(stars)
            if(s.newLevel>0)Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color(0xFFE7F7EB),border=BorderStroke(1.dp,Color(0xFF86C7A2))){Text("🔓 Открыто новое задание: уровень ${s.newLevel}",Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=10.dp),fontWeight=FontWeight.ExtraBold,color=Color(0xFF28704C),textAlign=androidx.compose.ui.text.style.TextAlign.Center)}
            Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(26.dp),color=Color.White,shadowElevation=9.dp){Column(Modifier.padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)){Text(if(s.reward>0)"Награда за первое прохождение" else "Повтор завершён",fontSize=15.sp,color=Color(0xFF667085));Text(if(s.reward>0)"+${s.reward} P" else "пари уже получены",fontSize=27.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text(if(s.reward>0)"пари уже в кошельке." else "За повтор пари не начисляются, но лучший результат сохраняется.",fontSize=14.sp,color=Color(0xFF667085),textAlign=androidx.compose.ui.text.style.TextAlign.Center)}}
        }
    }
}

@Composable fun AdultScreen(onBack:()->Unit,onReset:()->Unit,vm:AdultViewModel=hiltViewModel()) {
    val s by vm.state.collectAsState()
    var confirmed by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    var resetDialog by remember { mutableStateOf(false) }
    if (!confirmed) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE4F1FF), Color(0xFFFFF1D8)))).navigationBarsPadding().imePadding()) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement=Arrangement.Center, horizontalAlignment=Alignment.CenterHorizontally) {
                FinnyCard(Modifier.fillMaxWidth()) {
                    PetView(showLabels=false, sizeOverride=86.dp)
                    Text("Для взрослых", fontSize=25.sp, fontWeight=FontWeight.Black, color=Color(0xFF263A65))
                    Text("Сколько будет 6 + 3?", fontSize=18.sp, fontWeight=FontWeight.Bold)
                    OutlinedTextField(answer, { answer=it.filter(Char::isDigit).take(2) }, modifier=Modifier.fillMaxWidth(), singleLine=true, label={Text("Ответ")})
                    FinnyButton("Продолжить", { confirmed=answer=="9" }, modifier=Modifier.fillMaxWidth())
                    OutlinedButton(onBack, modifier=Modifier.fillMaxWidth()) { Text("Назад") }
                }
            }
        }
        return
    }
    if (resetDialog) AlertDialog(onDismissRequest={resetDialog=false}, title={Text("Сбросить профиль?")}, text={Text("Прогресс будет удалён. После сброса откроется начальный выбор питомца.")}, dismissButton={TextButton({resetDialog=false}){Text("Отмена")}}, confirmButton={Button({vm.reset(onReset)}){Text("Сбросить")}})
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE4F1FF), Color(0xFFFFF1D8)))).navigationBarsPadding()) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement=Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically) { IconButton(onBack){Icon(Icons.Default.ArrowBack,"Назад")}; Text("Прогресс", fontSize=28.sp, fontWeight=FontWeight.Black, color=Color(0xFF263A65)) }
            FinnyCard(Modifier.fillMaxWidth()) { PetView(showLabels=false, sizeOverride=105.dp); Text("Уровень питомца: ${s.level}", fontSize=18.sp); Text("Завершено заданий: ${s.completed}"); Text("Накоплено звёзд: ${s.stars}"); Text("пари сейчас: ${s.coins}") }
            OutlinedButton({resetDialog=true}, modifier=Modifier.fillMaxWidth()) { Text("Сбросить профиль") }
            FinnyButton("Назад", onBack, modifier=Modifier.fillMaxWidth())
        }
    }
}



