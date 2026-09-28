package com.finny.pet.ui.games

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finny.pet.domain.MoneyCard
import com.finny.pet.audio.FinnyAudio
import com.finny.pet.audio.FinnySfx
import com.finny.pet.ui.components.FeedbackDialog
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScalesScreen(onBack: () -> Unit, onDone: (Int) -> Unit, vm: ScalesViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    if (state.message.isNotEmpty() && state.message != "Верно!") {
        FeedbackDialog("Проверим решение", state.message, vm::clearMessage, FinnySfx.WARNING)
    }
    LaunchedEffect(state.cards.size) {
        if (state.message == "Верно!") FinnyAudio.play(FinnySfx.PLACE)
    }
    var selectedId by remember(state.level) { mutableStateOf<String?>(null) }
    val selected = state.cards.firstOrNull { it.id == selectedId }
    var dragging by remember { mutableStateOf<MoneyCard?>(null) }
    var pointer by remember { mutableStateOf(Offset.Zero) }
    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    var incomeBounds by remember { mutableStateOf(Rect.Zero) }
    var expenseBounds by remember { mutableStateOf(Rect.Zero) }
    val density = LocalDensity.current
    val tiltTarget = ((state.expenses - state.income) / 6f).coerceIn(-14f, 14f)
    val tilt by animateFloatAsState(tiltTarget, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "scalesTilt")

    LaunchedEffect(state.cards.isEmpty()) {
        if (state.cards.isEmpty()) {
            delay(600)
            vm.finish(onDone)
        }
    }

    Box(
        Modifier.fillMaxSize().onGloballyPositioned { rootOrigin = it.boundsInRoot().topLeft }
            .background(Brush.verticalGradient(listOf(Color(0xFFE0EDFF), Color(0xFFFFF4DB), Color(0xFFF0E8FF))))
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(23.dp), color = Color(0xFF3467BE), shadowElevation = 8.dp) {
                    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.ArrowBack, "Назад", tint = Color.White)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Финансовые весы", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("Уровень ${state.level} из 10", fontSize = 14.sp, color = Color.White)
                        }
                        Icon(Icons.Default.Balance, null, tint = Color(0xFFFFDC72), modifier = Modifier.size(32.dp))
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(17.dp), color = Color.White, shadowElevation = 4.dp) {
                    Text(
                        "Возьми карточку пальцем и перенеси: полученные деньги — налево, покупки — направо. Можно коснуться карточки, а затем чаши.",
                        Modifier.fillMaxWidth().padding(13.dp), fontSize = 16.sp, lineHeight = 21.sp, color = Color(0xFF344866)
                    )
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ScaleStat("+", "Доходы", state.income, Color(0xFFDDF7E8), Modifier.weight(1f))
                    ScaleStat("−", "Расходы", state.expenses, Color(0xFFFFE5DC), Modifier.weight(1f))
                    ScaleStat("=", "Осталось", state.balance, Color(0xFFE4EDFF), Modifier.weight(1f))
                }
            }
            stickyHeader {
                Surface(
                    shape = RoundedCornerShape(26.dp), color = Color(0xFFFFF2D2),
                    border = BorderStroke(2.dp, Color(0xFFE8C16B)), shadowElevation = 9.dp
                ) {
                    BoxWithConstraints(Modifier.fillMaxWidth().height(220.dp)) {
                        Canvas(Modifier.fillMaxSize()) {
                            val middle = size.width / 2f
                            val beamY = 77.dp.toPx()
                            val left = size.width * .23f
                            val right = size.width * .77f
                            val swing = tilt.dp.toPx()
                            drawLine(Color(0xFF8F5E31), Offset(middle, beamY), Offset(middle, 205.dp.toPx()), 13.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(Color(0xFFFFD474), Offset(middle, beamY), Offset(middle, 205.dp.toPx()), 7.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(Color(0xFF6D4730), Offset(left, beamY - swing), Offset(right, beamY + swing), 16.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(Color(0xFFFFCA62), Offset(left, beamY - swing - 3.dp.toPx()), Offset(right, beamY + swing - 3.dp.toPx()), 8.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(Color(0xFF94703C), Offset(left, beamY - swing), Offset(left, 116.dp.toPx() - swing), 3.dp.toPx())
                            drawLine(Color(0xFF94703C), Offset(right, beamY + swing), Offset(right, 116.dp.toPx() + swing), 3.dp.toPx())
                            drawCircle(Color(0xFFFFD56A), 12.dp.toPx(), Offset(middle, beamY))
                            drawCircle(Color(0xFF80532E), 5.dp.toPx(), Offset(middle, beamY))
                        }
                        Text("ДОХОДЫ                                  РАСХОДЫ", Modifier.align(Alignment.TopCenter).padding(top = 13.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF72502A))
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(top = 112.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ScalePan("ДОХОД", state.income, "+", selected != null, Color(0xFFD9F7E8),
                                Modifier.weight(1f).offset(y = (-tilt).dp).onGloballyPositioned { incomeBounds = it.boundsInRoot() }) {
                                selected?.let { vm.place(it, true); selectedId = null }
                            }
                            Spacer(Modifier.width(30.dp))
                            ScalePan("РАСХОД", state.expenses, "−", selected != null, Color(0xFFFFE1D8),
                                Modifier.weight(1f).offset(y = tilt.dp).onGloballyPositioned { expenseBounds = it.boundsInRoot() }) {
                                selected?.let { vm.place(it, false); selectedId = null }
                            }
                        }
                    }
                }
            }
            item { Text("КАРТОЧКИ · осталось ${state.cards.size}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF263A65)) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    state.cards.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                            row.forEach { card ->
                                var cardOrigin by remember(card.id) { mutableStateOf(Offset.Zero) }
                                val active = selectedId == card.id
                                Surface(
                                    Modifier.weight(1f).height(82.dp)
                                        .onGloballyPositioned { cardOrigin = it.boundsInRoot().topLeft }
                                        .pointerInput(card.id) {
                                            detectDragGestures(
                                                onDragStart = { start -> dragging = card; selectedId = card.id; pointer = cardOrigin + start },
                                                onDrag = { change, amount -> change.consume(); pointer += amount },
                                                onDragCancel = { dragging = null },
                                                onDragEnd = {
                                                    when {
                                                        incomeBounds.contains(pointer) -> vm.place(card, true)
                                                        expenseBounds.contains(pointer) -> vm.place(card, false)
                                                    }
                                                    dragging = null
                                                    selectedId = null
                                                }
                                            )
                                        }.clickable { selectedId = card.id },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (active) Color(0xFFFFE4A2) else Color.White,
                                    border = BorderStroke(if (active) 3.dp else 1.dp, if (active) Color(0xFFE6A72D) else Color(0xFFD2DCF0)),
                                    shadowElevation = 5.dp
                                ) { ScaleCardFace(card) }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(17.dp), color = Color(0xFFFFE9B9)) {
                    Text(if (selected == null) "Держи карточку и веди к чаше. Доход — слева, расход — справа." else "«${selected.title}»: деньги приходят или уходят? Нажми нужную чашу.", Modifier.fillMaxWidth().padding(13.dp), fontSize = 15.sp, color = Color(0xFF654712), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        dragging?.let { card ->
            val halfWidth = with(density) { 53.dp.toPx() }
            val halfHeight = with(density) { 41.dp.toPx() }
            Surface(
                Modifier.offset { IntOffset((pointer.x - rootOrigin.x - halfWidth).roundToInt(), (pointer.y - rootOrigin.y - halfHeight).roundToInt()) }
                    .width(106.dp).height(82.dp).zIndex(10f),
                shape = RoundedCornerShape(17.dp), color = Color.White,
                border = BorderStroke(3.dp, Color(0xFFFFBE42)), shadowElevation = 17.dp
            ) { ScaleCardFace(card) }
        }
    }
}

@Composable
private fun ScaleStat(symbol: String, label: String, amount: Int, background: Color, modifier: Modifier) {
    Surface(modifier.height(64.dp), shape = RoundedCornerShape(17.dp), color = background, shadowElevation = 3.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("$symbol $label", fontSize = 12.sp, color = Color(0xFF4D5E7A), fontWeight = FontWeight.Bold)
            Text("$amount", fontSize = 20.sp, color = Color(0xFF263A65), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun ScalePan(title: String, total: Int, sign: String, active: Boolean, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(modifier.height(82.dp).clickable(onClick = onClick), shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 35.dp, bottomEnd = 35.dp), color = tint,
        border = BorderStroke(3.dp, if (active) Color(0xFFFFB72E) else Color(0xFFAF864D)), shadowElevation = 6.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("$sign $title", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF243C64))
            Text("$total монет", fontSize = 17.sp, fontWeight = FontWeight.Black, color = Color(0xFF243C64))
        }
    }
}

@Composable
private fun ScaleCardFace(card: MoneyCard) {
    val symbol = when (card.id) {
        "work" -> "💼"; "reward" -> "🏆"; "gift" -> "🎁"; "food" -> "🍲"
        "rent" -> "🏠"; "transport" -> "🚌"; "medicine" -> "💊"
        "toy" -> "★"; "sweet" -> "🍬"; else -> "★"
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
        Text("$symbol  ${card.amount}", fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color(0xFF263A65), maxLines = 1)
        Text(card.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF263A65), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
