package com.finny.pet.ui.games

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finny.pet.ui.components.FinnyCard
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private data class BoardPoint(val x: Int, val y: Int)
private enum class SnakeHeading(val dx: Int, val dy: Int) { UP(0,-1), DOWN(0,1), LEFT(-1,0), RIGHT(1,0) }
private fun reversed(a: SnakeHeading, b: SnakeHeading) = a.dx == -b.dx && a.dy == -b.dy

@Composable
fun SnakeGame(done: () -> Unit) {
    val columns = 12
    val rows = 15
    var body by remember { mutableStateOf(listOf(BoardPoint(5, 7), BoardPoint(4, 7), BoardPoint(3, 7))) }
    var heading by remember { mutableStateOf(SnakeHeading.RIGHT) }
    var nextHeading by remember { mutableStateOf(SnakeHeading.RIGHT) }
    var fruit by remember { mutableStateOf(BoardPoint(9, 7)) }
    var collected by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(true) }
    var crashed by remember { mutableStateOf(false) }
    var generation by remember { mutableIntStateOf(0) }

    fun reset() {
        body = listOf(BoardPoint(5, 7), BoardPoint(4, 7), BoardPoint(3, 7))
        heading = SnakeHeading.RIGHT
        nextHeading = SnakeHeading.RIGHT
        fruit = BoardPoint(9, 7)
        collected = 0
        crashed = false
        running = true
        generation++
    }
    fun steer(to: SnakeHeading) {
        if (running && !reversed(heading, to)) nextHeading = to
    }
    fun step() {
        heading = nextHeading
        val head = body.first()
        val next = BoardPoint(head.x + heading.dx, head.y + heading.dy)
        val eats = next == fruit
        val collisionBody = if (eats) body else body.dropLast(1)
        if (next.x !in 0 until columns || next.y !in 0 until rows || next in collisionBody) {
            running = false
            crashed = true
            return
        }
        body = listOf(next) + if (eats) body else body.dropLast(1)
        if (eats) {
            collected++
            if (collected >= 4) {
                running = false
                done()
            } else {
                val free = (0 until rows).flatMap { y -> (0 until columns).map { x -> BoardPoint(x, y) } }.filterNot { it in body }
                fruit = free.random()
            }
        }
    }
    val latestStep by rememberUpdatedState(::step)
    LaunchedEffect(running, generation) {
        while (running) {
            delay((420 - collected * 38L).coerceAtLeast(260))
            if (running) latestStep()
        }
    }

    FinnyCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Змейка", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF25436D))
                Text("Яблоки: $collected из 4", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF218350))
            }
            Text("🍎", fontSize = 32.sp)
        }
        Text(
            if (crashed) "Змейка коснулась края или хвоста. Нажми «Заново» и попробуй другой путь."
            else if (running) "Веди змейку свайпом по полю или стрелками. Собери 4 яблока, не касаясь краёв и хвоста."
            else "Пауза. Нажми «Продолжить».",
            fontSize = 15.sp, lineHeight = 19.sp, color = Color(0xFF465B7A)
        )
        var swipe by remember { mutableStateOf(Offset.Zero) }
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val cell = ((maxWidth - 8.dp) / columns).coerceAtMost(29.dp)
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF143A62), border = BorderStroke(3.dp, Color(0xFF78BFF2)), shadowElevation = 7.dp) {
                Canvas(
                    Modifier.width(cell * columns).height(cell * rows)
                        .pointerInput(running, heading) {
                            detectDragGestures(
                                onDragStart = { swipe = Offset.Zero },
                                onDrag = { change, amount -> change.consume(); swipe += amount },
                                onDragEnd = {
                                    if (abs(swipe.x) > abs(swipe.y)) steer(if (swipe.x > 0) SnakeHeading.RIGHT else SnakeHeading.LEFT)
                                    else if (abs(swipe.y) > 8f) steer(if (swipe.y > 0) SnakeHeading.DOWN else SnakeHeading.UP)
                                }
                            )
                        }
                ) {
                    val c = size.width / columns
                    for (y in 0 until rows) for (x in 0 until columns) {
                        drawRoundRect(if ((x + y) % 2 == 0) Color(0xFF1D5278) else Color(0xFF235B80),
                            Offset(x * c + 1f, y * c + 1f), Size(c - 2f, c - 2f), CornerRadius(c * .12f))
                    }
                    drawCircle(Color(0xFFEF4D62), c * .35f, Offset((fruit.x + .5f) * c, (fruit.y + .52f) * c))
                    drawCircle(Color(0xFF8EEA74), c * .11f, Offset((fruit.x + .69f) * c, (fruit.y + .23f) * c))
                    body.forEachIndexed { index, part ->
                        val center = Offset((part.x + .5f) * c, (part.y + .5f) * c)
                        drawRoundRect(if (index == 0) Color(0xFFFFD257) else Color(0xFF65DB93),
                            Offset(part.x * c + 2f, part.y * c + 2f), Size(c - 4f, c - 4f), CornerRadius(c * .35f))
                        if (index == 0) {
                            drawCircle(Color(0xFF253A5B), c * .055f, center + Offset(-c * .13f, -c * .1f))
                            drawCircle(Color(0xFF253A5B), c * .055f, center + Offset(c * .13f, -c * .1f))
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ArcadeButton("←", Modifier.weight(1f)) { steer(SnakeHeading.LEFT) }
            ArcadeButton("↑", Modifier.weight(1f)) { steer(SnakeHeading.UP) }
            ArcadeButton("↓", Modifier.weight(1f)) { steer(SnakeHeading.DOWN) }
            ArcadeButton("→", Modifier.weight(1f)) { steer(SnakeHeading.RIGHT) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (crashed) reset() else running = !running }, modifier = Modifier.weight(1f).heightIn(min = 50.dp)) {
                Text(if (crashed) "Заново" else if (running) "Пауза" else "Продолжить")
            }
            OutlinedButton(onClick = ::reset, modifier = Modifier.weight(1f).heightIn(min = 50.dp)) { Text("Новая игра") }
        }
    }
}

