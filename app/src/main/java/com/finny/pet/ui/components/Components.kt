package com.finny.pet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.finny.pet.ui.theme.*
import com.finny.pet.R
import com.finny.pet.audio.FinnyAudio
import com.finny.pet.audio.FinnySfx
import com.finny.pet.ui.screens.VisualPrefs

@Composable fun FinnyButton(text:String, onClick:()->Unit, enabled:Boolean=true, modifier:Modifier=Modifier) = Button(
    onClick={FinnyAudio.play(FinnySfx.TAP);onClick()}, enabled=enabled,
    modifier=modifier.heightIn(min=54.dp).shadow(if(enabled)6.dp else 0.dp,RoundedCornerShape(18.dp)),
    shape=RoundedCornerShape(18.dp),
    colors=ButtonDefaults.buttonColors(containerColor=FinnyBlue,disabledContainerColor=Color(0xFFB9C4DB)),
    border=BorderStroke(1.dp,Color.White.copy(.65f))
) { Text(text, fontSize=16.sp, fontWeight=FontWeight.ExtraBold) }

@Composable fun FinnyCard(modifier:Modifier=Modifier, content: @Composable ColumnScope.() -> Unit) = Card(
    modifier.shadow(6.dp,MaterialTheme.shapes.large), shape=MaterialTheme.shapes.large,
    border=BorderStroke(1.dp,FinnyCardBorder),
    colors=CardDefaults.cardColors(containerColor=FinnyCardBackground)
) { Column(Modifier.background(Brush.verticalGradient(listOf(Color.White,FinnyCardBackground))).padding(16.dp), verticalArrangement=Arrangement.spacedBy(10.dp), content=content) }

