package com.bium.youngssoo.core.media

import android.media.ToneGenerator
import android.media.AudioManager

actual fun createSoundPlayer(): SoundPlayer = AndroidSoundPlayer()

class AndroidSoundPlayer : SoundPlayer {
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME)

    override fun playSound(soundType: SoundType) {
        try {
            when (soundType) {
                SoundType.CORRECT -> {
                    // 정답 음 - 높은 음 (SUCCESS tone)
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
                }
                SoundType.INCORRECT -> {
                    // 오답 음 - 낮은 음 (ERROR tone)
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK, 200)
                }
                SoundType.BUTTON_CLICK -> {
                    // 버튼 클릭음
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        toneGenerator.release()
    }
}
