package com.finny.pet.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Primitive palette -> semantic roles -> shared components.
private val DeepNavy = Color(0xFF1B315B)
private val RoyalBlue = Color(0xFF3565D6)
private val Paper = Color(0xFFFFFCF5)
private val PaleBlue = Color(0xFFEAF2FF)
private val BorderBlue = Color(0xFFC8D8F2)
val FinnyBlue = RoyalBlue
val FinnyInk = DeepNavy
val FinnyCream = Paper
val FinnyMint = Color(0xFF2C9C72)
val FinnyPurple = Color(0xFF7956C5)
val FinnyCoral = Color(0xFFD95769)
val FinnyYellow = Color(0xFFFFBE48)
val FinnyCardBackground = Color(0xFFF9FBFF)
val FinnyCardBorder = BorderBlue

private val scheme = lightColorScheme(primary=FinnyBlue, secondary=FinnyPurple, tertiary=FinnyMint, background=FinnyCream, surface=Color.White, surfaceVariant=PaleBlue, onPrimary=Color.White, onSecondary=Color.White, onBackground=FinnyInk, onSurface=FinnyInk, onSurfaceVariant=Color(0xFF40516F), outline=Color(0xFF667A9D), outlineVariant=FinnyCardBorder, error=FinnyCoral)

private val type = Typography(
    headlineMedium = TextStyle(fontSize=27.sp,lineHeight=32.sp,fontWeight=FontWeight.Black,color=FinnyInk),
    titleLarge = TextStyle(fontSize=22.sp,lineHeight=28.sp,fontWeight=FontWeight.ExtraBold,color=FinnyInk),
    titleMedium = TextStyle(fontSize=18.sp,lineHeight=24.sp,fontWeight=FontWeight.Bold,color=FinnyInk),
    bodyLarge = TextStyle(fontSize=16.sp,lineHeight=23.sp,fontWeight=FontWeight.Medium,color=FinnyInk),
    bodyMedium = TextStyle(fontSize=15.sp,lineHeight=21.sp,fontWeight=FontWeight.Medium,color=FinnyInk),
    labelLarge = TextStyle(fontSize=16.sp,lineHeight=20.sp,fontWeight=FontWeight.Bold),
)

@Composable fun FinnyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme=scheme, typography=type, shapes=Shapes(small=RoundedCornerShape(14.dp), medium=RoundedCornerShape(20.dp), large=RoundedCornerShape(26.dp)), content=content)
}
