package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class PspSoundEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrate(durationMs: Long = 25, amplitude: Int = 180) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255)))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Ignore if vibrator fails
        }
    }

    /**
     * Plays the authentic PSP multi-toned crystalline startup chime chord.
     * Ascending harmonics and warm resonant tail.
     */
    fun playBootChime() {
        scope.launch {
            try {
                val sampleRate = 44100
                val durationSeconds = 3.2
                val numSamples = (sampleRate * durationSeconds).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    
                    // Envelope with warm attack and long shimmering tail
                    val env = when {
                        t < 0.2 -> (t / 0.2)
                        t < 1.0 -> 1.0
                        else -> ((durationSeconds - t) / (durationSeconds - 1.0)).coerceAtLeast(0.0)
                    }

                    // Frequencies of classic PSP chord: E4 (329.63Hz), B4 (493.88Hz), E5 (659.25Hz), G#5 (830.61Hz), B5 (987.77Hz)
                    val tone1 = sin(2.0 * PI * 329.63 * t) * 0.25
                    val tone2 = sin(2.0 * PI * 493.88 * t) * 0.22
                    val tone3 = sin(2.0 * PI * 659.25 * t) * 0.20
                    val tone4 = sin(2.0 * PI * 830.61 * t) * 0.18
                    val toneSweep = if (t < 0.6) {
                        sin(2.0 * PI * (1200.0 + 800.0 * (t / 0.6)) * t) * 0.15 * (1.0 - t / 0.6)
                    } else 0.0

                    val sampleVal = (tone1 + tone2 + tone3 + tone4 + toneSweep) * env * Short.MAX_VALUE * 0.8
                    buffer[i] = sampleVal.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcm(buffer, sampleRate)
            } catch (_: Exception) {
                // Audio fallback safe
            }
        }
    }

    /**
     * Authentic PSP XMB cursor navigation click (very short, crisp high tick).
     */
    fun playClick() {
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * 0.025).toInt() // 25ms
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = 1.0 - (i.toDouble() / numSamples)
                    val freq = 2200.0 - (t * 8000.0)
                    val sample = sin(2.0 * PI * freq * t) * env * Short.MAX_VALUE * 0.5
                    buffer[i] = sample.toInt().toShort()
                }

                playPcm(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * Authentic PSP confirmation chime (ascending two-tone chime).
     */
    fun playConfirm() {
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * 0.18).toInt() // 180ms
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = if (t < 0.08) 880.0 else 1318.51 // A5 -> E6
                    val env = (1.0 - (i.toDouble() / numSamples))
                    val sample = sin(2.0 * PI * freq * t) * env * Short.MAX_VALUE * 0.55
                    buffer[i] = sample.toInt().toShort()
                }

                playPcm(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * PSP Back/Cancel sound.
     */
    fun playCancel() {
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * 0.12).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 587.33 - (t * 1200.0).coerceAtLeast(0.0) // D5 down
                    val env = 1.0 - (i.toDouble() / numSamples)
                    val sample = sin(2.0 * PI * freq * t) * env * Short.MAX_VALUE * 0.45
                    buffer[i] = sample.toInt().toShort()
                }

                playPcm(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * Play game sword slash SFX.
     */
    fun playSlash() {
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * 0.15).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - (i.toDouble() / numSamples))
                    val noise = (Math.random() * 2.0 - 1.0) * 0.3
                    val freq = 1800.0 - (t * 6000.0)
                    val sample = (sin(2.0 * PI * freq * t) * 0.7 + noise) * env * Short.MAX_VALUE * 0.6
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcm(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    /**
     * Play game nitro / boost SFX.
     */
    fun playBoost() {
        scope.launch {
            try {
                val sampleRate = 44100
                val numSamples = (sampleRate * 0.35).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = (1.0 - (i.toDouble() / numSamples))
                    val freq = 400.0 + (t * 1200.0)
                    val noise = (Math.random() * 2.0 - 1.0) * 0.4
                    val sample = (sin(2.0 * PI * freq * t) * 0.6 + noise) * env * Short.MAX_VALUE * 0.65
                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                playPcm(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    private fun playPcm(pcm: ShortArray, sampleRate: Int) {
        val audioTrack = AudioTrack.Builder()
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
            .setBufferSizeInBytes(pcm.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(pcm, 0, pcm.size)
        audioTrack.play()

        // Release track after completion
        scope.launch {
            kotlinx.coroutines.delay((pcm.size.toDouble() / sampleRate * 1000).toLong() + 100)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
