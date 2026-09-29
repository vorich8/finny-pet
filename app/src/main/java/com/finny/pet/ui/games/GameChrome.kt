package com.finny.pet.ui.games

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finny.pet.ui.theme.FinnyCardBorder
import com.finny.pet.ui.theme.FinnyInk

internal object GameChrome {
    val Ink = FinnyInk
    val Muted = Color(0xFF50617D)
    val CanvasTop = Color(0xFFF4F8FF)
    val CanvasBottom = Color(0xFFFFF8E9)
    val Hint = Color(0xFFFFF2C9)
    val HintBorder = Color(0xFFF2C45E)
    val Success = Color(0xFF238463)
    val Warning = Color(0xFFD56A35)
}

@Composable
internal fun PremiumGameHeader(
    title: String,
    subtitle: String,
    level: Int,
    accent: Color,
    icon: ImageVector,
    onBack: () -> Unit,
    badge: String = "$level / 10",
) {
    Surface(
        color = accent,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color.White.copy(.55f)),
        shadowElevation = 8.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", tint = Color.White)
            }
            Surface(Modifier.size(44.dp), color = Color.White.copy(.18f), shape = RoundedCornerShape(15.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(25.dp), tint = Color.White)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontSize = 18.sp, lineHeight = 21.sp, fontWeight = FontWeight.Black, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(.92f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(shape = CircleShape, color = Color.White.copy(.18f), border = BorderStroke(1.dp, Color.White.copy(.28f))) {
                Text(badge.replace(" ", ""), Modifier.padding(horizontal = 8.dp, vertical = 6.dp), fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Black, maxLines = 1)
            }
        }
    }
}

@Composable
internal fun GameInstructionCard(text: String, accent: Color, title: String = "Что делать") {
    Surface(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color.White,
        border = BorderStroke(1.dp, accent.copy(.35f)), shadowElevation = 4.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(15.dp), verticalAlignment = Alignment.Top) {
            Surface(shape = RoundedCornerShape(13.dp), color = accent.copy(.13f)) {
                Icon(Icons.Default.Lightbulb, null, Modifier.padding(8.dp).size(23.dp), tint = accent)
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Black, color = accent)
                Text(text, fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, color = GameChrome.Ink)
            }
        }
    }
}

@Composable
internal fun GameStatCard(label: String, value: String, background: Color, modifier: Modifier = Modifier, icon: String = "") {
    Surface(
        modifier.heightIn(min = 72.dp), shape = RoundedCornerShape(18.dp), color = background,
        border = BorderStroke(1.dp, FinnyCardBorder), shadowElevation = 3.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 7.dp, vertical = 9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(listOf(icon, label).filter { it.isNotBlank() }.joinToString(" "), Modifier.fillMaxWidth(), fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.Bold, color = GameChrome.Muted, maxLines = 1, softWrap = false, textAlign = TextAlign.Center, overflow = TextOverflow.Ellipsis)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = GameChrome.Ink, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
internal fun FinnyCoachCard(text: String, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = GameChrome.Hint, border = BorderStroke(1.dp, GameChrome.HintBorder)) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(40.dp), shape = CircleShape, color = Color.White.copy(.75f)) {
                Icon(Icons.Default.Pets, null, Modifier.padding(8.dp), tint = Color(0xFF9B6817))
            }
            Spacer(Modifier.width(10.dp))
            Text(text, Modifier.weight(1f), fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF5E4315))
        }
    }
}

internal fun gameBackground(accent: Color): Brush = Brush.verticalGradient(
    listOf(accent.copy(alpha = .18f), GameChrome.CanvasTop, GameChrome.CanvasBottom)
)
