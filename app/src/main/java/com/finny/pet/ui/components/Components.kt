package com.finny.pet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.finny.pet.ui.theme.*
import com.finny.pet.R
import com.finny.pet.ui.screens.VisualPrefs

@Composable fun FinnyButton(text:String, onClick:()->Unit, enabled:Boolean=true, modifier:Modifier=Modifier) = Button(onClick=onClick, enabled=enabled, modifier=modifier.heightIn(min=54.dp).shadow(if(enabled)7.dp else 0.dp,CircleShape), shape=CircleShape, colors=ButtonDefaults.buttonColors(containerColor=FinnyBlue,disabledContainerColor=Color(0xFFB9C4DB)),border=BorderStroke(1.dp,Color.White.copy(.55f))) { Text(text, fontSize=16.sp, fontWeight=FontWeight.ExtraBold) }

@Composable fun FinnyCard(modifier:Modifier=Modifier, content: @Composable ColumnScope.() -> Unit) = Card(modifier.shadow(10.dp,MaterialTheme.shapes.large), shape=MaterialTheme.shapes.large, border=BorderStroke(1.dp,Color(0xFFD6DFEE)), colors=CardDefaults.cardColors(containerColor=Color.White)) { Column(Modifier.background(Brush.verticalGradient(listOf(Color.White,Color(0xFFF3F7FF)))).padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp), content=content) }

@Composable fun PetView(name:String="Финни", emotion:String="Радость", variantId:String="variant_1", stage:Int=1, skinId:String?=null, modifier:Modifier=Modifier, showLabels:Boolean=true, sizeOverride:Dp?=null) {
    val number=variantId.substringAfterLast('_').toIntOrNull() ?: 1
    val species=when {
        variantId.startsWith("rabbit_")->"rabbit"
        variantId.startsWith("squirrel_")->"squirrel"
        variantId.startsWith("hamster_")->"hamster"
        variantId.startsWith("axolotl_")->"axolotl"
        variantId.startsWith("dragon_")->"dragon"
        variantId.startsWith("fox_")->"fox"
        variantId.startsWith("variant_")->when(number){4,5->"hamster";6,7->"axolotl";8,9->"dragon";else->"fox"}
        else->"fox"
    }
    val isSad=emotion.equals("SAD",true)||emotion.equals("SICK",true)||emotion.contains("груст",true)||emotion.contains("забот",true)||emotion.contains("бол",true)||emotion.contains("нездоров",true)
    val drawable=when(species){
        "rabbit"->if(isSad)R.drawable.pet_rabbit_sad else R.drawable.pet_rabbit
        "squirrel"->if(isSad)R.drawable.pet_squirrel_sad else R.drawable.pet_squirrel
        "hamster"->R.drawable.pet_hamster
        "axolotl"->R.drawable.pet_axolotl
        "dragon"->R.drawable.pet_dragon
        else->if(isSad)R.drawable.finny_fox_sad else R.drawable.finny_fox
    }
    val motion=rememberInfiniteTransition(label="petBreathing")
    val breathe by motion.animateFloat(.985f,1.015f,infiniteRepeatable(tween(1400,easing=EaseInOutSine),RepeatMode.Reverse),label="breathe")
    val floatY by motion.animateFloat(2f,-3f,infiniteRepeatable(tween(1700,easing=EaseInOutSine),RepeatMode.Reverse),label="float")
    Column(modifier, horizontalAlignment=Alignment.CenterHorizontally) {
        val petSize=sizeOverride?:when(stage.coerceIn(1,6)){1->170.dp;2->184.dp;3->196.dp;4->208.dp;5->220.dp;else->232.dp}
        val animate=VisualPrefs.animationsEnabled.value
        Box(Modifier.size(petSize).graphicsLayer{scaleX=if(animate)breathe else 1f;scaleY=if(animate)breathe else 1f;translationY=if(animate)floatY else 0f},contentAlignment=Alignment.BottomCenter){Image(painterResource(drawable),"Питомец $name, стадия $stage",Modifier.fillMaxSize(),contentScale=ContentScale.Fit);skinId?.let{Surface(shape=CircleShape,color=Color(0xEEFFFFFF),shadowElevation=4.dp,modifier=Modifier.padding(bottom=5.dp)){Text(when(it){"weekly_winter"->"❄";"scarf_sun"->"☀";"hat_saver"->"◆";"glasses_smart"->"◎";"cape_goal"->"▲";else->"♛"},Modifier.padding(horizontal=13.dp,vertical=5.dp),fontSize=22.sp,color=Color(0xFF7D56C2))}}}
        if(showLabels){ Text(name,fontSize=24.sp,fontWeight=FontWeight.Bold); Text(if(emotion.equals("SICK",true))"Мне нужно лекарство" else if(isSad)"Нужна забота" else emotion,fontSize=16.sp,color=FinnyInk) }
    }
}
@Composable fun CoinCounter(coins:Int,modifier:Modifier=Modifier) {
    Surface(modifier.widthIn(min=74.dp),shape=CircleShape,color=Color(0xFFFFF1B8),shadowElevation=4.dp){
        Row(
            Modifier.padding(horizontal=12.dp,vertical=8.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.Center
        ){
            Image(painterResource(R.drawable.coin),null,Modifier.size(24.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                coins.coerceAtLeast(0).toString(),
                fontSize=18.sp,
                fontWeight=FontWeight.Bold,
                color=FinnyInk,
                maxLines=1,
                softWrap=false,
                overflow=TextOverflow.Clip
            )
        }
    }
}
@Composable fun FinnyProgressTrack(progress:Float, modifier:Modifier=Modifier, color:Color=FinnyBlue, trackColor:Color=Color(0xFFE4E9F3), height:Dp=10.dp) {
    val safe=progress.coerceIn(0f,1f)
    Box(modifier.height(height).clip(CircleShape).background(trackColor)){
        if(safe>0f) Box(Modifier.fillMaxHeight().fillMaxWidth(safe).background(Brush.verticalGradient(listOf(color.copy(alpha=.78f),color)),CircleShape))
    }
}
@Composable fun FinnyProgressBar(progress:Float, label:String) { Column(verticalArrangement=Arrangement.spacedBy(6.dp)) { Text(label,fontSize=16.sp);FinnyProgressTrack(progress,Modifier.fillMaxWidth()) } }
@Composable fun StarsRow(stars:Int) {
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){(1..3).forEach{index->
        val scale=remember{Animatable(.25f)}
        LaunchedEffect(stars,VisualPrefs.animationsEnabled.value){if(VisualPrefs.animationsEnabled.value){scale.snapTo(.25f);scale.animateTo(1f,tween(380,delayMillis=index*150,easing=EaseOutBack))}else scale.snapTo(1f)}
        Text(if(index<=stars)"★" else "☆",fontSize=38.sp,color=FinnyYellow,modifier=Modifier.graphicsLayer{scaleX=scale.value;scaleY=scale.value})
    }}
}
@Composable fun FeedbackDialog(title:String,message:String,onDismiss:()->Unit){ AlertDialog(onDismissRequest=onDismiss,confirmButton={FinnyButton("Понятно",onDismiss)},title={Text(title)},text={Text(message,fontSize=16.sp)}) }
