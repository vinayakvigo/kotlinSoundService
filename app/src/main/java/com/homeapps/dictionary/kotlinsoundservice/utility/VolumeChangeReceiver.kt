package com.homeapps.dictionary.kotlinsoundservice.utility



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.widget.Toast

class VolumeChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            audioManager?.let {
                val currentVolume = it.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVolume = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                if (currentVolume < maxVolume) {
                    it.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
                    Toast.makeText(context, "Volume set to maximum", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
