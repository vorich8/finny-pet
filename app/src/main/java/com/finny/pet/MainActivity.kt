package com.finny.pet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.finny.pet.navigation.FinnyNavHost
import com.finny.pet.ui.theme.FinnyTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { FinnyTheme { FinnyNavHost() } }
    }
}