@Composable fun PetView(name:String="Питомец", emotion:String="Радость", variantId:String="rabbit_1", stage:Int=1, skinId:String?=null, modifier:Modifier=Modifier, showLabels:Boolean=true, sizeOverride:Dp?=null) {
    val number=variantId.substringAfterLast('_').toIntOrNull() ?: 1
    val species=when {
        variantId.startsWith("rabbit_")->"rabbit"
        variantId.startsWith("squirrel_")->"squirrel"
        variantId.startsWith("hamster_")->"hamster"
        variantId.startsWith("axolotl_")->"axolotl"
        variantId.startsWith("dragon_")->"dragon"
        variantId.startsWith("fox_")->"rabbit"
        variantId.startsWith("variant_")->when(number){4,5->"hamster";6,7->"axolotl";8,9->"dragon";else->"rabbit"}
        else->"rabbit"
    }
    val isSad=emotion.equals("SAD",true)||emotion.equals("SICK",true)||emotion.contains("груст",true)||emotion.contains("забот",true)||emotion.contains("бол",true)||emotion.contains("нездоров",true)
    val drawable=when(species){
        "rabbit"->if(isSad)R.drawable.pet_rabbit_sad else R.drawable.pet_rabbit
        "squirrel"->if(isSad)R.drawable.pet_squirrel_sad else R.drawable.pet_squirrel
        "hamster"->R.drawable.pet_hamster
        "axolotl"->R.drawable.pet_axolotl
        "dragon"->R.drawable.pet_dragon
        else->if(isSad)R.drawable.pet_rabbit_sad else R.drawable.pet_rabbit
    }
    val motion=rememberInfiniteTransition(label="petBreathing")
    val breathe by motion.animateFloat(.985f,1.015f,infiniteRepeatable(tween(1400,easing=EaseInOutSine),RepeatMode.Reverse),label="breathe")
    val floatY by motion.animateFloat(2f,-3f,infiniteRepeatable(tween(1700,easing=EaseInOutSine),RepeatMode.Reverse),label="float")
    Column(modifier, horizontalAlignment=Alignment.CenterHorizontally) {
        val petSize=sizeOverride?:when(stage.coerceIn(1,6)){1->170.dp;2->184.dp;3->196.dp;4->208.dp;5->220.dp;else->232.dp}
        val animate=VisualPrefs.animationsEnabled.value
        // The source illustrations have different transparent bottom margins.
        // Compensate per species so every pet's feet sit on the same baseline.
        val baselineDrop=when(species){"rabbit","squirrel"->.055f;"hamster"->.075f;"axolotl"->.065f;"dragon"->.075f;else->.06f}
        // Keep the artwork and its accessory in one bounded layer. This avoids
        // transparent-padded accessory sprites spilling into adjacent panels on
        // devices with a different density/aspect ratio (notably BlueStacks).
        Box(Modifier.size(petSize).offset(y=petSize*baselineDrop).clip(RoundedCornerShape(1.dp)).graphicsLayer{scaleX=if(animate)breathe else 1f;scaleY=if(animate)breathe else 1f;translationY=if(animate)floatY else 0f},contentAlignment=Alignment.BottomCenter){
            Image(painterResource(drawable),"Питомец $name, стадия $stage",Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
            skinId?.split('|')?.forEach{if(it.isNotBlank())PetSkinOverlay(it,species,petSize)}
        }
        if(showLabels){ Text(name,fontSize=24.sp,fontWeight=FontWeight.Bold); Text(if(emotion.equals("SICK",true))"Мне нужно лекарство" else if(isSad)"Нужна забота" else emotion,fontSize=16.sp,color=FinnyInk) }
    }
}

private fun skinDrawable(skinId:String)=when(skinId){
    "hat_saver","weekly_winter","goal_hat"->R.drawable.skin_saver_hat
    "glasses_smart","goal_glasses"->R.drawable.skin_smart_glasses
    else->R.drawable.skin_wisdom_crown
}

val headAccessoryIds=setOf("hat_saver","glasses_smart","weekly_winter","goal_hat","goal_crown","flower_bow","bowtie")

@Composable fun SkinArtwork(skinId:String,modifier:Modifier=Modifier){
    if(skinId.startsWith("sweater_" )||skinId=="flower_bow"||skinId=="bowtie")WearableArtwork(skinId,modifier)
    else Image(painterResource(skinDrawable(skinId)),skinId,modifier,contentScale=ContentScale.Fit)
}

@Composable private fun WearableArtwork(id:String,modifier:Modifier=Modifier){
    Canvas(modifier){val w=size.width;val h=size.height;val color=when(id){"sweater_mint"->Color(0xFF45B99A);"sweater_coral"->Color(0xFFEE7E72);"flower_bow"->Color(0xFFFF79AA);else->Color(0xFF8057C8)}
        if(id.startsWith("sweater_")){
            val body=androidx.compose.ui.graphics.Path().apply{moveTo(w*.22f,h*.28f);quadraticTo(w*.5f,h*.12f,w*.78f,h*.28f);lineTo(w*.9f,h*.84f);quadraticTo(w*.5f,h*.99f,w*.1f,h*.84f);close()}
            drawPath(body,color);drawRoundRect(color.copy(alpha=.86f),androidx.compose.ui.geometry.Offset(w*.02f,h*.30f),androidx.compose.ui.geometry.Size(w*.25f,h*.42f),androidx.compose.ui.geometry.CornerRadius(w*.10f));drawRoundRect(color.copy(alpha=.86f),androidx.compose.ui.geometry.Offset(w*.73f,h*.30f),androidx.compose.ui.geometry.Size(w*.25f,h*.42f),androidx.compose.ui.geometry.CornerRadius(w*.10f));drawRoundRect(Color.White.copy(alpha=.9f),androidx.compose.ui.geometry.Offset(w*.24f,h*.78f),androidx.compose.ui.geometry.Size(w*.52f,h*.08f),androidx.compose.ui.geometry.CornerRadius(w*.04f))
            val accent=if(id=="sweater_mint")Color(0xFFFFE27A) else Color(0xFFFFF1CF);drawCircle(accent,w*.055f,androidx.compose.ui.geometry.Offset(w*.5f,h*.48f));drawCircle(accent,w*.055f,androidx.compose.ui.geometry.Offset(w*.5f,h*.63f))
        }else if(id=="flower_bow"){
            for(i in 0..4){val a=i*Math.PI*2/5;drawCircle(color,w*.19f,androidx.compose.ui.geometry.Offset(w*.5f+(kotlin.math.cos(a)*w*.2).toFloat(),h*.48f+(kotlin.math.sin(a)*h*.2).toFloat()))};drawCircle(Color(0xFFFFD75D),w*.105f,androidx.compose.ui.geometry.Offset(w*.5f,h*.48f))
        }else{
            drawRoundRect(color,androidx.compose.ui.geometry.Offset(w*.08f,h*.2f),androidx.compose.ui.geometry.Size(w*.84f,h*.62f),androidx.compose.ui.geometry.CornerRadius(w*.15f));val p=androidx.compose.ui.graphics.Path().apply{moveTo(w*.5f,h*.34f);lineTo(w*.3f,h*.14f);lineTo(w*.22f,h*.28f);lineTo(w*.42f,h*.45f);lineTo(w*.5f,h*.38f);lineTo(w*.58f,h*.45f);lineTo(w*.78f,h*.28f);lineTo(w*.7f,h*.14f);close()};drawPath(p,Color(0xFFFFD75D))
        }
    }
}

@Composable fun MedicineArtwork(kind:String="bottle",modifier:Modifier=Modifier){
    val drawable=when(kind){
        "capsule"->R.drawable.icon_medicine_capsule
        "syringe"->R.drawable.icon_medicine_syringe
        else->R.drawable.icon_medicine_bottle
    }
    Image(painterResource(drawable),"Лекарство",modifier,contentScale=ContentScale.Fit)
}

@Composable fun ItemArtwork(id:String,fallback:String,size:Dp=48.dp,medicineKind:String="bottle"){
    if(id=="medicine"||id=="med") MedicineArtwork(medicineKind,Modifier.size(size))
    else Text(fallback,fontSize=(size.value*.62f).sp)
}

@Composable fun ToyArtwork(id:String,modifier:Modifier=Modifier){
    Canvas(modifier){
        val w=size.width; val h=size.height
        when(id){
            "ball"->{
                drawCircle(Color(0xFF4B8EDB),minOf(w,h)*.43f,center=androidx.compose.ui.geometry.Offset(w/2f,h/2f))
                drawArc(color=Color(0xFFF4F7FA),startAngle=-55f,sweepAngle=28f,useCenter=false,topLeft=androidx.compose.ui.geometry.Offset(0f,h*.08f),size=androidx.compose.ui.geometry.Size(w,h*.84f),style=Stroke(width=minOf(w,h)*.12f))
                drawArc(color=Color(0xFFF4F7FA),startAngle=125f,sweepAngle=27f,useCenter=false,topLeft=androidx.compose.ui.geometry.Offset(w*.1f,h*.03f),size=androidx.compose.ui.geometry.Size(w*.8f,h*.94f),style=Stroke(width=minOf(w,h)*.11f))
                drawCircle(Color.White.copy(alpha=.4f),minOf(w,h)*.08f,androidx.compose.ui.geometry.Offset(w*.34f,h*.28f))
            }
            "feeder"->{
                drawRoundRect(Color(0xFFE6A64B),androidx.compose.ui.geometry.Offset(w*.14f,h*.35f),androidx.compose.ui.geometry.Size(w*.72f,h*.38f),androidx.compose.ui.geometry.CornerRadius(w*.1f))
                drawOval(color=Color(0xFFFFD989),topLeft=androidx.compose.ui.geometry.Offset(w*.1f,h*.23f),size=androidx.compose.ui.geometry.Size(w*.8f,h*.29f));drawOval(color=Color(0xFF8B5A31),topLeft=androidx.compose.ui.geometry.Offset(w*.2f,h*.31f),size=androidx.compose.ui.geometry.Size(w*.6f,h*.17f))
                drawRoundRect(Color(0xFF9B6331),androidx.compose.ui.geometry.Offset(w*.2f,h*.7f),androidx.compose.ui.geometry.Size(w*.12f,h*.2f),androidx.compose.ui.geometry.CornerRadius(w*.04f));drawRoundRect(Color(0xFF9B6331),androidx.compose.ui.geometry.Offset(w*.68f,h*.7f),androidx.compose.ui.geometry.Size(w*.12f,h*.2f),androidx.compose.ui.geometry.CornerRadius(w*.04f))
            }
            else->{
                drawRoundRect(Color(0xFFF1A24A),androidx.compose.ui.geometry.Offset(w*.12f,h*.42f),androidx.compose.ui.geometry.Size(w*.76f,h*.42f),androidx.compose.ui.geometry.CornerRadius(w*.06f));drawRoundRect(Color(0xFF5AA7D9),androidx.compose.ui.geometry.Offset(w*.22f,h*.2f),androidx.compose.ui.geometry.Size(w*.56f,h*.3f),androidx.compose.ui.geometry.CornerRadius(w*.06f));drawRoundRect(Color(0xFFE86E73),androidx.compose.ui.geometry.Offset(w*.35f,h*.05f),androidx.compose.ui.geometry.Size(w*.3f,h*.22f),androidx.compose.ui.geometry.CornerRadius(w*.04f))
            }
        }
    }
}

@Composable private fun BoxScope.PetSkinOverlay(skinId:String,species:String,petSize:Dp){
    val (widthFactor,heightFactor,yFactor)=when(skinId){
        "hat_saver","weekly_winter","goal_hat"->when(species){
            "rabbit"->Triple(.29f,.21f,.12f)
            "squirrel"->Triple(.29f,.21f,.09f)
            "hamster"->Triple(.28f,.20f,.02f)
            "axolotl"->Triple(.29f,.21f,.08f)
            "dragon"->Triple(.28f,.20f,.07f)
            else->Triple(.29f,.21f,.07f)
        }
        "glasses_smart","goal_glasses"->Triple(.30f,.14f,when(species){"rabbit"->.32f;"squirrel"->.30f;"hamster"->.18f;"axolotl"->.29f;"dragon"->.27f;else->.29f})
        "sweater_mint","sweater_coral"->Triple(.55f,.38f,.49f)
        "flower_bow"->Triple(.18f,.15f,when(species){"rabbit"->.12f;"squirrel"->.10f;else->.07f})
        "bowtie"->Triple(.19f,.16f,.48f)
        "sun_scarf"->Triple(.42f,.27f,.37f)
        else->when(species){
            "rabbit"->Triple(.27f,.19f,.21f)
            "squirrel"->Triple(.27f,.19f,.15f)
            "hamster"->Triple(.25f,.18f,.00f)
            "axolotl"->Triple(.27f,.19f,.15f)
            "dragon"->Triple(.27f,.19f,.06f)
            else->Triple(.27f,.19f,.08f)
        }
    }
    SkinArtwork(
        skinId,
        Modifier.align(Alignment.TopCenter).offset(y=petSize*yFactor).size(petSize*widthFactor,petSize*heightFactor)
    )
}
@Composable fun CoinCounter(coins:Int,modifier:Modifier=Modifier) {
    Surface(modifier.widthIn(min=74.dp),shape=CircleShape,color=Color(0xFFFFF1B8),shadowElevation=4.dp){
        Row(
            Modifier.padding(horizontal=12.dp,vertical=8.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.Center
        ){
            RubleIcon(Modifier.size(25.dp))
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
@Composable fun RubleIcon(modifier:Modifier=Modifier){
    BoxWithConstraints(modifier,contentAlignment=Alignment.Center){Canvas(Modifier.fillMaxSize()){
        val r=size.minDimension/2f
        drawCircle(Color(0xFF9E641F),r)
        drawCircle(Color(0xFFD99635),r*.91f)
        drawCircle(Color(0xFFFFD776),r*.78f)
        drawCircle(Color(0xFFB87925),r*.68f,style=Stroke(width=r*.055f))
        drawCircle(Color.White.copy(alpha=.32f),r*.60f,center=androidx.compose.ui.geometry.Offset(size.width*.37f,size.height*.32f),style=Stroke(width=r*.08f))
    };Text("P",fontSize=(maxWidth.value*.54f).sp,fontWeight=FontWeight.Black,color=Color(0xFF774813))}
}
@Composable fun FinnyProgressTrack(progress:Float, modifier:Modifier=Modifier, color:Color=FinnyBlue, trackColor:Color=Color(0xFFE4E9F3), height:Dp=10.dp) {
    val safe=progress.coerceIn(0f,1f)
    Box(modifier.height(height).clip(CircleShape).background(trackColor)){
        if(safe>0f) Box(Modifier.fillMaxHeight().fillMaxWidth(safe).background(Brush.verticalGradient(listOf(color.copy(alpha=.78f),color)),CircleShape))
    }
}
@Composable fun FinnyProgressBar(progress:Float, label:String) { Column(verticalArrangement=Arrangement.spacedBy(7.dp)) { Text(label,fontSize=16.sp,fontWeight=FontWeight.Bold,color=FinnyInk);FinnyProgressTrack(progress,Modifier.fillMaxWidth(),height=12.dp) } }
@Composable fun StarsRow(stars:Int) {
    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){(1..3).forEach{index->
        val scale=remember{Animatable(.25f)}
        LaunchedEffect(stars,VisualPrefs.animationsEnabled.value){if(VisualPrefs.animationsEnabled.value){scale.snapTo(.25f);scale.animateTo(1f,tween(380,delayMillis=index*150,easing=EaseOutBack))}else scale.snapTo(1f)}
        Text(if(index<=stars)"★" else "☆",fontSize=38.sp,color=FinnyYellow,modifier=Modifier.graphicsLayer{scaleX=scale.value;scaleY=scale.value})
    }}
}
@Composable fun FeedbackDialog(title:String,message:String,onDismiss:()->Unit,sound:FinnySfx?=null){
    val effect=sound ?: if(title.startsWith("Почему")||title.startsWith("Проверь")||title.startsWith("Проверим")||title.startsWith("Давай разберёмся"))FinnySfx.WARNING else null
    LaunchedEffect(message,effect){effect?.let(FinnyAudio::play)}
    val accent=when(effect){FinnySfx.WARNING->FinnyCoral;FinnySfx.COIN,FinnySfx.LEVEL_UP,FinnySfx.SUCCESS->Color(0xFFB97B0E);else->FinnyBlue}
    AlertDialog(
        onDismissRequest=onDismiss,
        shape=RoundedCornerShape(28.dp),containerColor=Color(0xFFF9FBFF),
        confirmButton={FinnyButton("Понятно",onDismiss,modifier=Modifier.fillMaxWidth())},
        title={Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(11.dp)){
            Surface(shape=RoundedCornerShape(14.dp),color=accent.copy(alpha=.13f)){
                Icon(when(effect){FinnySfx.WARNING->Icons.Default.Lightbulb;FinnySfx.COIN->Icons.Default.Savings;FinnySfx.LEVEL_UP,FinnySfx.SUCCESS->Icons.Default.Stars;else->Icons.Default.Info},null,Modifier.padding(9.dp).size(24.dp),tint=accent)
            }
            Text(title,fontSize=21.sp,lineHeight=25.sp,fontWeight=FontWeight.Black,color=FinnyInk)
        }},
        text={Text(message,fontSize=16.sp,lineHeight=23.sp,color=FinnyInk)}
    )
}
