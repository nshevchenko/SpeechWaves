package com.example.speechwaves

import android.media.MediaRecorder
import android.os.Handler
import java.io.IOException

class AudioMeter {

    private val mediaRecorder: MediaRecorder = MediaRecorder()
    private val handler = Handler()

    var callback: GetAmplitudeCallback? = null

    fun start(callback: GetAmplitudeCallback) {
        this.callback = callback
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile("/dev/null")

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        startLoop()
    }

    private fun startLoop() {
        handler.post(object : Runnable {
            override fun run() {
                callback?.onAmplitudeUpdate(this@AudioMeter.getAmplitude())
                handler.postDelayed(this, 50)
            }
        })
    }

    fun getAmplitude(): Int {
        return mediaRecorder.maxAmplitude;
    }
}

interface GetAmplitudeCallback {
    fun onAmplitudeUpdate(amplitude: Int)
}
