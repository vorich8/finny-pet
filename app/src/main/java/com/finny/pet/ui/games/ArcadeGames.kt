package com.finny.pet.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finny.pet.domain.GameEconomy
import com.finny.pet.domain.usecase.EarnArcadeReward
import com.finny.pet.audio.FinnyAudio
import com.finny.pet.audio.FinnySfx
import com.finny.pet.ui.components.FinnyButton
import com.finny.pet.ui.components.FinnyCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import javax.inject.Inject

@HiltViewModel
class ArcadeViewModel @Inject constructor(private val earn:EarnArcadeReward):ViewModel(){
    private var finishing=false
    fun finish(game:String,onDone:()->Unit){if(finishing)return;finishing=true;viewModelScope.launch{earn(game);FinnyAudio.play(FinnySfx.SUCCESS);onDone()}}
}

@Composable fun ArcadeGameScreen(kind:String,onBack:()->Unit,onDone:()->Unit,vm:ArcadeViewModel=hiltViewModel()){
    val title=when(kind){"snake"->"Змейка";"rocket"->"Ракета Финни";"pairs"->"Пары";"catch"->"Корзинка";"jumper"->"Джампер";else->"Block Blast"}
    val icon=when(kind){"snake"->Icons.Default.Route;"rocket"->Icons.Default.RocketLaunch;"pairs"->Icons.Default.Style;"catch"->Icons.Default.ShoppingBasket;"jumper"->Icons.Default.Pets;else->Icons.Default.GridView}
    val accent=when(kind){"snake"->Color(0xFF1D9A64);"rocket"->Color(0xFFD64D87);"pairs"->Color(0xFFCF841B);"catch"->Color(0xFF8056C7);"jumper"->Color(0xFF148E87);else->Color(0xFF2F83B8)}
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF120927),Color(0xFF37205E),Color(0xFF080E22))))){
        Column(Modifier.fillMaxSize().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(9.dp)){
            PremiumGameHeader(title,"Игровой зал • награда ${GameEconomy.arcadeReward} P",1,accent,icon,onBack,"АРКАДА")
            when(kind){
                "snake"->SnakeGame{vm.finish("Змейка",onDone)}
                "rocket"->RocketGame{vm.finish("Ракета",onDone)}
                "pairs"->QuickArcadeGame("pairs"){vm.finish("Пары",onDone)}
                "catch"->QuickArcadeGame("catch"){vm.finish("Корзинка",onDone)}
                "jumper"->QuickArcadeGame("jumper"){vm.finish("Джампер",onDone)}
                else->TetrisGame{vm.finish("Block Blast",onDone)}
            }
        }
    }
}

private data class ArcadeCabinet(val id:String,val icon:ImageVector,val title:String,val rule:String,val top:Color,val bottom:Color)
private val arcadeCabinets=listOf(
    ArcadeCabinet("snake",Icons.Default.Route,"ЗМЕЙКА","Собери 4 яблока",Color(0xFF4ADE80),Color(0xFF15803D)),
    ArcadeCabinet("blockblast",Icons.Default.GridView,"БЛОКИ","Собери 2 линии",Color(0xFF38BDF8),Color(0xFF075985)),
    ArcadeCabinet("pairs",Icons.Default.Style,"ПАРЫ","Найди 6 совпадений",Color(0xFFF59E0B),Color(0xFFB45309)),
    ArcadeCabinet("rocket",Icons.Default.RocketLaunch,"РАКЕТА","Собери 10 кристаллов",Color(0xFFEC4899),Color(0xFF831843)),
    ArcadeCabinet("catch",Icons.Default.ShoppingBasket,"КОРЗИНКА","Лови фрукты, не бомбы",Color(0xFFA855F7),Color(0xFF581C87)),
    ArcadeCabinet("jumper",Icons.Default.Pets,"ДЖАМПЕР","Прыгай по платформам",Color(0xFF14B8A6),Color(0xFF134E4A))
)

