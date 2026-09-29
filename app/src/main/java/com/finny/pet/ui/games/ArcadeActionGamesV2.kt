package com.finny.pet.ui.games

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finny.pet.ui.components.FinnyButton
import com.finny.pet.ui.components.FinnyCard
import com.finny.pet.audio.FinnyAudio
import com.finny.pet.audio.FinnySfx
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private data class FlyingObject(val id:Int, val lane:Int, val x:Float, val crystal:Boolean)
private data class FallingObject(val id:Int, val x:Float, val y:Float, val bomb:Boolean, val icon:String)

@Composable
fun RocketGame(done:()->Unit) {
    val objects=remember{mutableStateListOf<FlyingObject>()}
    var lane by remember{mutableIntStateOf(2)}
    var crystals by remember{mutableIntStateOf(0)}
    var lives by remember{mutableIntStateOf(3)}
    var frame by remember{mutableIntStateOf(0)}
    var running by remember{mutableStateOf(true)}
    var ended by remember{mutableStateOf(false)}
    var generation by remember{mutableIntStateOf(0)}
    fun reset(){objects.clear();lane=2;crystals=0;lives=3;frame=0;running=true;ended=false;generation++}
    LaunchedEffect(running,generation) {
        while(running) {
            delay(45)
            frame++
            if(frame%19==0) objects.add(FlyingObject(frame,Random.nextInt(5),1.0f,frame%5!=0))
            for(i in objects.lastIndex downTo 0) {
                val moved=objects[i].copy(x=objects[i].x-.018f)
                if(moved.x<=.13f) {
                    objects.removeAt(i)
                    if(moved.lane==lane) {
                        if(moved.crystal) { crystals++; FinnyAudio.play(FinnySfx.COIN) }
                        else { lives--; FinnyAudio.play(FinnySfx.WARNING) }
                    }
                } else objects[i]=moved
            }
            if(crystals>=10) { running=false; done() }
            else if(lives<=0) { running=false; ended=true }
        }
    }
    FinnyCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){GameStatCard("Кристаллы","$crystals / 10",Color(0xFFE8F3FF),Modifier.weight(1f),"◆");GameStatCard("Прочность",lives.toString(),Color(0xFFFFE8EC),Modifier.weight(1f),"❤")}
        GameInstructionCard("Ракета летит сама. Веди её пальцем вверх и вниз, собирай кристаллы и обходи красные препятствия.",Color(0xFFD64D87),"Управление")
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color(0xFF071532),border=BorderStroke(3.dp,Color(0xFF58C5F5))) {
            BoxWithConstraints(Modifier.fillMaxWidth().height(300.dp).pointerInput(running) {
                detectDragGestures(onDragStart={p->if(running)lane=(p.y/(size.height/5f)).toInt().coerceIn(0,4)},onDrag={change,_ ->change.consume();if(running)lane=(change.position.y/(size.height/5f)).toInt().coerceIn(0,4)})
            }) {
                repeat(5){row->Box(Modifier.fillMaxWidth().offset(y=(row*60).dp).height(59.dp).background(if(row%2==0)Color(0xFF102548) else Color(0xFF142B50)))}
                objects.forEach { obj -> Text(if(obj.crystal)"💎" else "◆",Modifier.offset(x=maxWidth*obj.x-20.dp,y=(obj.lane*60+7).dp),fontSize=36.sp,color=if(obj.crystal)Color.White else Color(0xFFFF826F)) }
                Text("🚀",Modifier.offset(x=12.dp,y=(lane*60+3).dp),fontSize=45.sp)
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button(onClick={lane=(lane-1).coerceAtLeast(0)},modifier=Modifier.weight(1f).heightIn(min=54.dp)){Text("▲ Выше",fontWeight=FontWeight.Bold)}
            Button(onClick={lane=(lane+1).coerceAtMost(4)},modifier=Modifier.weight(1f).heightIn(min=54.dp)){Text("▼ Ниже",fontWeight=FontWeight.Bold)}
        }
        if(ended) { FinnyCoachCard("Три столкновения. Следи за дорожкой впереди и заранее уходи от препятствия.");FinnyButton("Новый полёт",::reset,modifier=Modifier.fillMaxWidth()) }
    }
}

