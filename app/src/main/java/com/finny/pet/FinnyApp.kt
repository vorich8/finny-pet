package com.finny.pet

import android.app.Application
import com.finny.pet.audio.FinnyAudio
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FinnyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FinnyAudio.initialize(this)
    }
}