@Composable fun ArcadeHallPanel(navigate:(String)->Unit){
    Column(Modifier.fillMaxSize().background(Color(0xCC0B1120)).padding(top=12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        Row(Modifier.fillMaxWidth().padding(horizontal=15.dp),verticalAlignment=Alignment.CenterVertically){
            Surface(Modifier.weight(1f),shape=CircleShape,color=Color(0xFFA855F7)){Text("🕹  ИГРОВОЙ ЗАЛ",Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=8.dp),fontWeight=FontWeight.Black,color=Color.White,maxLines=1,overflow=TextOverflow.Ellipsis)}
            Spacer(Modifier.width(8.dp))
            Surface(shape=CircleShape,color=Color(0xFFF59E0B),border=BorderStroke(2.dp,Color(0xFFFEF08A))){Text("P +${GameEconomy.arcadeReward}",Modifier.padding(horizontal=12.dp,vertical=7.dp),fontWeight=FontWeight.Black,color=Color.White)}
        }
        Text("Листай автоматы и выбирай игру",Modifier.padding(horizontal=16.dp),color=Color(0xFFE9D5FF),fontWeight=FontWeight.Bold)
        LazyRow(Modifier.weight(1f),contentPadding=PaddingValues(horizontal=18.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){
            items(arcadeCabinets){cabinet->
                Surface(Modifier.width(230.dp).height(330.dp),shape=RoundedCornerShape(topStart=28.dp,topEnd=28.dp,bottomStart=15.dp,bottomEnd=15.dp),color=cabinet.bottom,border=BorderStroke(4.dp,cabinet.top),shadowElevation=13.dp){
                    Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(11.dp)){
                        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(13.dp),color=Color(0xFF0F172A),border=BorderStroke(2.dp,Color.White.copy(.35f))){Text(cabinet.title,Modifier.padding(8.dp),textAlign=TextAlign.Center,fontWeight=FontWeight.Black,color=Color(0xFFFACC15),fontSize=14.sp)}
                        Surface(Modifier.fillMaxWidth().weight(1f),shape=RoundedCornerShape(18.dp),color=Color(0xFF020617),border=BorderStroke(3.dp,Color(0xFF1E293B))){
                            Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Surface(Modifier.size(86.dp),shape=RoundedCornerShape(24.dp),color=cabinet.top.copy(.18f),border=BorderStroke(2.dp,cabinet.top)){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Icon(cabinet.icon,null,Modifier.size(49.dp),tint=cabinet.top)}};Spacer(Modifier.height(12.dp));Text(cabinet.rule,textAlign=TextAlign.Center,color=Color.White,fontWeight=FontWeight.Bold,fontSize=14.sp)}
                        }
                        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                            Surface(Modifier.size(34.dp),shape=CircleShape,color=Color(0xFFEF4444),shadowElevation=4.dp){}
                            Spacer(Modifier.weight(1f))
                            Button(onClick={navigate(com.finny.pet.navigation.Routes.arcadeGame(cabinet.id))},shape=RoundedCornerShape(13.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFACC15),contentColor=Color(0xFF78350F))){Text("ИГРАТЬ ▶",fontWeight=FontWeight.Black)}
                        }
                    }
                }
            }
        }
        FinnyCoachCard("Выбери автомат. Награда начисляется после завершённой игры, а не за простые нажатия.",Modifier.padding(horizontal=14.dp,vertical=8.dp))
    }
}

