package com.finny.pet.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.finny.pet.R
import com.finny.pet.ui.screens.VisualPrefs

/** Short, locally bundled effects. Playback never needs a network connection. */
enum class FinnySfx(val resource: Int, val volume: Float) {
    TAP(R.raw.sfx_tap, .32f),
    NAVIGATE(R.raw.sfx_navigate, .32f),
    PLACE(R.raw.sfx_place, .38f),
    COIN(R.raw.sfx_coin, .38f),
    WARNING(R.raw.sfx_warning, .28f),
    PET(R.raw.sfx_pet, .30f),
    SUCCESS(R.raw.sfx_success, .38f),
    LEVEL_UP(R.raw.sfx_level_up, .38f),
}

object FinnyAudio {
    private var pool: SoundPool? = null
    private val samples = mutableMapOf<FinnySfx, Int>()
    private val loaded = mutableSetOf<Int>()

    @Synchronized
    fun initialize(context: Context) {
        if (pool != null) return
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val soundPool = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attributes).build()
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) synchronized(this) { loaded.add(sampleId) }
        }
        FinnySfx.entries.forEach { effect ->
            samples[effect] = soundPool.load(context.applicationContext, effect.resource, 1)
        }
        pool = soundPool
    }

    fun play(effect: FinnySfx) {
        if (!VisualPrefs.soundEnabled.value) return
        val soundPool: SoundPool
        val sampleId: Int
        synchronized(this) {
            soundPool = pool ?: return
            sampleId = samples[effect] ?: return
            if (sampleId !in loaded) return
        }
        soundPool.play(sampleId, effect.volume, effect.volume, 1, 0, 1f)
    }
}
