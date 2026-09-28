package com.finny.pet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.finny.pet.navigation.FinnyNavHost
import com.finny.pet.data.repository.ProgressRepository
import com.finny.pet.ui.screens.VisualPrefs
import com.finny.pet.ui.theme.FinnyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var progress: ProgressRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            VisualPrefs.soundEnabled.value = (progress.get("settings_sound")?.value ?: 1) == 1
            VisualPrefs.animationsEnabled.value = (progress.get("settings_animation")?.value ?: 1) == 1
        }
        setContent {
            FinnyTheme {
                BoxWithConstraints(Modifier.fillMaxSize().background(Color(0xFF203D75))) {
                    val gameWidth = maxWidth.coerceAtMost(480.dp)
                    Box(Modifier.width(gameWidth).fillMaxHeight().align(Alignment.Center)) {
                        FinnyNavHost()
                    }
                }
            }
        }
    }
}
