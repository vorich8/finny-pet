package com.finny.pet.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

val FinnyBlue = Color(0xFF4169D8)
val FinnyInk = Color(0xFF21345B)
val FinnyCream = Color(0xFFFFFDF8)
val FinnyMint = Color(0xFF45B96B)
val FinnyPurple = Color(0xFF8B5BD9)
val FinnyCoral = Color(0xFFF17775)
val FinnyYellow = Color(0xFFF6C84A)

private val scheme = lightColorScheme(primary=FinnyBlue, secondary=FinnyPurple, tertiary=FinnyMint, background=FinnyCream, surface=Color.White, surfaceVariant=Color(0xFFEAF0FA), onPrimary=Color.White, onSecondary=Color.White, onBackground=FinnyInk, onSurface=FinnyInk, onSurfaceVariant=Color(0xFF40516F), outline=Color(0xFF687893), outlineVariant=Color(0xFFC6D0E1), error=FinnyCoral)

@Composable fun FinnyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme=scheme, typography=Typography(), shapes=Shapes(small=RoundedCornerShape(12.dp), medium=RoundedCornerShape(20.dp), large=RoundedCornerShape(28.dp)), content=content)
}
