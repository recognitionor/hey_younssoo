package com.bium.youngssoo.core.data

expect fun speakTextToSpeech(text: String)
expect fun isTextToSpeechSpeaking(): Boolean
expect fun stopTextToSpeech()
