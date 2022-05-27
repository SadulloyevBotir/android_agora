package com.example.android_agora

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.view.SurfaceView
import android.widget.Button
import android.widget.FrameLayout
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        var bn_open = findViewById<Button>(R.id.bn_open)
        bn_open.setOnClickListener {
            callViedoCallActivity()
        }
    }

    private fun callViedoCallActivity() {
        var intent = Intent(this, VideoCallActivity::class.java)
        startActivity(intent)
    }


}