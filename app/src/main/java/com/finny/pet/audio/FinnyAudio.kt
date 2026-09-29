package com.finny.pet.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.util.Log
import com.finny.pet.R
import com.finny.pet.ui.screens.VisualPrefs

/** Short, locally bundled effects. Playback never needs a network connection. */
enum class FinnySfx(val resource: Int, val volume: Float) {
    TAP(R.raw.sfx_tap, .40f),
    NAVIGATE(R.raw.sfx_navigate, .44f),
    PLACE(R.raw.sfx_place, .52f),
    COIN(R.raw.sfx_coin, .56f),
    WARNING(R.raw.sfx_warning, .46f),
    PET(R.raw.sfx_pet, .48f),
    SUCCESS(R.raw.sfx_success, .60f),
    LEVEL_UP(R.raw.sfx_level_up, .62f),
}

object FinnyAudio {
    private var pool: SoundPool? = null
    private var appContext: Context? = null
    private val samples = mutableMapOf<FinnySfx, Int>()
    private val loaded = mutableSetOf<Int>()
    private val pending = mutableSetOf<FinnySfx>()
    private val lastPlayedAt = mutableMapOf<FinnySfx, Long>()

    @Synchronized
    fun initialize(context: Context) {
        if (pool != null) return
        appContext = context.applicationContext
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val soundPool = SoundPool.Builder().setMaxStreams(8).setAudioAttributes(attributes).build()
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            val queued = synchronized(this) {
                if (status == 0) loaded.add(sampleId)
                else Log.w("FinnyAudio", "SoundPool could not load sample $sampleId: $status")
                samples.entries.firstOrNull { it.value == sampleId }?.key?.takeIf { pending.remove(it) }
            }
            if (status == 0 && queued != null) play(queued)
        }
        pool = soundPool
        FinnySfx.entries.forEach { effect ->
            samples[effect] = soundPool.load(context.applicationContext, effect.resource, 1)
        }
    }

    fun play(effect: FinnySfx) {
        if (!VisualPrefs.soundEnabled.value) return
        val now = android.os.SystemClock.elapsedRealtime()
        val soundPool: SoundPool
        val sampleId: Int
        synchronized(this) {
            soundPool = pool ?: return
            sampleId = samples[effect] ?: return
            if (sampleId !in loaded) { pending.add(effect); return }
            if (now - (lastPlayedAt[effect] ?: 0L) < 90L) return
            lastPlayedAt[effect] = now
        }
        if (soundPool.play(sampleId, effect.volume, effect.volume, 1, 0, 1f) == 0) {
            Log.w("FinnyAudio", "SoundPool playback failed for $effect; trying MediaPlayer")
            playPreview(effect)
        }
    }

    /** Explicit test sound; MediaPlayer uses the device's multimedia volume. */
    fun playPreview(effect: FinnySfx = FinnySfx.SUCCESS): Boolean {
        if (!VisualPrefs.soundEnabled.value) return false
        val context = appContext ?: return false
        return try {
            val player = MediaPlayer.create(context, effect.resource) ?: return false
            player.setVolume(1f, 1f)
            player.setOnCompletionListener { it.release() }
            player.setOnErrorListener { broken, _, _ -> broken.release(); true }
            player.start()
            true
        } catch (error: Exception) {
            Log.e("FinnyAudio", "Preview playback failed", error)
            false
        }
    }
}