private data class BlockPoint(val x: Int, val y: Int)
private data class FallingBlock(val kind: Int, val rotation: Int = 0, val x: Int = 3, val y: Int = 0)
private val shapes = listOf(
    listOf(BlockPoint(0,0),BlockPoint(1,0),BlockPoint(2,0),BlockPoint(3,0)),
    listOf(BlockPoint(0,0),BlockPoint(1,0),BlockPoint(0,1),BlockPoint(1,1)),
    listOf(BlockPoint(0,0),BlockPoint(1,0),BlockPoint(2,0),BlockPoint(1,1)),
    listOf(BlockPoint(0,0),BlockPoint(0,1),BlockPoint(1,1),BlockPoint(2,1)),
    listOf(BlockPoint(2,0),BlockPoint(0,1),BlockPoint(1,1),BlockPoint(2,1)),
    listOf(BlockPoint(1,0),BlockPoint(2,0),BlockPoint(0,1),BlockPoint(1,1)),
    listOf(BlockPoint(0,0),BlockPoint(1,0),BlockPoint(1,1),BlockPoint(2,1))
)
private val blockColors = listOf(Color(0xFF50C5F3),Color(0xFFFFCD54),Color(0xFFBE7EF1),Color(0xFF578DF1),Color(0xFFFF8C63),Color(0xFF5DDC9A),Color(0xFFF8759A))
private fun rotatedBlocks(block: FallingBlock): List<BlockPoint> {
    var points = shapes[block.kind]
    repeat(block.rotation % 4) {
        val turned = points.map { BlockPoint(-it.y, it.x) }
        val minX = turned.minOf { it.x }; val minY = turned.minOf { it.y }
        points = turned.map { BlockPoint(it.x - minX, it.y - minY) }
    }
    return points.map { BlockPoint(it.x + block.x, it.y + block.y) }
}

