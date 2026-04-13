package com.bium.youngssoo.core.media

enum class SoundType {
    CORRECT,
    INCORRECT,
    BUTTON_CLICK
}

interface SoundPlayer {
    fun playSound(soundType: SoundType)
    fun release()
}

expect fun createSoundPlayer(): SoundPlayer
