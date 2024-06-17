package com.homeapps.dictionary.kotlinsoundservice.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.homeapps.dictionary.kotlinsoundservice.R
import com.homeapps.dictionary.kotlinsoundservice.utility.ForegroundSoundService
import com.homeapps.dictionary.kotlinsoundservice.utility.GetPhoneNumber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val buttonStart: Button = findViewById(R.id.button_start)
        val buttonStop: Button = findViewById(R.id.button_stop)
        val btnPauseSound: Button = findViewById(R.id.btn_pause_sound)

        val tw: TextView = findViewById(R.id.tw)

        buttonStart.setOnClickListener {
            val serviceIntent = Intent(this@MainActivity, ForegroundSoundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                startForegroundService(serviceIntent)
            }
            startService(serviceIntent)
        }

        buttonStop.setOnClickListener {
            val serviceIntent = Intent(this@MainActivity, ForegroundSoundService::class.java)
            stopService(serviceIntent)
        }

        btnPauseSound.setOnClickListener {
            val pauseIntent = Intent(this@MainActivity, ForegroundSoundService::class.java)
            pauseIntent.action = "PAUSE_SOUND"
            startService(pauseIntent)
        }


        tw.setOnClickListener {

            var obj = GetPhoneNumber(this);
            tw.setText((("num" + obj.getPhoneNumber()).toString() + " code :" + obj.getCountryCode()).toString() + "\nlist" + obj.getPhoneNumbers())
        }

    }
}