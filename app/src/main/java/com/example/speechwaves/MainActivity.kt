package com.example.speechwaves

import android.app.Activity
import android.os.Bundle
import android.os.Handler

class MainActivity : Activity() {

    private lateinit var speechWaves: SpeechWaves

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        speechWaves = findViewById(R.id.speech_waves)
        speechWaves.start()
    }
}
