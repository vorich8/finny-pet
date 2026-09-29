package com.finny.pet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
        "rabbit"->when{skinId=="scarf_sun"&&isSad->R.drawable.pet_rabbit_scarf_sun_sad;skinId=="scarf_sun"->R.drawable.pet_rabbit_scarf_sun;isSad->R.drawable.pet_rabbit_sad;else->R.drawable.pet_rabbit}
        "squirrel"->when{skinId=="scarf_sun"&&isSad->R.drawable.pet_squirrel_scarf_sun_sad;skinId=="scarf_sun"->R.drawable.pet_squirrel_scarf_sun;isSad->R.drawable.pet_squirrel_sad;else->R.drawable.pet_squirrel}
        "hamster"->if(skinId=="scarf_sun")R.drawable.pet_hamster_scarf_sun else R.drawable.pet_hamster
        "axolotl"->if(skinId=="scarf_sun")R.drawable.pet_axolotl_scarf_sun else R.drawable.pet_axolotl
        "dragon"->if(skinId=="scarf_sun")R.drawable.pet_dragon_scarf_sun else R.drawable.pet_dragon
        else->when {
            skinId=="scarf_sun"&&isSad->R.drawable.finny_fox_scarf_sun_sad
            skinId=="scarf_sun"->R.drawable.finny_fox_scarf_sun
            isSad->R.drawable.finny_fox_sad
            else->R.drawable.finny_fox
        }
    }
    val motion=rememberInfiniteTransition(label="petBreathing")
    val breathe by motion.animateFloat(.985f,1.015f,infiniteRepeatable(tween(1400,easing=EaseInOutSine),RepeatMode.Reverse),label="breathe")
    val floatY by motion.animateFloat(2f,-3f,infiniteRepeatable(tween(1700,easing=EaseInOutSine),RepeatMode.Reverse),label="float")
    Column(modifier, horizontalAlignment=Alignment.CenterHorizontally) {
        val petSize=sizeOverride?:when(stage.coerceIn(1,6)){1->170.dp;2->184.dp;3->196.dp;4->208.dp;5->220.dp;else->232.dp}
        val animate=VisualPrefs.animationsEnabled.value
        Box(Modifier.size(petSize).graphicsLayer{scaleX=if(animate)breathe else 1f;scaleY=if(animate)breathe else 1f;translationY=if(animate)floatY else 0f},contentAlignment=Alignment.BottomCenter){
            Image(painterResource(drawable),"Питомец $name, стадия $stage",Modifier.fillMaxSize(),contentScale=ContentScale.Fit)
            skinId?.takeUnless{it=="scarf_sun"}?.let{PetSkinOverlay(it,species,petSize)}
        }
        if(showLabels){ Text(name,fontSize=24.sp,fontWeight=FontWeight.Bold); Text(if(emotion.equals("SICK",true))"Мне нужно лекарство" else if(isSad)"Нужна забота" else emotion,fontSize=16.sp,color=FinnyInk) }
    }
}

private fun skinDrawable(skinId:String)=when(skinId){
    "scarf_sun"->R.drawable.skin_sun_scarf
    "hat_saver","weekly_winter"->R.drawable.skin_saver_hat
    "glasses_smart"->R.drawable.skin_smart_glasses
    "cape_goal"->R.drawable.skin_goal_cape
    else->R.drawable.skin_wisdom_crown
}

@Composable fun SkinArtwork(skinId:String,modifier:Modifier=Modifier){
    Image(painterResource(skinDrawable(skinId)),skinId,modifier,contentScale=ContentScale.Fit)
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

@Composable private fun BoxScope.PetSkinOverlay(skinId:String,species:String,petSize:Dp){
    val (widthFactor,heightFactor,yFactor)=when(skinId){
        "scarf_sun"->Triple(.35f,.235f,when(species){"rabbit"->.51f;"squirrel"->.50f;"hamster"->.46f;"axolotl"->.48f;"dragon"->.48f;else->.60f})
        "hat_saver","weekly_winter"->when(species){
            "rabbit"->Triple(.38f,.32f,.15f)
            "squirrel"->Triple(.38f,.32f,.10f)
            "hamster"->Triple(.34f,.29f,-.03f)
            "axolotl"->Triple(.35f,.30f,.08f)
            "dragon"->Triple(.34f,.29f,.06f)
            else->Triple(.38f,.32f,.05f)
        }
        "glasses_smart"->Triple(.43f,.21f,when(species){"rabbit"->.39f;"squirrel"->.37f;"hamster"->.19f;"axolotl"->.34f;"dragon"->.30f;else->.33f})
        "cape_goal"->Triple(.50f,.34f,when(species){"hamster"->.54f;"axolotl"->.52f;else->.55f})
        else->when(species){
            "rabbit"->Triple(.31f,.22f,.20f)
            "squirrel"->Triple(.31f,.22f,.13f)
            "hamster"->Triple(.28f,.20f,-.03f)
            "axolotl"->Triple(.31f,.22f,.13f)
            "dragon"->Triple(.31f,.22f,.04f)
            else->Triple(.31f,.22f,.06f)
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
