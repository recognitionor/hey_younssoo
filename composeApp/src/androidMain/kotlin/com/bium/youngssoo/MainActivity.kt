package com.bium.youngssoo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import android.speech.tts.TextToSpeech
import com.bium.youngssoo.core.data.androidTextToSpeech

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private val requestNotif = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> /* 필요시 처리 */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initPlatformContext(this)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotif.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        androidTextToSpeech = TextToSpeech(this, this)
        
        setContent {
            App()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            androidTextToSpeech?.language = java.util.Locale.US
            androidTextToSpeech?.speak("", TextToSpeech.QUEUE_FLUSH, null, "warmup")
        }
    }

    override fun onDestroy() {
        androidTextToSpeech?.stop()
        androidTextToSpeech?.shutdown()
        androidTextToSpeech = null
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}