@Composable
fun CatchGame(done:()->Unit) {
    val objects=remember{mutableStateListOf<FallingObject>()}
    var basketX by remember{mutableFloatStateOf(.5f)}
    var caught by remember{mutableIntStateOf(0)}
    var mistakes by remember{mutableIntStateOf(0)}
    var frame by remember{mutableIntStateOf(0)}
    var running by remember{mutableStateOf(true)}
    var ended by remember{mutableStateOf(false)}
    var generation by remember{mutableIntStateOf(0)}
    fun reset(){objects.clear();basketX=.5f;caught=0;mistakes=0;frame=0;running=true;ended=false;generation++}
    LaunchedEffect(running,generation) {
        while(running) {
            delay(40)
            frame++
            if(frame%18==0) {
                val bomb=frame%7==0
                objects.add(FallingObject(frame,Random.nextFloat()*.8f+.1f,0f,bomb,if(bomb)"💣" else listOf("🍎","🍓","🍐","🍊").random()))
            }
            for(i in objects.lastIndex downTo 0) {
                val moved=objects[i].copy(y=objects[i].y+.018f)
                if(moved.y>=.82f) {
                    objects.removeAt(i)
                    if(abs(moved.x-basketX)<.16f) {
                        if(moved.bomb) { mistakes++; FinnyAudio.play(FinnySfx.WARNING) }
                        else { caught++; FinnyAudio.play(FinnySfx.COIN) }
                    }
                } else objects[i]=moved
            }
            if(caught>=8) { running=false;done() }
            else if(mistakes>=3) { running=false;ended=true }
        }
    }
    FinnyCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){GameStatCard("Фрукты","$caught / 8",Color(0xFFE8F7EE),Modifier.weight(1f),"✓");GameStatCard("Ошибки","$mistakes / 3",Color(0xFFFFE8EC),Modifier.weight(1f),"!")}
        GameInstructionCard("Веди корзинку пальцем влево и вправо. Лови фрукты, а от бомбочек уходи в сторону.",Color(0xFF8056C7),"Управление")
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color(0xFF37245F),border=BorderStroke(3.dp,Color(0xFFBCA3FF))) {
            BoxWithConstraints(Modifier.fillMaxWidth().height(330.dp).background(Brush.verticalGradient(listOf(Color(0xFF32205F),Color(0xFF7866AA)))).pointerInput(running) {
                detectDragGestures(onDragStart={p->if(running)basketX=(p.x/size.width).coerceIn(.1f,.9f)},onDrag={change,amount->change.consume();if(running)basketX=(basketX+amount.x/size.width).coerceIn(.1f,.9f)})
            }) {
                objects.forEach { obj -> Text(obj.icon,Modifier.offset(x=maxWidth*obj.x-19.dp,y=320.dp*obj.y),fontSize=34.sp) }
                Text("🛒",Modifier.offset(x=maxWidth*basketX-30.dp,y=272.dp),fontSize=53.sp)
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button(onClick={basketX=(basketX-.16f).coerceAtLeast(.1f)},modifier=Modifier.weight(1f).heightIn(min=54.dp)){Text("◀ Левее",fontWeight=FontWeight.Bold)}
            Button(onClick={basketX=(basketX+.16f).coerceAtMost(.9f)},modifier=Modifier.weight(1f).heightIn(min=54.dp)){Text("Правее ▶",fontWeight=FontWeight.Bold)}
        }
        if(ended) { FinnyCoachCard("Пойманы три бомбочки. Смотри, где падает опасный предмет, и заранее освобождай это место.");FinnyButton("Попробовать ещё",::reset,modifier=Modifier.fillMaxWidth()) }
    }
}

@Composable
fun JumperGame(done:()->Unit) {
    var level by remember{mutableIntStateOf(0)}
    var energy by remember{mutableIntStateOf(3)}
    var lane by remember{mutableIntStateOf(1)}
    var safeLane by remember{mutableIntStateOf(Random.nextInt(3))}
    var running by remember{mutableStateOf(true)}
    var ended by remember{mutableStateOf(false)}
    var generation by remember{mutableIntStateOf(0)}
    val jump by rememberInfiniteTransition(label="jumperMotion").animateFloat(0f,24f,infiniteRepeatable(tween(620,easing=EaseInOutSine),RepeatMode.Reverse),label="jump")
    fun reset(){level=0;energy=3;lane=1;safeLane=Random.nextInt(3);running=true;ended=false;generation++}
    LaunchedEffect(running,level,energy,generation) {
        if(running) {
            delay(1550)
            if(running) {
                if(lane==safeLane) {
                    level++
                    FinnyAudio.play(FinnySfx.PLACE)
                    energy=(energy+1).coerceAtMost(3)
                    if(level>=10) { running=false;done() }
                } else {
                    energy--
                    FinnyAudio.play(FinnySfx.WARNING)
                    if(energy<=0) {running=false;ended=true}
                }
                safeLane=Random.nextInt(3)
            }
        }
    }
    FinnyCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){GameStatCard("Высота","$level / 10",Color(0xFFE8F3FF),Modifier.weight(1f),"↑");GameStatCard("Энергия",energy.toString(),Color(0xFFE7F8F1),Modifier.weight(1f),"⚡")}
        GameInstructionCard("Финни прыгает сам каждые полторы секунды. Успей перевести его на зелёную платформу стрелками или касанием.",Color(0xFF148E87),"Управление")
        Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),color=Color(0xFF0F4960),border=BorderStroke(3.dp,Color(0xFF73DDCA))) {
            Column(Modifier.fillMaxWidth().height(300.dp).padding(12.dp),verticalArrangement=Arrangement.SpaceBetween) {
                Text("☁️               ⭐              ☁️",fontSize=24.sp,modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceAround){repeat(3){i->Box(Modifier.weight(1f),contentAlignment=Alignment.Center){if(lane==i)Text("🦊",Modifier.offset(y=(-jump).dp),fontSize=48.sp)}}}
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {repeat(3){i->Surface(Modifier.weight(1f).height(62.dp).clickable{lane=i},shape=RoundedCornerShape(16.dp),color=if(i==safeLane)Color(0xFF4CD395) else Color(0xFF547080),border=BorderStroke(2.dp,Color.White)){Box(contentAlignment=Alignment.Center){Text(if(i==safeLane)"✓" else "?",fontSize=27.sp,color=Color.White,fontWeight=FontWeight.Black)}}} }
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            Button(onClick={lane=(lane-1).coerceAtLeast(0)},modifier=Modifier.weight(1f).heightIn(min=54.dp)){Text("◀ Левее",fontWeight=FontWeight.Bold)}
            Button(onClick={lane=(lane+1).coerceAtMost(2)},modifier=Modifier.weight(1f).heightIn(min=54.dp)){Text("Правее ▶",fontWeight=FontWeight.Bold)}
        }
        if(ended) { FinnyCoachCard("Энергия закончилась. Сначала найди зелёную платформу, затем переведи Финни на неё.");FinnyButton("Прыгнуть заново",::reset,modifier=Modifier.fillMaxWidth()) }
    }
}