private data class Cell(val x:Int,val y:Int)
private data class Piece(val type:Int,val rotation:Int=0,val x:Int=3,val y:Int=0)
private val tetrominoes=listOf(
    listOf(Cell(0,0),Cell(1,0),Cell(2,0),Cell(3,0)),listOf(Cell(0,0),Cell(1,0),Cell(0,1),Cell(1,1)),
    listOf(Cell(0,0),Cell(1,0),Cell(2,0),Cell(1,1)),listOf(Cell(0,0),Cell(0,1),Cell(1,1),Cell(2,1)),
    listOf(Cell(2,0),Cell(0,1),Cell(1,1),Cell(2,1)),listOf(Cell(1,0),Cell(2,0),Cell(0,1),Cell(1,1)),
    listOf(Cell(0,0),Cell(1,0),Cell(1,1),Cell(2,1))
)
private val tetrisColors=listOf(Color(0xFF58C7F2),Color(0xFFFFC34D),Color(0xFFC17CF2),Color(0xFF5F8FF3),Color(0xFFFF8B5E),Color(0xFF66D58B),Color(0xFFF06E91))
private fun rotated(type:Int,rotation:Int):List<Cell>{var r=tetrominoes[type];repeat(rotation%4){r=r.map{Cell(-it.y,it.x)}.let{c->val mx=c.minOf{it.x};val my=c.minOf{it.y};c.map{Cell(it.x-mx,it.y-my)}}};return r}
private fun cells(p:Piece)=rotated(p.type,p.rotation).map{Cell(it.x+p.x,it.y+p.y)}

@Composable private fun LegacyTetrisGame(done:()->Unit){
    val columns=10;val rows=14
    var board by remember{mutableStateOf<Map<Int,Int>>(emptyMap())};var piece by remember{mutableStateOf(Piece(Random.nextInt(tetrominoes.size)))};var nextType by remember{mutableIntStateOf(Random.nextInt(tetrominoes.size))}
    var lines by remember{mutableIntStateOf(0)};var score by remember{mutableIntStateOf(0)};var running by remember{mutableStateOf(true)};var gameOver by remember{mutableStateOf(false)};var tick by remember{mutableIntStateOf(0)}
    fun fits(p:Piece)=cells(p).all{it.x in 0 until columns&&it.y in 0 until rows&&(it.y*columns+it.x)!in board}
    fun reset(){board=emptyMap();piece=Piece(Random.nextInt(tetrominoes.size));nextType=Random.nextInt(tetrominoes.size);lines=0;score=0;gameOver=false;running=true;tick++}
    fun spawn(){piece=Piece(nextType);nextType=Random.nextInt(tetrominoes.size);if(!fits(piece)){gameOver=true;running=false}}
    fun lock(){val placed=board.toMutableMap();cells(piece).forEach{placed[it.y*columns+it.x]=piece.type};val full=(0 until rows).filter{y->(0 until columns).all{y*columns+it in placed}};if(full.isNotEmpty()){val rebuilt=mutableMapOf<Int,Int>();placed.forEach{(i,t)->val x=i%columns;val y=i/columns;if(y !in full){val shift=full.count{it>y};rebuilt[(y+shift)*columns+x]=t}};board=rebuilt;lines+=full.size;score+=full.size*100}else board=placed;if(lines>=2){running=false;done()}else spawn()}
    fun down(){val p=piece.copy(y=piece.y+1);if(fits(p))piece=p else lock();tick++}
    fun move(dx:Int){if(running){val p=piece.copy(x=piece.x+dx);if(fits(p))piece=p;tick++}}
    fun rotate(){if(running){val p=piece.copy(rotation=(piece.rotation+1)%4);listOf(p,p.copy(x=p.x-1),p.copy(x=p.x+1)).firstOrNull(::fits)?.let{piece=it};tick++}}
    fun hardDrop(){if(running){var p=piece;while(fits(p.copy(y=p.y+1)))p=p.copy(y=p.y+1);piece=p;lock();tick++}}
    LaunchedEffect(running,tick){if(running){delay((650-lines*35L).coerceAtLeast(300));down()}}
    val active=if(running)cells(piece).associate{it.y*columns+it.x to piece.type} else emptyMap()
    FinnyCard(Modifier.fillMaxWidth()){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Линии: $lines / 2",fontSize=20.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text("Счёт: $score",fontSize=13.sp,color=Color(0xFF4C5E7B))};Surface(shape=RoundedCornerShape(12.dp),color=Color(0xFFE9EEFF)){Text("Следующая ■",Modifier.padding(8.dp),fontWeight=FontWeight.Bold,color=tetrisColors[nextType])}}
        Text(if(gameOver)"Поле заполнено. Начни заново." else if(running)"Собери 2 линии. Фигуры падают сами." else "Пауза",color=if(gameOver)Color(0xFFB23A48) else Color(0xFF4C5E7B),fontWeight=FontWeight.SemiBold)
        Column(Modifier.align(Alignment.CenterHorizontally).background(Color(0xFF132445),RoundedCornerShape(14.dp)).padding(5.dp),verticalArrangement=Arrangement.spacedBy(1.dp)){repeat(rows){y->Row(horizontalArrangement=Arrangement.spacedBy(1.dp)){repeat(columns){x->val i=y*columns+x;val t=active[i]?:board[i];Box(Modifier.size(15.dp).background(t?.let{tetrisColors[it]}?:Color(0xFF223B69),RoundedCornerShape(3.dp)).border(1.dp,Color.White.copy(.08f),RoundedCornerShape(3.dp)))}}}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){GameControl("◀",Modifier.weight(1f)){move(-1)};GameControl("↻",Modifier.weight(1f)){rotate()};GameControl("▶",Modifier.weight(1f)){move(1)};GameControl("⇓",Modifier.weight(1f)){hardDrop()}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={if(gameOver)reset() else{running=!running;tick++}},modifier=Modifier.weight(1f).heightIn(min=48.dp),shape=RoundedCornerShape(16.dp)){Text(if(gameOver)"Заново" else if(running)"Пауза" else "Продолжить",fontWeight=FontWeight.Bold)};OutlinedButton(onClick={reset()},modifier=Modifier.weight(1f).heightIn(min=48.dp),shape=RoundedCornerShape(16.dp)){Text("Сброс",fontWeight=FontWeight.Bold)}}
    }
}

