package com.bium.youngssoo.core.data

import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.NaturalLanguage.NLLanguageRecognizer
import platform.darwin.NSObject

class TextToSpeechManager : NSObject(), AVSpeechSynthesizerDelegateProtocol {
    private var synthesizer: AVSpeechSynthesizer = AVSpeechSynthesizer()
    var isSpeaking = false

    init {
        synthesizer.delegate = this
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        isSpeaking = true
        synthesizer.delegate = this

        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("en-US") // default to english

        // Detect language
        val recognizer = NLLanguageRecognizer()
        recognizer.processString(text)
        val language = recognizer.dominantLanguage
        language?.let {
            utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(it)
        }

        synthesizer.speakUtterance(utterance)
    }

    override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        didFinishSpeechUtterance: AVSpeechUtterance
    ) {
        isSpeaking = false
    }

    fun stopSpeaking() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        isSpeaking = false
    }

    fun isSpeakingNow(): Boolean {
        return isSpeaking || synthesizer.isSpeaking()
    }
}

var textToSpeechManager: TextToSpeechManager = TextToSpeechManager()

actual fun speakTextToSpeech(text: String) {
    textToSpeechManager.speak(text)
}

actual fun stopTextToSpeech() {
    textToSpeechManager.stopSpeaking()
}

actual fun isTextToSpeechSpeaking(): Boolean {
    return textToSpeechManager.isSpeakingNow()
}
