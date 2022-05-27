package com.example.android_agora

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android_agora.databinding.ActivityVideocallBinding
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas

class VideoCallActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideocallBinding
    private var isClicked: Boolean = true

    // Kotlin
    private val PERMISSION_REQ_ID_RECORD_AUDIO = 22
    private val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1

    // Kotlin
    // Fill the App ID of your project generated on Agora Console.
    private val APP_ID = "f39751ad7eef43c5a2318b5407db41c5"

    // Fill the channel name.
    private val CHANNEL = "talkon"

    // Fill the temp token generated on Agora Console.
    private val TOKEN =
        "006f39751ad7eef43c5a2318b5407db41c5IAAbLp53tBn56ls4V37uQ7qaURRQZwyNG7pBSlsX+lnJycTnPvgAAAAAEADOb2NvzE+SYgEAAQDET5Ji"

    private var mRtcEngine: RtcEngine? = null

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                requestCode
            )
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideocallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkSelfPermission(
                Manifest.permission.RECORD_AUDIO,
                PERMISSION_REQ_ID_RECORD_AUDIO
            ) && checkSelfPermission(
                Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA
            )
        ) {
            initializeAndJoinChannel()
        }
        initViews()
    }

    private fun initViews() {
        binding.apply {
            //Button leave click
            bnLeave.setOnClickListener {
                callLeaveButton()

            }

            //Button microphone
            bnMicrophone.setOnClickListener {
                callMicrophoneButton()
            }

            //Button camera
            bnCamera.setOnClickListener {
                callCameraButton()
            }

            //Button camera change

            bnCameraChange.setOnClickListener {
                callCameraChangeButton()
            }

        }

    }


    private val mRtcEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote user joining the channel to get the uid of the user.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                setupRemoteVideo(uid)
            }
        }
    }

    private fun initializeAndJoinChannel() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, APP_ID, mRtcEventHandler)
        } catch (e: Exception) {

        }

        // By default, video is disabled, and you need to call enableVideo to start a video stream.
        mRtcEngine!!.enableVideo()

        val localContainer = findViewById(R.id.local_video_view_container) as FrameLayout
        // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
        val localFrame = RtcEngine.CreateRendererView(baseContext)
        localContainer.addView(localFrame)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        mRtcEngine!!.setupLocalVideo(VideoCanvas(localFrame, VideoCanvas.RENDER_MODE_FIT, 0))

        // Join the channel with a token.
        mRtcEngine!!.joinChannel(TOKEN, CHANNEL, "", 0)
    }

    private fun setupRemoteVideo(uid: Int) {
        val remoteContainer = findViewById(R.id.remote_video_view_container) as FrameLayout

        val remoteFrame = RtcEngine.CreateRendererView(baseContext)
        remoteFrame.setZOrderMediaOverlay(true)
        remoteContainer.addView(remoteFrame)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(remoteFrame, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    // Kotlin
    override fun onDestroy() {
        super.onDestroy()

        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }

    private fun callLeaveButton() {
        mRtcEngine!!.leaveChannel()
        finish()
    }

    private fun callMicrophoneButton() {
        binding.apply {

            if (isClicked) {
                icMicrophone.setImageResource(R.drawable.ic_microphone_off)
                mRtcEngine!!.pauseAudioMixing()
                isClicked = false
            } else {
                icMicrophone.setImageResource(R.drawable.ic_microphone_on)
                mRtcEngine!!.resumeAudio()
                isClicked = true
            }
        }
    }

    private fun callCameraButton() {
        binding.apply {

            if (isClicked) {
                icCamera.setImageResource(R.drawable.ic_camera_off)
                mRtcEngine!!.disableVideo()

                localVideoViewContainer2.visibility = View.VISIBLE
                localUserProfileImage.setImageResource(R.drawable.im_sample_1)


                isClicked = false
            } else {
                localVideoViewContainer2.visibility = View.GONE
                icCamera.setImageResource(R.drawable.ic_camera_on)
                mRtcEngine!!.enableVideo()
                isClicked = true
            }
        }
    }

    private fun callCameraChangeButton() {

        binding.apply {

            if (isClicked) {
                icCameraChange.setImageResource(R.drawable.ic_camera_change)
                isClicked = false
                mRtcEngine!!.switchCamera()
            } else {
                icCameraChange.setImageResource(R.drawable.ic_camera_change)
                mRtcEngine!!.switchCamera()
                isClicked = true
            }

        }

    }

}