private enum class Direction(val dx:Int,val dy:Int){UP(0,-1),DOWN(0,1),LEFT(-1,0),RIGHT(1,0)}
private fun opposite(a:Direction,b:Direction)=(a==Direction.UP&&b==Direction.DOWN)||(a==Direction.DOWN&&b==Direction.UP)||(a==Direction.LEFT&&b==Direction.RIGHT)||(a==Direction.RIGHT&&b==Direction.LEFT)

@Composable private fun LegacySnakeGame(done:()->Unit){
    val columns=12;val rows=14
    var snake by remember{mutableStateOf(listOf(Cell(6,7),Cell(5,7),Cell(4,7)))};var direction by remember{mutableStateOf(Direction.RIGHT)};var queued by remember{mutableStateOf(Direction.RIGHT)};var apple by remember{mutableStateOf(Cell(9,7))}
    var apples by remember{mutableIntStateOf(0)};var running by remember{mutableStateOf(true)};var gameOver by remember{mutableStateOf(false)};var tick by remember{mutableIntStateOf(0)}
    fun newApple(occupied:List<Cell>):Cell{val free=buildList{for(y in 0 until rows)for(x in 0 until columns)if(Cell(x,y)!in occupied)add(Cell(x,y))};return free.randomOrNull()?:Cell(0,0)}
    fun reset(){snake=listOf(Cell(6,7),Cell(5,7),Cell(4,7));direction=Direction.RIGHT;queued=Direction.RIGHT;apple=Cell(9,7);apples=0;gameOver=false;running=true;tick++}
    fun turn(d:Direction){if(running&&!opposite(direction,d))queued=d}
    fun step(){direction=queued;val h=snake.first();val n=Cell(h.x+direction.dx,h.y+direction.dy);val ate=n==apple;val body=if(ate)snake else snake.dropLast(1);if(n.x !in 0 until columns||n.y !in 0 until rows||n in body){gameOver=true;running=false;return};snake=listOf(n)+body;if(ate){apples++;if(apples>=4){running=false;done()}else apple=newApple(snake)};tick++}
    LaunchedEffect(running,tick){if(running){delay((390-apples*35L).coerceAtLeast(230));step()}}
    FinnyCard(Modifier.fillMaxWidth()){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("Яблоки: $apples / 4",fontSize=20.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text("Скорость: ${apples+1}",fontSize=13.sp,color=Color(0xFF4C5E7B))};Text(if(gameOver)"💥" else "🍎",fontSize=30.sp)}
        Text(if(gameOver)"Змейка столкнулась. Попробуй ещё!" else if(running)"Меняй направление и собери 4 яблока." else "Пауза",color=if(gameOver)Color(0xFFB23A48) else Color(0xFF4C5E7B),fontWeight=FontWeight.SemiBold)
        Column(Modifier.align(Alignment.CenterHorizontally).background(Color(0xFF143556),RoundedCornerShape(14.dp)).padding(4.dp),verticalArrangement=Arrangement.spacedBy(1.dp)){repeat(rows){y->Row(horizontalArrangement=Arrangement.spacedBy(1.dp)){repeat(columns){x->val c=Cell(x,y);Box(Modifier.size(14.dp).background(when{c==apple->Color(0xFFFF5E66);c==snake.first()->Color(0xFFFFD54D);c in snake->Color(0xFF58CF83);else->if((x+y)%2==0)Color(0xFF235174) else Color(0xFF285A7D)},if(c==apple||c in snake)CircleShape else RoundedCornerShape(2.dp)))}}}}
        Column(Modifier.align(Alignment.CenterHorizontally),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(5.dp)){ArcadeArrow("▲"){turn(Direction.UP)};Row(horizontalArrangement=Arrangement.spacedBy(34.dp)){ArcadeArrow("◀"){turn(Direction.LEFT)};ArcadeArrow("▼"){turn(Direction.DOWN)};ArcadeArrow("▶"){turn(Direction.RIGHT)}}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={if(gameOver)reset() else{running=!running;tick++}},modifier=Modifier.weight(1f).heightIn(min=48.dp),shape=RoundedCornerShape(16.dp)){Text(if(gameOver)"Заново" else if(running)"Пауза" else "Продолжить",fontWeight=FontWeight.Bold)};OutlinedButton(onClick={reset()},modifier=Modifier.weight(1f).heightIn(min=48.dp),shape=RoundedCornerShape(16.dp)){Text("Сброс",fontWeight=FontWeight.Bold)}}
    }
}

