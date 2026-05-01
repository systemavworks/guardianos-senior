package es.guardianos.senior.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

object CompanionSpeaker {
    private var tts: TextToSpeech? = null
    private var ready = false

    fun initialize(context: Context) {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "ES")
                tts?.setSpeechRate(0.85f)
                ready = true
            }
        }
    }

    fun speak(text: String) {
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, "guardianos_senior_tts")
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }
}
