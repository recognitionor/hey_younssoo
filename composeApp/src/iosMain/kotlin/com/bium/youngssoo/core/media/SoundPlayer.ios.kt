@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.bium.youngssoo.core.media

import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.Foundation.NSURL
import platform.Foundation.NSBundle
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

actual fun createSoundPlayer(): SoundPlayer = IosSoundPlayer()

class IosSoundPlayer : SoundPlayer {
    private var correctPlayer: AVAudioPlayer? = null
    private var incorrectPlayer: AVAudioPlayer? = null

    init {
        val audioSession = AVAudioSession.sharedInstance()
        audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
    }

    override fun playSound(soundType: SoundType) {
        try {
            when (soundType) {
                SoundType.CORRECT -> {
                    val path = NSBundle.mainBundle.pathForResource("correct", "mp3")
                    if (path != null) {
                        val url = NSURL.fileURLWithPath(path)
                        correctPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
                        correctPlayer?.play()
                    } else {
                        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate.toUInt())
                    }
                }
                SoundType.INCORRECT -> {
                    val path = NSBundle.mainBundle.pathForResource("incorrect", "mp3")
                    if (path != null) {
                        val url = NSURL.fileURLWithPath(path)
                        incorrectPlayer = AVAudioPlayer(contentsOfURL = url, error = null)
                        incorrectPlayer?.play()
                    } else {
                        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate.toUInt())
                    }
                }
                SoundType.BUTTON_CLICK -> {
                    AudioServicesPlaySystemSound(kSystemSoundID_Vibrate.toUInt())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
        correctPlayer?.stop()
        incorrectPlayer?.stop()
    }
}