private data class SpaceObject(val lane:Int,val crystal:Boolean,val icon:String)

@Composable private fun LegacyRocketGame(done:()->Unit){
    val objects=remember{List(24){i->val hazard=i%4==3;SpaceObject((i*3+1)%5,!hazard,if(hazard)listOf("🪨","🛸","🪐")[i%3] else "💎")}}
    var lane by remember{mutableIntStateOf(2)};var index by remember{mutableIntStateOf(0)};var crystals by remember{mutableIntStateOf(0)};var lives by remember{mutableIntStateOf(3)};var finished by remember{mutableStateOf(false)}
    val current=objects[index.coerceAtMost(objects.lastIndex)]
    fun reset(){lane=2;index=0;crystals=0;lives=3;finished=false}
    FinnyCard(Modifier.fillMaxWidth()){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("💎 $crystals / 10",fontSize=21.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text("Собирай кристаллы и уходи от планет",fontSize=13.sp,color=Color(0xFF5A6780))};Text("❤️ $lives",fontSize=18.sp,fontWeight=FontWeight.Black,color=Color(0xFFE34C67))}
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color(0xFF020B24),border=BorderStroke(3.dp,Color(0xFF38BDF8))){Box(Modifier.fillMaxWidth().height(350.dp).padding(10.dp)){repeat(5){i->Row(Modifier.align(Alignment.CenterStart).offset(y=((i-2)*63).dp).fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){repeat(12){j->Box(Modifier.padding(horizontal=8.dp).size(if((i+j)%3==0)3.dp else 2.dp).background(if((i+j)%3==0)Color.White else Color(0xFF93C5FD),CircleShape))}}};Text("🚀",Modifier.align(Alignment.CenterStart).offset(x=24.dp,y=((lane-2)*63).dp),fontSize=49.sp);Text(current.icon,Modifier.align(Alignment.CenterEnd).offset(x=(-35).dp,y=((current.lane-2)*63).dp),fontSize=if(current.crystal)42.sp else 55.sp)}}
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){GameControl("▲",Modifier.weight(1f)){lane=(lane-1).coerceAtLeast(0)};GameControl("▼",Modifier.weight(1f)){lane=(lane+1).coerceAtMost(4)};Button(onClick={if(finished)return@Button;if(lane==current.lane){if(current.crystal)crystals++ else lives--};index=(index+1)%objects.size;if(crystals>=10){finished=true;done()}else if(lives<=0)finished=true},modifier=Modifier.weight(2f).height(50.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFEC4899))){Text("ПРОЛЕТЕТЬ ▶",fontWeight=FontWeight.Black)}}
        Text(if(lives<=0)"Ракета повреждена. Попробуй другой маршрут." else "Подними или опусти ракету на линию кристалла. От препятствий уходи на свободную линию.",fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=if(lives<=0)Color(0xFFB23A48)else Color(0xFF4C5E7B))
        if(lives<=0)FinnyButton("Новый полёт",{reset()},modifier=Modifier.fillMaxWidth())
    }
}

