package com.bium.youngssoo.core.data

import android.speech.tts.TextToSpeech

var androidTextToSpeech: TextToSpeech? = null
var unspokenText: String? = null

actual fun speakTextToSpeech(text: String) {
    androidTextToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "youngssoo_tts_id")
}

actual fun isTextToSpeechSpeaking(): Boolean {
    return androidTextToSpeech?.isSpeaking ?: false
}

actual fun stopTextToSpeech() {
    androidTextToSpeech?.stop()
    unspokenText = ""
}