@Composable
fun TetrisGame(done: () -> Unit) {
    val columns = 10; val rows = 14
    var locked by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var active by remember { mutableStateOf(FallingBlock(Random.nextInt(shapes.size))) }
    var next by remember { mutableIntStateOf(Random.nextInt(shapes.size)) }
    var lines by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(true) }
    var over by remember { mutableStateOf(false) }
    var generation by remember { mutableIntStateOf(0) }
    fun fits(block: FallingBlock) = rotatedBlocks(block).all { it.x in 0 until columns && it.y in 0 until rows && it.y * columns + it.x !in locked }
    fun reset() { locked = emptyMap(); active = FallingBlock(Random.nextInt(shapes.size)); next = Random.nextInt(shapes.size); lines = 0; over = false; running = true; generation++ }
    fun settle() {
        val filled = locked.toMutableMap()
        rotatedBlocks(active).forEach { filled[it.y * columns + it.x] = active.kind }
        val clear = (0 until rows).filter { y -> (0 until columns).all { y * columns + it in filled } }
        val rebuilt = mutableMapOf<Int, Int>()
        filled.forEach { (index, kind) ->
            val x = index % columns; val y = index / columns
            if (y !in clear) rebuilt[(y + clear.count { it > y }) * columns + x] = kind
        }
        locked = rebuilt
        lines += clear.size
        if (lines >= 2) { running = false; done(); return }
        active = FallingBlock(next)
        next = Random.nextInt(shapes.size)
        if (!fits(active)) { running = false; over = true }
    }
    fun descend() { if (!running) return; val moved = active.copy(y = active.y + 1); if (fits(moved)) active = moved else settle() }
    fun move(dx: Int) { if (running) active.copy(x = active.x + dx).takeIf(::fits)?.let { active = it } }
    fun rotate() { if (running) { val turned = active.copy(rotation = (active.rotation + 1) % 4); listOf(turned, turned.copy(x = turned.x - 1), turned.copy(x = turned.x + 1)).firstOrNull(::fits)?.let { active = it } } }
    fun drop() { if (!running) return; while (fits(active.copy(y = active.y + 1))) active = active.copy(y = active.y + 1); settle() }
    val latestDescend by rememberUpdatedState(::descend)
    LaunchedEffect(running, generation, lines) {
        while (running) { delay((700 - lines * 45L).coerceAtLeast(420)); if (running) latestDescend() }
    }
    val visible = locked + if (running) rotatedBlocks(active).associate { it.y * columns + it.x to active.kind } else emptyMap()
    FinnyCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Block Blast", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF25436D))
                Text("Готовые линии: $lines из 2", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3475B9))
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFE7EEFF)) {
                Text("Следующая ■", Modifier.padding(8.dp), fontWeight = FontWeight.Bold, color = blockColors[next])
            }
        }
        Text(if (over) "Поле заполнилось. Нажми «Заново» и освобождай ряды." else "Передвигай и поворачивай падающие фигуры. Заполни целиком два горизонтальных ряда.", fontSize = 15.sp, lineHeight = 19.sp, color = Color(0xFF465B7A))
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val cell = ((maxWidth - 8.dp) / columns).coerceAtMost(29.dp)
            Surface(shape = RoundedCornerShape(19.dp), color = Color(0xFF132445), border = BorderStroke(3.dp, Color(0xFF69B7F4))) {
                Canvas(Modifier.width(cell * columns).height(cell * rows)) {
                    val c = size.width / columns
                    for (y in 0 until rows) for (x in 0 until columns) {
                        val kind = visible[y * columns + x]
                        drawRoundRect(kind?.let { blockColors[it] } ?: Color(0xFF213B68),
                            Offset(x * c + 1.5f, y * c + 1.5f), Size(c - 3f, c - 3f), CornerRadius(c * .17f))
                        if (kind != null) drawRoundRect(Color.White.copy(alpha = .25f),
                            Offset(x * c + 4f, y * c + 4f), Size(c - 8f, c * .13f), CornerRadius(c * .08f))
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ArcadeButton("←", Modifier.weight(1f)) { move(-1) }
            ArcadeButton("↻", Modifier.weight(1f)) { rotate() }
            ArcadeButton("→", Modifier.weight(1f)) { move(1) }
            ArcadeButton("↓", Modifier.weight(1f)) { drop() }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (over) reset() else running = !running }, modifier = Modifier.weight(1f).heightIn(min = 50.dp)) {
                Text(if (over) "Заново" else if (running) "Пауза" else "Продолжить")
            }
            OutlinedButton(onClick = ::reset, modifier = Modifier.weight(1f).heightIn(min = 50.dp)) { Text("Новая игра") }
        }
    }
}

@Composable
private fun ArcadeButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = 52.dp), shape = RoundedCornerShape(15.dp), contentPadding = PaddingValues(0.dp)) {
        Text(label, fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}