@Composable private fun QuickArcadeGame(kind:String,done:()->Unit){
    when(kind){
        "pairs"->PairsGame(done)
        "catch"->CatchGame(done)
        else->JumperGame(done)
    }
}

@Composable private fun PairsGame(done:()->Unit){
    val symbols=listOf("🍎","⭐","🎈","🐾","P","💎");val cards=remember{(symbols+symbols).shuffled()};val open=remember{mutableStateListOf<Int>()};val matched=remember{mutableStateListOf<Int>()};var locked by remember{mutableStateOf(false)}
    LaunchedEffect(open.size){if(open.size==2){locked=true;delay(550);if(cards[open[0]]==cards[open[1]]){matched.addAll(open);FinnyAudio.play(FinnySfx.PLACE);if(matched.size>=cards.size)done()}else FinnyAudio.play(FinnySfx.WARNING);open.clear();locked=false}}
    FinnyCard(Modifier.fillMaxWidth()){
        Text("Пары: ${matched.size/2} / 6",fontSize=21.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text("Открой две одинаковые карточки",color=Color(0xFF5A6780))
        Column(Modifier.align(Alignment.CenterHorizontally),verticalArrangement=Arrangement.spacedBy(8.dp)){repeat(3){r->Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){repeat(4){c->val i=r*4+c;val visible=i in open||i in matched;Surface(Modifier.size(69.dp).clickable(enabled=!locked&&i !in matched&&i !in open){open.add(i)},shape=RoundedCornerShape(14.dp),color=if(visible)Color(0xFFFFF2CB)else Color(0xFFF59E0B),border=BorderStroke(2.dp,Color(0xFFFFD54A)),shadowElevation=4.dp){Box(contentAlignment=Alignment.Center){Text(if(visible)cards[i] else "?",fontSize=31.sp,fontWeight=FontWeight.Black,color=Color.White)}}}}}}
        Text("Запоминай расположение символов. Ошибка не забирает пари — просто попробуй снова.",fontSize=13.sp,color=Color(0xFF5A6780))
    }
}

@Composable private fun LegacyCatchGame(done:()->Unit){
    val stream=remember{List(18){i->if(i%5==4)"💣" else listOf("🍎","🍓","🍐","🍊")[i%4]}};var index by remember{mutableIntStateOf(0)};var caught by remember{mutableIntStateOf(0)};var mistakes by remember{mutableIntStateOf(0)};val item=stream[index.coerceAtMost(stream.lastIndex)]
    FinnyCard(Modifier.fillMaxWidth()){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Фрукты: $caught / 8",fontSize=20.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text("Ошибки: $mistakes / 3",fontWeight=FontWeight.Bold,color=Color(0xFFE05667))}
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color(0xFF35205B)){Column(Modifier.fillMaxWidth().height(320.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceBetween){Text(item,Modifier.padding(top=42.dp),fontSize=75.sp);Text("🧺",Modifier.padding(bottom=20.dp),fontSize=70.sp)}}
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedButton(onClick={index=(index+1)%stream.size},modifier=Modifier.weight(1f).height(52.dp)){Text("Пропустить",fontWeight=FontWeight.Bold)};Button(onClick={if(item=="💣"){val next=mistakes+1;if(next>=3){index=0;caught=0;mistakes=0}else mistakes=next}else{val next=caught+1;caught=next;if(next>=8)done()};index=(index+1)%stream.size},modifier=Modifier.weight(1f).height(52.dp),shape=RoundedCornerShape(15.dp)){Text("Поймать",fontWeight=FontWeight.Black)}}
        Text("Лови фрукты, а бомбочки обязательно пропускай.",fontSize=14.sp,color=Color(0xFF5A6780))
    }
}

