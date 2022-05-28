package com.example.android_agora

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android_agora.databinding.ActivityVideocallBinding
import com.hbisoft.hbrecorder.HBRecorder
import com.hbisoft.hbrecorder.HBRecorderListener
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas


class VideoCallActivity : AppCompatActivity(), HBRecorderListener {
    lateinit var binding: ActivityVideocallBinding
    private var isClicked: Boolean = true
    lateinit var hbRecorder: HBRecorder
    lateinit var bt_start_record: LinearLayout
    lateinit var ic_record: ImageView
    private lateinit var byteArray: ByteArray

    //Permissions
    private val SCREEN_RECORD_REQUEST_CODE = 777
    private var hasPermissions = false
    // Kotlin
    private val PERMISSION_REQ_ID_RECORD_AUDIO = 22

    private val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    private val PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = PERMISSION_REQ_ID_RECORD_AUDIO + 1

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

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)
            && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {

            //Init HBRecorder
            hbRecorder = HBRecorder(this, this)
            bt_start_record = findViewById(R.id.bn_record)

            initializeAndJoinChannel()
        }
        initViews()
    }

    private fun initViews() {
        //Init HBRecorder
        //Init HBRecorder

        ic_record = findViewById(R.id.ic_record)

        hbRecorder = HBRecorder(this, this)
        binding.apply {
            //Button leave click
            bnLeave.setOnClickListener {
                callLeaveButton()
                hbRecorder!!.stopScreenRecording()
            }

            //Button microphone
            bnMicrophone.setOnClickListener {
                callMicrophoneButton()
            }

            //Button recorder
            bnRecord.setOnClickListener {
                if (hbRecorder!!.isBusyRecording) {
                    hbRecorder!!.stopScreenRecording()
                    ic_record.setImageResource(R.drawable.bt_record)
                } else {
                    ic_record.setImageResource(R.drawable.bt_record_stop)
                    startRecordingScreen()
                }
            }

            //Button camera change

            bnCameraChange.setOnClickListener {
                callCameraChangeButton()
            }
        }
    }

    override fun HBRecorderOnStart() {
        //When the recording starts
    }

    override fun HBRecorderOnComplete() {
        //After file was created
    }

    override fun HBRecorderOnError(errorCode: Int, reason: String?) {
        //When an error occurs
    }

    //Start Button OnClickListener
    private fun startRecordingScreen() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val permissionIntent = mediaProjectionManager?.createScreenCaptureIntent()
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                //Start screen recording
                hbRecorder!!.startScreenRecording(data, resultCode, this)
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
//                icCamera.setImageResource(R.drawable.ic_camera_off)
                mRtcEngine!!.disableVideo()

                localVideoViewContainer2.visibility = View.VISIBLE
                localUserProfileImage.setImageResource(R.drawable.im_sample_1)


                isClicked = false
            } else {
                localVideoViewContainer2.visibility = View.GONE
//                icCamera.setImageResource(R.drawable.ic_camera_on)
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