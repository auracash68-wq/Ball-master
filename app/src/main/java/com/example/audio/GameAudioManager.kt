package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Self-contained procedural PCM game audio synthesizer for Ball Master.
 * Produces crisp wooden clicks, ceramic cup slides, bright marimba chimes,
 * celebratory coin jingles, and comical game-over tones with zero external asset risk.
 */
class GameAudioManager {

    @Volatile
    var soundEnabled: Boolean = true

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val sampleRate = 22050

    // Pre-synthesized PCM buffers so playback has minimal latency & zero GC churn
    private val clickPcm by lazy { synthesizeWoodClick() }
    private val lockedPcm by lazy { synthesizeLockedThud() }
    private val cupSlidePcm by lazy { synthesizeCupWhoosh() }
    private val cupLiftPcm by lazy { synthesizeCupLift() }
    private val correctPcm by lazy { synthesizeCorrectChime() }
    private val coinPcm by lazy { synthesizeCoinJingle() }
    private val levelCompletePcm by lazy { synthesizeLevelCompleteFanfare() }
    private val wrongPcm by lazy { synthesizeWrongTone() }
    private val gameOverPcm by lazy { synthesizeGameOverTone() }

    fun playButtonClick() = playPcm(clickPcm, 0.55f)
    fun playLockedClick() = playPcm(lockedPcm, 0.45f)
    fun playCupMove() = playPcm(cupSlidePcm, 0.40f)
    fun playCupLift() = playPcm(cupLiftPcm, 0.50f)
    fun playCorrectAnswer() = playPcm(correctPcm, 0.65f)
    fun playCoinReward() = playPcm(coinPcm, 0.65f)
    fun playLevelCompleted() = playPcm(levelCompletePcm, 0.72f)
    fun playWrongAnswer() = playPcm(wrongPcm, 0.60f)
    fun playGameOver() = playPcm(gameOverPcm, 0.65f)

    fun release() {
        scope.cancel()
    }

    private fun playPcm(samples: ShortArray, volume: Float) {
        if (!soundEnabled || samples.isEmpty()) return
        scope.launch {
            var track: AudioTrack? = null
            try {
                val byteLength = samples.size * 2
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(byteLength)
                    .build()

                track.setVolume(volume)
                track.write(samples, 0, samples.size)
                track.play()

                val durationMs = (samples.size * 1000L / sampleRate) + 45L
                kotlinx.coroutines.delay(durationMs)
            } catch (_: Exception) {
                // Safe fallback if audio hardware is unavailable in headless/test environments
            } finally {
                try {
                    track?.stop()
                    track?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun synthesizeWoodClick(): ShortArray {
        val durationSec = 0.065
        val count = (sampleRate * durationSec).toInt()
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 58.0)
            val freq = 620.0 + 380.0 * exp(-t * 90.0)
            val s = sin(2.0 * PI * freq * t) * 0.75 + sin(2.0 * PI * freq * 2.1 * t) * 0.25
            (s * env * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
    }

    private fun synthesizeLockedThud(): ShortArray {
        val durationSec = 0.11
        val count = (sampleRate * durationSec).toInt()
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 34.0)
            val s = sin(2.0 * PI * 190.0 * t) * 0.7 + sin(2.0 * PI * 135.0 * t) * 0.3
            (s * env * 22000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
    }

    private fun synthesizeCupWhoosh(): ShortArray {
        val durationSec = 0.13
        val count = (sampleRate * durationSec).toInt()
        var seed = 1337
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            val norm = t / durationSec
            val env = sin(PI * norm) * exp(-norm * 0.6)
            // Soft ceramic slide resonance + subtle filtered brush noise
            seed = (seed * 1103515245 + 12345) and 0x7fffffff
            val noise = (seed.toDouble() / 0x7fffffff.toDouble()) * 2.0 - 1.0
            val tone = sin(2.0 * PI * (240.0 + 180.0 * sin(PI * norm)) * t)
            val mix = tone * 0.55 + noise * 0.22
            (mix * env * 16500.0).toInt().coerceIn(-32767, 32767).toShort()
        }
    }

    private fun synthesizeCupLift(): ShortArray {
        val durationSec = 0.14
        val count = (sampleRate * durationSec).toInt()
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            val norm = t / durationSec
            val env = exp(-t * 20.0)
            val freq = 340.0 + 340.0 * norm
            val s = sin(2.0 * PI * freq * t)
            (s * env * 20000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
    }

    private fun synthesizeCorrectChime(): ShortArray {
        val notes = listOf(523.25 to 0.0, 659.25 to 0.075, 783.99 to 0.15, 1046.50 to 0.225)
        return renderNotes(notes, totalSec = 0.55, decay = 9.5)
    }

    private fun synthesizeCoinJingle(): ShortArray {
        val notes = listOf(987.77 to 0.0, 1318.51 to 0.075, 1567.98 to 0.15)
        return renderNotes(notes, totalSec = 0.45, decay = 11.0)
    }

    private fun synthesizeLevelCompleteFanfare(): ShortArray {
        val notes = listOf(
            523.25 to 0.0,
            659.25 to 0.09,
            783.99 to 0.18,
            1046.50 to 0.28,
            783.99 to 0.39,
            1046.50 to 0.48
        )
        return renderNotes(notes, totalSec = 0.92, decay = 6.2)
    }

    private fun synthesizeWrongTone(): ShortArray {
        val notes = listOf(311.13 to 0.0, 233.08 to 0.12)
        return renderNotes(notes, totalSec = 0.42, decay = 8.0)
    }

    private fun synthesizeGameOverTone(): ShortArray {
        val notes = listOf(
            392.00 to 0.0,
            349.23 to 0.16,
            311.13 to 0.32,
            261.63 to 0.48
        )
        return renderNotes(notes, totalSec = 0.95, decay = 4.8)
    }

    private fun renderNotes(
        notes: List<Pair<Double, Double>>,
        totalSec: Double,
        decay: Double
    ): ShortArray {
        val count = (sampleRate * totalSec).toInt()
        return ShortArray(count) { i ->
            val t = i.toDouble() / sampleRate
            var acc = 0.0
            for ((freq, start) in notes) {
                val dt = t - start
                if (dt >= 0.0) {
                    val attack = (dt / 0.008).coerceAtMost(1.0)
                    val env = attack * exp(-dt * decay)
                    val marimba = sin(2.0 * PI * freq * dt) * 0.72 +
                        sin(2.0 * PI * freq * 2.0 * dt) * 0.20 +
                        sin(2.0 * PI * freq * 3.0 * dt) * 0.08
                    acc += marimba * env
                }
            }
            (acc.coerceIn(-1.0, 1.0) * 24000.0).toInt().coerceIn(-32767, 32767).toShort()
        }
    }
}