@Composable private fun LegacyJumperGame(done:()->Unit){
    var height by remember{mutableIntStateOf(0)};var energy by remember{mutableIntStateOf(3)};val safe=(height*2+1)%3
    FinnyCard(Modifier.fillMaxWidth()){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){Text("Высота: $height / 10",fontSize=20.sp,fontWeight=FontWeight.Black,color=Color(0xFF263A65));Text("⚡ $energy",fontWeight=FontWeight.Black,color=Color(0xFF129A8F))}
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color(0xFF073B4C)){Column(Modifier.fillMaxWidth().height(320.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.SpaceAround){Text("☁️     ⭐     ☁️",fontSize=25.sp);Text("        🦊",fontSize=48.sp);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){repeat(3){i->Text(if(i==safe)"🟩" else "🟫",fontSize=55.sp)}}}}
        Text("Выбери зелёную платформу для безопасного прыжка",fontWeight=FontWeight.Bold,color=Color(0xFF4C5E7B))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){repeat(3){i->GameControl(listOf("←","↑","→")[i],Modifier.weight(1f)){if(i==safe){val next=height+1;height=next;energy=(energy+1).coerceAtMost(3);if(next>=10)done()}else{val next=energy-1;if(next<=0){height=0;energy=3}else energy=next}}}}
    }
}

@Composable private fun GameControl(label:String,modifier:Modifier=Modifier,onClick:()->Unit){Button(onClick=onClick,modifier=modifier.height(50.dp),shape=RoundedCornerShape(15.dp),contentPadding=PaddingValues(0.dp)){Text(label,fontSize=22.sp,fontWeight=FontWeight.Black)}}
@Composable private fun ArcadeArrow(label:String,onClick:()->Unit){Surface(Modifier.size(52.dp).clickable(onClick=onClick),shape=CircleShape,color=Color(0xFF4169D8),shadowElevation=5.dp,border=androidx.compose.foundation.BorderStroke(2.dp,Color.White)){Box(contentAlignment=Alignment.Center){Text(label,color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Black)}}}

@Composable fun ArcadeResultScreen(onHome:()->Unit){Column(Modifier.fillMaxSize().navigationBarsPadding().background(gameBackground(Color(0xFF3565D6))).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Surface(shape=RoundedCornerShape(32.dp),color=Color.White,border=BorderStroke(1.dp,Color(0xFFC8D8F2)),shadowElevation=10.dp){Column(Modifier.padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(16.dp)){Icon(Icons.Default.EmojiEvents,null,Modifier.size(70.dp),tint=Color(0xFFE6A521));Text("Отличная игра!",fontSize=30.sp,fontWeight=FontWeight.Black,color=GameChrome.Ink);Surface(shape=CircleShape,color=Color(0xFFFFF0B8)){Text("+${GameEconomy.arcadeReward} P",Modifier.padding(horizontal=25.dp,vertical=16.dp),fontSize=28.sp,fontWeight=FontWeight.Black,color=Color(0xFF8C5B05))};Text("пари уже добавлены в кошелёк. Аркада тренирует внимание и не начисляет учебные звёзды.",fontSize=17.sp,lineHeight=23.sp,textAlign=TextAlign.Center,color=GameChrome.Muted);FinnyButton("Вернуться домой",onHome,modifier=Modifier.fillMaxWidth())}}}}
