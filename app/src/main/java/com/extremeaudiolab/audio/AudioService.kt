package com.extremeaudiolab.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder

/** Serviço em primeiro plano que mantém a thread de áudio viva. */
class AudioService : Service() {
    private val engine = NativeEngine()
    private var handle = 0L
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun service(): AudioService = this@AudioService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val sampleRate = 48_000
        val frames = 192
        handle = engine.nativeCreate(sampleRate, frames)
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    fun start(): Boolean = engine.nativeStart(handle)

    fun stop() = engine.nativeStop(handle)

    fun setParam(module: String, param: String, value: Float) =
        engine.nativeSetParam(handle, module, param, value)

    fun setStep(i: Int, on: Boolean, voice: Int, pitch: Float, velocity: Float, probability: Float) =
        engine.nativeSetStep(handle, i, on, voice, pitch, velocity, probability)

    fun setTempo(bpm: Float, swing: Float) = engine.nativeSetTempo(handle, bpm, swing)

    fun setKaoss(program: Int, x: Float, y: Float, latch: Boolean, mix: Float) =
        engine.nativeSetKaoss(handle, program, x, y, latch, mix)

    fun triggerGlitch() = engine.nativeTriggerGlitch(handle)
    fun triggerGranular() = engine.nativeTriggerGranular(handle)

    fun startRecording(path: String): Boolean = engine.nativeStartRecording(handle, path)
    fun stopRecording(): String? = engine.nativeStopRecording(handle)
    fun setStream(host: String, port: Int): Boolean = engine.nativeSetStream(handle, host, port)
    fun stopStream() = engine.nativeStopStream(handle)
    fun meter(): FloatArray = engine.nativeMeter(handle)

    override fun onDestroy() {
        engine.nativeStop(handle)
        engine.nativeDestroy(handle)
        handle = 0L
        super.onDestroy()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(CHANNEL_ID, "Extreme Audio Engine", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
        }
        return builder
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("PMNT NOISE LAB")
            .setContentText("Motor de áudio ativo — offline")
            .setOngoing(true)
            .build()
    }

    private companion object {
        const val CHANNEL_ID = "extreme_audio_engine"
        const val NOTIFICATION_ID = 1001
    }
}
