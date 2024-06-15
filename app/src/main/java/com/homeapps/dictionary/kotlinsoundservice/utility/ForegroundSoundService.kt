package com.homeapps.dictionary.kotlinsoundservice.utility

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder

import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.app.NotificationCompat
import com.homeapps.dictionary.kotlinsoundservice.R
import com.homeapps.dictionary.kotlinsoundservice.activity.MainActivity


class ForegroundSoundService : Service() {

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        private const val TAG = "ForegroundService"
    }

    private var audioManager: AudioManager? = null
    private var volumeChangeReceiver: BroadcastReceiver? = null
    private var isVolumeMaxed = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var handler: Handler
    private var playSoundRunnable: Runnable? = null

    private val RingingTime = 60000 // 3600000; // 1 hour

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        registerVolumeChangeReceiver()
        handler = Handler()
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Foreground Service")
            .setContentText("Playing sound")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)

        val notification = builder.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else{
            startForeground(1, notification)
        }
        if (intent != null) {
            when (intent.action) {
                "PAUSE_SOUND" -> {
                    pauseSound()
                    setMaxVolume()
                    forcePlayThroughSpeaker()
                    playSound() // play sound after every one hour
                }
                else -> {
                    if (mediaPlayer == null) {
                        try {
                            mediaPlayer = MediaPlayer.create(this, R.raw.sound).apply {
                                isLooping = true
                                playSound() // play sound after service start
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    handler.postDelayed(playSoundRunnable!!, RingingTime.toLong()) // Start the hourly playback
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        unregisterReceiver(volumeChangeReceiver)
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun setMaxVolume() {
        audioManager?.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0,
            0
        )
    }

    private fun registerVolumeChangeReceiver() {
        volumeChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                    val currentVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                    val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 0
                    if (currentVolume < maxVolume) {
                        Toast.makeText(context, "Open App and stop Service", Toast.LENGTH_SHORT).show()
                        audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction("android.media.VOLUME_CHANGED_ACTION")
        }
        registerReceiver(volumeChangeReceiver, filter)
    }

    private fun forcePlayThroughSpeaker() {
        audioManager?.apply {
            mode = AudioManager.MODE_IN_COMMUNICATION
            isSpeakerphoneOn = true
        }
    }

    private fun pauseSound() {
        mediaPlayer?.takeIf { it.isPlaying }?.pause()
    }

    private fun playSound() {
        playSoundRunnable = object : Runnable {
            override fun run() {
                if (mediaPlayer != null) {
                    if (!isVolumeMaxed) {
                        setMaxVolume()
                        isVolumeMaxed = true
                    }
                    forcePlayThroughSpeaker()
                    mediaPlayer?.start()
                }
                handler.postDelayed(this, RingingTime.toLong()) // 1 hour = 3600000 ms
            }
        }
    }
}
